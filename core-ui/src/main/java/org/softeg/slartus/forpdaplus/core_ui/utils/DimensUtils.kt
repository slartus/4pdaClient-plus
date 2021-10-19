package org.softeg.slartus.forpdaplus.core_ui.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

fun Double.dip2px(context: Context): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
).roundToInt()

fun Float.dip2px(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale).toInt()
}

fun Int.dip2px(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale).toInt()
}

fun Float.px2dip(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return this / scale
}
