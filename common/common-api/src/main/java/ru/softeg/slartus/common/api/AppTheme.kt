package ru.softeg.slartus.common.api

interface AppTheme {
    suspend fun getStyle(): AppStyle
    suspend fun getAccentColor(): AppAccentColor
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
        private const val THEME_LIGHT = 0
        private const val THEME_DARK = 1
        private const val THEME_BLACK = 6
        private const val THEME_MATERIAL_LIGHT = 2
        private const val THEME_MATERIAL_DARK = 3
        private const val THEME_MATERIAL_BLACK = 5
        private const val THEME_LIGHT_OLD_HD = 4

        fun AppStyle.toOldValue(): Int {
            return when (this) {
                Light -> THEME_LIGHT
                Dark -> THEME_DARK
                Black -> THEME_BLACK
                MaterialLight -> THEME_MATERIAL_LIGHT
                MaterialDark -> THEME_MATERIAL_DARK
                MaterialBlack -> THEME_MATERIAL_BLACK
                Standard4PDA -> THEME_LIGHT_OLD_HD
            }
        }

        fun of(oldValue: Int): AppStyle {
            return when (oldValue) {
                THEME_LIGHT -> Light
                THEME_DARK -> Dark
                THEME_BLACK -> Black
                THEME_MATERIAL_LIGHT -> MaterialLight
                THEME_MATERIAL_DARK -> MaterialDark
                THEME_MATERIAL_BLACK -> MaterialBlack
                THEME_LIGHT_OLD_HD -> Standard4PDA
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

enum class AppAccentColor(val key: String) {
    Pink("pink"),
    Blue("blue"),
    Gray("gray")
}

val AppStyle.isBlack: Boolean get() = this in setOf(AppStyle.Black, AppStyle.MaterialBlack)