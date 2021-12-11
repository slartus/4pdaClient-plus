package org.softeg.slartus.forpdacommon

import android.net.Uri
import java.net.URLEncoder

class URIUtils {
    companion object {
        @JvmStatic
        fun createURI(
            scheme: String,
            authority: String,
            path: String,
            params: MutableList<NameValuePair>,
            encoding: String
        ): String {
            val builder =
                Uri.Builder()
                    .scheme(scheme)
                    .authority(authority)

            path.split("/").forEach {
                builder.appendPath(it)
            }

            var url = builder.build().toString()
            url += "?" + params.joinToString("&") {
                "${it.name}=${URLEncoder.encode(it.value ?: "", encoding)}"
            }
            return url
        }

        @JvmStatic
        fun createURI(
            scheme: String,
            authority: String,
            path: String,
            params: Map<String, String>,
            encoding: String
        ): String {
            val builder =
                Uri.Builder()
                    .scheme(scheme)
                    .authority(authority)

            path.split("/").forEach {
                builder.appendPath(it)
            }

            var url = builder.build().toString()
            url += "?" + params
                .map { "${it.key}=${URLEncoder.encode(it.value, encoding)}" }
                .joinToString("&")
            return url
        }
    }
}

class URLEncodedUtils {
    companion object {
        @JvmStatic
        fun format(params: MutableList<NameValuePair>, encoding: String) =
            params.map {
                "${it.name}=${URLEncoder.encode(it.value, encoding)}"
            }.joinToString(separator = "&")

    }
}