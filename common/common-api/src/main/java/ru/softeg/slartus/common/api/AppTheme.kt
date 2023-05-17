package ru.softeg.slartus.common.api

import java.io.File

interface AppTheme {
    suspend fun getStyle(): AppStyle
    suspend fun putStyle(appStyle: AppStyle)
    suspend fun getAccentColor(): AppAccentColor
    suspend fun putAccentColor(color: AppAccentColor)
}

sealed class AppStyle(val type: AppStyleType) {
    object Light : AppStyle(AppStyleType.Light)
    object Dark : AppStyle(AppStyleType.Dark)
    object Black : AppStyle(AppStyleType.Black)
    object MaterialLight : AppStyle(AppStyleType.Light)
    object MaterialDark : AppStyle(AppStyleType.Dark)
    object MaterialBlack : AppStyle(AppStyleType.Black)
    object Standard4PDA : AppStyle(AppStyleType.Light)
    class Custom(type: AppStyleType, val cssPath: String) : AppStyle(type)

    companion object {
        @JvmStatic
        fun of(prefsValue: String): AppStyle {
            return when {
                prefsValue == Light.prefsValue -> Light
                prefsValue == Dark.prefsValue -> Dark
                prefsValue == Black.prefsValue -> Black
                prefsValue == MaterialLight.prefsValue -> MaterialLight
                prefsValue == MaterialDark.prefsValue -> MaterialDark
                prefsValue == MaterialBlack.prefsValue -> MaterialBlack
                prefsValue == Standard4PDA.prefsValue -> Standard4PDA
                prefsValue.endsWith(".css") -> Custom(prefsValue.appStyleType, prefsValue)
                else -> Light
            }
        }

        private val String.appStyleType: AppStyleType
            get() = when (split(File.separator).dropLast(1).lastOrNull()?.lowercase()) {
                "black" -> AppStyleType.Black
                "dark" -> AppStyleType.Dark
                "light" -> AppStyleType.Light
                else -> AppStyleType.Light
            }
    }
}

enum class AppStyleType(val light: Boolean) {
    Light(light = true),
    Dark(light = false),
    Black(light = false)
}

val AppStyle.htmlBackgroundColor: String
    get() = when (type) {
        AppStyleType.Light -> "#eeeeee"
        AppStyleType.Dark -> "#1a1a1a"
        AppStyleType.Black -> "#000000"
    }

val AppStyle.prefsValue: String
    get() = when (this) {
        AppStyle.Light -> "0"
        AppStyle.Dark -> "1"
        AppStyle.Black -> "6"
        AppStyle.MaterialLight -> "2"
        AppStyle.MaterialDark -> "3"
        AppStyle.MaterialBlack -> "5"
        AppStyle.Standard4PDA -> "4"
        is AppStyle.Custom -> this.cssPath
    }

fun AppStyle.getCssFilePath(accentColor: AppAccentColor): String {
    val path = "/android_asset/forum/css/"
    val accent = when (accentColor) {
        AppAccentColor.Blue -> "blue"
        AppAccentColor.Pink -> "pink"
        AppAccentColor.Gray -> "gray"
    }
    val fileName = when (this) {
        AppStyle.Light -> "4pda_light_$accent.css"
        AppStyle.Dark -> "4pda_dark_$accent.css"
        AppStyle.Black -> "4pda_black_$accent.css"
        AppStyle.MaterialLight -> "material_light.css"
        AppStyle.MaterialDark -> "material_dark.css"
        AppStyle.MaterialBlack -> "material_black.css"
        AppStyle.Standard4PDA -> "standart_4PDA.css"
        is AppStyle.Custom -> return cssPath
    }
    return "$path$fileName"
}

val AppStyle.isBlack: Boolean get() = this in setOf(AppStyle.Black, AppStyle.MaterialBlack)