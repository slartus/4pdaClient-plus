package ru.slartus.forpda.feature_preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import androidx.preference.PreferenceManager

val Context.preferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

@Suppress("SameParameterValue")
fun Context?.getAttr(attr: Int, fallbackAttr: Int): Int {
    val value = TypedValue()
    this?.theme?.resolveAttribute(attr, value, true)
    return if (value.resourceId != 0) {
        attr
    } else fallbackAttr
}