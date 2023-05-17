package ru.softeg.slartus.common.api

interface AppTheme {
    suspend fun getStyle(): AppStyle
    suspend fun getAccentColor(): AppAccentColor
    suspend fun updateAccentColor(color: AppAccentColor)
}

enum class AppStyle(val type: AppStyleType) {
    Light(AppStyleType.Light),
    Dark(AppStyleType.Dark),
    Black(AppStyleType.Black),
    MaterialLight(AppStyleType.Light),
    MaterialDark(AppStyleType.Dark),
    MaterialBlack(AppStyleType.Black),
    Standard4PDA(AppStyleType.Light);

    companion object {
        fun of(prefsValue: String): AppStyle {
            return when (prefsValue) {
                Light.prefsValue -> Light
                Dark.prefsValue -> Dark
                Black.prefsValue -> Black
                MaterialLight.prefsValue -> MaterialLight
                MaterialDark.prefsValue -> MaterialDark
                MaterialBlack.prefsValue -> MaterialBlack
                Standard4PDA.prefsValue -> Standard4PDA
                else -> Light
            }
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

enum class AppAccentColor {
    Pink,
    Blue,
    Gray
}

val AppStyle.prefsValue: String get() = when(this){
    AppStyle.Light -> "0"
    AppStyle.Dark -> "1"
    AppStyle.Black -> "6"
    AppStyle.MaterialLight -> "2"
    AppStyle.MaterialDark -> "3"
    AppStyle.MaterialBlack -> "5"
    AppStyle.Standard4PDA -> "4"}

val AppStyle.isBlack: Boolean get() = this in setOf(AppStyle.Black, AppStyle.MaterialBlack)