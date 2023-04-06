package org.softeg.slartus.forpdacommon

import android.content.Context
import android.util.TypedValue

fun Float.dp(context: Context): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
)

fun Int.dp(context: Context): Float = this.toFloat().dp(context)