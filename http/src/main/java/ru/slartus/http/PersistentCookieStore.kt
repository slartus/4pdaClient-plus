package ru.slartus.http

/*
 * Copyright (c) 2015 Fran Montiel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log

import java.io.File
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

import io.reactivex.subjects.BehaviorSubject
import ru.slartus.http.prefs.AppJsonSharedPrefs

class PersistentCookieStore private constructor(cookieFilePath: String) : CookieStore {


    private val sharedPreferences: AppJsonSharedPrefs = AppJsonSharedPrefs(cookieFilePath)

    // In memory
    private var allCookies: MutableMap<URI, MutableSet<HttpCookie>> = HashMap()

    var memberId = BehaviorSubject.createDefault("")

    init {
        loadAllFromPersistence()
    }

    companion object {
        private var INSTANCE: PersistentCookieStore? = null
        fun getInstance(context: Context): PersistentCookieStore {
            if (INSTANCE == null)
                INSTANCE = PersistentCookieStore(getCookieFilePath(context))
            return INSTANCE!!
        }

        private val TAG = PersistentCookieStore::class.java
                .simpleName

        @Suppress("DEPRECATION")
        private fun getSharedPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

        private fun getSystemDir(context: Context): String {
            var dir: File? = context.filesDir
            if (dir == null)
                dir = context.getExternalFilesDir(null)

            assert(dir != null)
            var res = getSharedPreferences(context).getString("path.system_path", dir!!.path)
            if (!res!!.endsWith(File.separator))
                res += File.separator
            return res
        }

        private fun getCookieFilePath(context: Context): String {

            var res = getSharedPreferences(context).getString("cookies.path", "")

            if (TextUtils.isEmpty(res))
                res = getSystemDir(context) + "cookieStore.json"

            return res!!.replace("/", File.separator)
        }

        private val SP_KEY_DELIMITER = "|" // Unusual char in URL
        private val SP_KEY_DELIMITER_REGEX = "\\" + SP_KEY_DELIMITER

        private val m_URI = URI.create("http://slartus.ru")

        /**
         * Get the real URI from the cookie "domain" and "path" attributes, if they
         * are not set then uses the URI provided (coming from the response)
         */
        private fun cookieUri(uri: URI, cookie: HttpCookie): URI {
            var cookieUri = uri
            if (cookie.domain != null) {
                // Remove the starting dot character of the domain, if exists (e.g: .domain.com -> domain.com)
                var domain = cookie.domain
                if (domain[0] == '.') {
                    domain = domain.substring(1)
                }
                try {
                    cookieUri = URI(if (uri.scheme == null)
                        "http"
                    else
                        uri.scheme, domain,
                            if (cookie.path == null) "/" else cookie.path, null)
                } catch (e: URISyntaxException) {
                    Log.w(TAG, e)
                }

            }
            return cookieUri
        }
    }


    fun reload() {
        sharedPreferences.reload()
        loadAllFromPersistence()
    }

    private fun loadAllFromPersistence() {
        allCookies = HashMap()

        val allPairs = sharedPreferences.all
        for ((key, value) in allPairs!!) {
            val uriAndName = key.split(SP_KEY_DELIMITER_REGEX.toRegex(), 2).toTypedArray()
            try {
                val uri = URI(uriAndName[0])
                val encodedCookie = value as String
                val cookie = SerializableHttpCookie()
                        .decode(encodedCookie)

                var targetCookies: MutableSet<HttpCookie>? = allCookies[uri]
                if (targetCookies == null) {
                    targetCookies = HashSet()
                    allCookies[uri] = targetCookies
                }
                // Repeated cookies cannot exist in persistence
                // targetCookies.remove(cookie)
                addedCookie(cookie!!)
                targetCookies.add(cookie)
            } catch (e: URISyntaxException) {
                Log.w(TAG, e)
            }

        }
    }

    private fun addedCookie(cookie: HttpCookie) {
        if ("member_id" == cookie.name)
            memberId.onNext(cookie.value)
    }

    private fun deletedCookit(cookie: HttpCookie) {
        if ("member_id" == cookie.name)
            memberId.onNext("")
    }

    fun addCustom(key: String, value: String) {
        add(m_URI, HttpCookie(key, value))
    }

    @Synchronized
    override fun add(uri1: URI, cookie: HttpCookie) {
        var uri = uri1
        uri = cookieUri(uri, cookie)

        var targetCookies: MutableSet<HttpCookie>? = allCookies[uri]
        if (targetCookies == null) {
            targetCookies = HashSet()
            allCookies[uri] = targetCookies
        }
        targetCookies.remove(cookie)
        targetCookies.add(cookie)

        saveToPersistence(uri, cookie)
    }

    private fun saveToPersistence(uri: URI, cookie: HttpCookie) {


        sharedPreferences.putString(uri.toString() + SP_KEY_DELIMITER + cookie.name,
                SerializableHttpCookie().encode(cookie)!!)
        sharedPreferences.apply()

        addedCookie(cookie)
    }

    @Synchronized
    override fun get(uri: URI): List<HttpCookie> {
        return getValidCookies(uri)
    }

    @Synchronized
    override fun getCookies(): List<HttpCookie> {
        val allValidCookies = ArrayList<HttpCookie>()
        for (storedUri in allCookies.keys) {
            allValidCookies.addAll(getValidCookies(storedUri))
        }

        return allValidCookies
    }

    private fun getValidCookies(uri: URI): List<HttpCookie> {
        val targetCookies = ArrayList<HttpCookie>()
        // If the stored URI does not have a path then it must match any URI in
        // the same domain
        for (storedUri in allCookies.keys) {
            // Check ith the domains match according to RFC 6265
            if (checkDomainsMatch(storedUri.host, uri.host)) {
                // Check if the paths match according to RFC 6265
                if (checkPathsMatch(storedUri.path, uri.path)) {
                    allCookies[storedUri]?.let { targetCookies.addAll(it) }
                }
            }
        }

        // Check it there are expired cookies and remove them
        if (targetCookies.isNotEmpty()) {
            val cookiesToRemoveFromPersistence = ArrayList<HttpCookie>()
            val it = targetCookies.iterator()
            while (it
                            .hasNext()) {
                val currentCookie = it.next()
                if (currentCookie.hasExpired()) {
                    cookiesToRemoveFromPersistence.add(currentCookie)
                    it.remove()
                }
            }

            if (!cookiesToRemoveFromPersistence.isEmpty()) {
                removeFromPersistence(uri, cookiesToRemoveFromPersistence)
            }
        }
        return targetCookies
    }

    /* http://tools.ietf.org/html/rfc6265#section-5.1.3
    A string domain-matches a given domain string if at least one of the
    following conditions hold:
    o  The domain string and the string are identical.  (Note that both
    the domain string and the string will have been canonicalized to
    lower case at this point.)
    o  All of the following conditions hold:
        *  The domain string is a suffix of the string.
        *  The last character of the string that is not included in the
           domain string is a %x2E (".") character.
        *  The string is a host name (i.e., not an IP address). */

    private fun checkDomainsMatch(cookieHost: String, requestHost: String): Boolean {
        return requestHost == cookieHost || requestHost.endsWith(".$cookieHost")
    }

    /*  http://tools.ietf.org/html/rfc6265#section-5.1.4
        A response-path path-matches a given cookie-path if at least one of
        the following conditions holds:
        o  The cookie-path and the response-path are identical.
        o  The cookie-path is a prefix of the response-path, and the last
        character of the cookie-path is %x2F ("/").
        o  The cookie-path is a prefix of the response-path, and the first
        character of the response-path that is not included in the cookie-
        path is a %x2F ("/") character. */

    private fun checkPathsMatch(cookiePath: String, requestPath: String): Boolean {
        if (cookiePath.isEmpty()&&requestPath.isEmpty())
            return true
        if (cookiePath.isEmpty())
            return false
        return requestPath == cookiePath ||
                requestPath.startsWith(cookiePath) && cookiePath[cookiePath.length - 1] == '/' ||
                requestPath.startsWith(cookiePath) && requestPath.substring(cookiePath.length)[0] == '/'
    }

    private fun removeFromPersistence(uri: URI, cookiesToRemove: List<HttpCookie>) {

        for (cookieToRemove in cookiesToRemove) {
            sharedPreferences.remove(uri.toString() + SP_KEY_DELIMITER
                    + cookieToRemove.name)
            deletedCookit(cookieToRemove)
        }
        sharedPreferences.apply()
    }

    @Synchronized
    override fun getURIs(): List<URI> {
        return ArrayList(allCookies.keys)
    }

    @Synchronized
    override fun remove(uri: URI, cookie: HttpCookie): Boolean {
        val targetCookies = allCookies[uri]
        val cookieRemoved = targetCookies != null && targetCookies
                .remove(cookie)
        if (cookieRemoved) {
            removeFromPersistence(uri, cookie)
        }
        return cookieRemoved

    }

    private fun removeFromPersistence(uri: URI, cookieToRemove: HttpCookie) {

        sharedPreferences.remove(uri.toString() + SP_KEY_DELIMITER
                + cookieToRemove.name)
        sharedPreferences.apply()
        deletedCookit(cookieToRemove)
    }

    @Synchronized
    override fun removeAll(): Boolean {
        allCookies.clear()
        removeAllFromPersistence()
        return true
    }

    private fun removeAllFromPersistence() {
        sharedPreferences.clear().apply()
        memberId.onNext("")
    }


}