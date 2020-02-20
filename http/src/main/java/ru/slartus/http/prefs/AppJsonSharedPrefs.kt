package ru.slartus.http.prefs

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

class AppJsonSharedPrefs(internal var filePath: String) : IPreferences {
    private val sync = Any()
    override fun getAll(): MutableMap<String, *>? {
        val map = HashMap<String, String?>()
        synchronized(sync) {
            mJSONObject?.let {
                for (key in it.keys()) {
                    map[key] = getContentByKey(key)
                }
            }
        }
        return map

    }

    private var mJSONObject: JSONObject? = null

    init {
        reload()
    }

    fun reload(){
        try {
            if (!MBFileUtils.fileExists(filePath)) {
                // this is important for the first time
                MBFileUtils.createFile(filePath, "{}") // put empty json object
            }
            val json = MBFileUtils.readFile(filePath)
            try {
                mJSONObject = JSONObject(json)
            } catch (e: JSONException) {
                mJSONObject = JSONObject("{}")
                apply()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun contains(key: String) = mJSONObject?.has(key) ?: false

    override fun getInt(key: String, defValue: Int) = tryParseInt(getContentByKey(key), defValue)

    private fun tryParseInt(strVal: String?, defValue: Int): Int {
        if (strVal == null) return defValue
        return try {
            Integer.parseInt(strVal)
        } catch (e: Exception) {
            defValue
        }

    }

    override fun getString(key: String, defValue: String): String {
        val value = getContentByKey(key)
        return value ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val value = getContentByKey(key)
        return if (value == null) defValue else value == "t"
    }

    override fun putInt(key: String, value: Int) {
        putContentByKey(key, value.toString() + "")
    }

    override fun put(key: String, value: Int) {
        putInt(key, value)
    }

    override fun putString(key: String, value: String) {
        putContentByKey(key, value)
    }

    override fun put(key: String, value: String) {
        putString(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        putContentByKey(key, if (value) "t" else "f")
    }

    override fun put(key: String, value: Boolean) {
        putBoolean(key, value)
    }

    private fun commit() {
        if (mJSONObject == null) return
        synchronized(sync) {
            try {
                MBFileUtils.writeToFile(mJSONObject!!.toString(), filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun apply() {
        MBThreadUtils.doOnBackground(Runnable { commit() })
    }

    private fun getContentByKey(key: String): String? {

        if (!contains(key)) return null
        synchronized(sync) {
            return try {
                mJSONObject?.get(key) as String?
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun putContentByKey(key: String, content: String) {
        synchronized(sync) {
            try {
                mJSONObject?.put(key, content)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun remove(key: String) {
        synchronized(sync) {
            try {
                mJSONObject?.remove(key)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun clear(): AppJsonSharedPrefs {
        synchronized(sync) {
            try {
                mJSONObject = JSONObject("{}")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return this
    }
}
