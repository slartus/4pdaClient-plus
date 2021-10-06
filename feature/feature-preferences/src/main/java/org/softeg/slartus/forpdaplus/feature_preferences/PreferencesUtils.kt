package org.softeg.slartus.forpdaplus.feature_preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import androidx.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val Context.preferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

fun Context?.getAttr(attr: Int, fallbackAttr: Int): Int {
    val value = TypedValue()
    this?.theme?.resolveAttribute(attr, value, true)
    return if (value.resourceId != 0) {
        attr
    } else fallbackAttr
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> appPreference(key: String, defaultValue: T): AppPreference<T> {
    return when (T::class) {
        String::class -> AppPreferenceString(key, defaultValue as String?)
        Boolean::class -> AppPreferenceBoolean(key, defaultValue as Boolean)
        Int::class -> AppPreferenceInt(key, defaultValue as Int)
        Float::class -> AppPreferenceFloat(key, defaultValue as Float)
        else -> throw Exception("appPreference given unknown class ${T::class}")
    } as AppPreference<T>
}

interface AppPreference<T> : ReadWriteProperty<Any?, T> {
    val value: T
}

data class AppPreferenceString(
    val key: String,
    val defaultValue: String?
) : AppPreference<String?> {
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        App.getPreferences()?.edit()?.putString(key, value)?.apply()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return value
    }

    override val value
        get() = App.getPreferences()
            ?.getString(key, defaultValue) ?: defaultValue
}

data class AppPreferenceBoolean(
    private val key: String,
    private val defaultValue: Boolean
) : AppPreference<Boolean> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        App.getPreferences()?.edit()?.putBoolean(key, value)?.apply()
    }

    override val value
        get() = App.getPreferences()?.getBoolean(key, defaultValue) ?: defaultValue
}

data class AppPreferenceInt(
    private val key: String,
    private val defaultValue: Int
) : AppPreference<Int> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        App.getPreferences()?.edit()?.putInt(key, value)?.apply()
    }

    override val value
        get() = try {
            App.getPreferences()?.getInt(key, defaultValue) ?: defaultValue
        } catch (ex: java.lang.Exception) {
            val strValue =
                App.getPreferences()?.getString(key, defaultValue.toString())
                    ?: defaultValue.toString()
            strValue.toIntOrNull() ?: defaultValue
        }
}

data class AppPreferenceFloat(
    private val key: String,
    private val defaultValue: Float
) : AppPreference<Float> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        App.getPreferences()?.edit()?.putFloat(key, value)?.apply()
    }

    override val value
        get() = try {
            App.getPreferences()?.getFloat(key, defaultValue) ?: defaultValue
        } catch (ex: java.lang.Exception) {
            val strValue =
                App.getPreferences()?.getString(key, defaultValue.toString())
                    ?: defaultValue.toString()
            strValue.toFloatOrNull() ?: defaultValue
        }
}