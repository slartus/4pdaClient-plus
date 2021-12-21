package org.softeg.slartus.forpdaplus.core_lib.utils

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
inline fun <reified T> appPreference(
    preferences: SharedPreferences,
    key: String,
    defaultValue: T
): AppPreference<T> {
    return when (T::class) {
        String::class -> AppPreferenceString(preferences, key, defaultValue as String?)
        Boolean::class -> AppPreferenceBoolean(preferences, key, defaultValue as Boolean)
        Int::class -> AppPreferenceInt(preferences, key, defaultValue as Int)
        Float::class -> AppPreferenceFloat(preferences, key, defaultValue as Float)
        else -> throw Exception("appPreference given unknown class ${T::class}")
    } as AppPreference<T>
}

interface AppPreference<T> : ReadWriteProperty<Any?, T> {
    val value: T
}

data class AppPreferenceString(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String?
) : AppPreference<String?> {
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return value
    }

    override val value
        get() = preferences.getString(key, defaultValue) ?: defaultValue
}

data class AppPreferenceBoolean(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
) : AppPreference<Boolean> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override val value
        get() = preferences.getBoolean(key, defaultValue)
}

data class AppPreferenceInt(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Int
) : AppPreference<Int> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    override val value
        get() = try {
            preferences.getInt(key, defaultValue)
        } catch (ex: java.lang.Exception) {
            val strValue =
                preferences.getString(key, defaultValue.toString())
                    ?: defaultValue.toString()
            strValue.toIntOrNull() ?: defaultValue
        }
}

data class AppPreferenceFloat(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Float
) : AppPreference<Float> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    override val value
        get() = try {
            preferences.getFloat(key, defaultValue)
        } catch (ex: java.lang.Exception) {
            val strValue =
                preferences.getString(key, defaultValue.toString())
                    ?: defaultValue.toString()
            strValue.toFloatOrNull() ?: defaultValue
        }
}