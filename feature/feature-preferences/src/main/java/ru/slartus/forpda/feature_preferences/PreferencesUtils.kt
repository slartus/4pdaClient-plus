package ru.slartus.forpda.feature_preferences

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
inline fun <reified T> appPreference(key: String, defaultValue: T): ReadWriteProperty<Any?, T> {
    return when (T::class) {
        String::class -> {
            AppPreferenceString(key, defaultValue as String?) as ReadWriteProperty<Any?, T>
        }
        Boolean::class -> {
            AppPreferenceBoolean(key, defaultValue as Boolean) as ReadWriteProperty<Any?, T>
        }
        Int::class -> {
            AppPreferenceInt(key, defaultValue as Int) as ReadWriteProperty<Any?, T>
        }
        else -> throw Exception("appPreference given unknown class ${T::class}")
    }
}

data class AppPreferenceString(
    val key: String,
    val defaultValue: String?
) : ReadWriteProperty<Any?, String?> {
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        App.getPreferences()?.edit()?.putString(key, value)?.apply()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return App.getPreferences()
            ?.getString(key, defaultValue) ?: defaultValue
    }
}

data class AppPreferenceBoolean(
    private val key: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any?, Boolean> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return App.getPreferences()?.getBoolean(key, defaultValue) ?: defaultValue
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        App.getPreferences()?.edit()?.putBoolean(key, value)?.apply()
    }
}

data class AppPreferenceInt(
    private val key: String,
    private val defaultValue: Int
) : ReadWriteProperty<Any?, Int> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        val strValue =
            App.getPreferences()?.getString(key, defaultValue.toString()) ?: defaultValue.toString()
        return strValue.toIntOrNull() ?: defaultValue
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        App.getPreferences()?.edit()?.putInt(key, value)?.apply()
    }
}