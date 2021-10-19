package org.softeg.slartus.forpdaplus.core_ui.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager

fun Context.getDisplaySize(): Point {
    val width: Int
    val height: Int
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = wm.currentWindowMetrics
        val windowInsets: WindowInsets = windowMetrics.windowInsets

        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
        )
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom

        val b = windowMetrics.bounds
        width = b.width() - insetsWidth
        height = b.height() - insetsHeight
    } else {
        val size = Point()
        val display = wm.defaultDisplay // deprecated in API 30
        display?.getSize(size) // deprecated in API 30
        width = size.x
        height = size.y
    }
    return Point(width, height)
}