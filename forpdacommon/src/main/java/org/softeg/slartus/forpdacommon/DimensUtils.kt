package org.softeg.slartus.forpdacommon

/*
 * Created by slinkin on 30.06.2017.
 */

import android.content.Context


fun Double.dip2px(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale).toInt()
}

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
