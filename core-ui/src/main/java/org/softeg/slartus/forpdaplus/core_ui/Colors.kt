package org.softeg.slartus.forpdaplus.core_ui

import android.graphics.Color

object AppColors {
    val pink=AppColor("pink", Color.rgb(2, 119, 189), Color.rgb(0, 89, 159))
    val blue=AppColor("blue", Color.rgb(233, 30, 99), Color.rgb(203, 0, 69))
    val gray=AppColor("gray", Color.rgb(117, 117, 117), Color.rgb(87, 87, 87))
}

data class AppColor(val name: String, val color: Int, val pressedColor: Int)