package ru.softeg.slartus.common.api

import android.graphics.Color

enum class AppAccentColor {
    Pink,
    Blue,
    Gray
}

enum class AppAccentColorType {
    Accent,
    Pressed
}

val AppAccentColor.colorRgb: Int
    get() = when (this) {
        AppAccentColor.Pink -> Color.rgb(233, 30, 99)
        AppAccentColor.Blue -> Color.rgb(2, 119, 189)
        AppAccentColor.Gray -> Color.rgb(117, 117, 117)
    }

val AppAccentColor.pressedColorRgb: Int
    get() = when (this) {
        AppAccentColor.Pink -> Color.rgb(203, 0, 69)
        AppAccentColor.Blue -> Color.rgb(0, 89, 159)
        AppAccentColor.Gray -> Color.rgb(87, 87, 87)
    }