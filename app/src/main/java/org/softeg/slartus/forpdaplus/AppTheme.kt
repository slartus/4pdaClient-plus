package org.softeg.slartus.forpdaplus

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Environment
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils
import org.softeg.slartus.forpdaplus.core.AppPreferences
import java.io.File

object AppTheme {
    private const val THEME_LIGHT = 0
    private const val THEME_DARK = 1
    private const val THEME_BLACK = 6
    private const val THEME_MATERIAL_LIGHT = 2
    private const val THEME_MATERIAL_DARK = 3
    private const val THEME_MATERIAL_BLACK = 5
    private const val THEME_LIGHT_OLD_HD = 4
    private const val THEME_CUSTOM_CSS = 99
    const val THEME_TYPE_LIGHT = 0
    const val THEME_TYPE_DARK = 2
    private const val THEME_TYPE_BLACK = 3
    private val LIGHT_THEMES = arrayOf(THEME_LIGHT, THEME_LIGHT_OLD_HD, THEME_MATERIAL_LIGHT)
    private val DARK_THEMES = arrayOf(THEME_MATERIAL_DARK, THEME_DARK)
    @JvmStatic
    val webViewFont: String?
        get() = preferences.getString("webViewFontName", "")

    @JvmStatic
    fun getColorAccent(type: String?): Int {
        var color = 0
        when (type) {
            "Accent" -> color = preferences.getInt("accentColor", Color.rgb(2, 119, 189))
            "Pressed" -> color = preferences.getInt("accentColorPressed", Color.rgb(0, 89, 159))
        }
        return color
    }

    @JvmStatic
    val mainAccentColor: Int
        get() {
            var color = R.color.accentPink
            when (preferences.getString("mainAccentColor", "pink")) {
                AppPreferences.ACCENT_COLOR_PINK_NAME -> color = R.color.accentPink
                AppPreferences.ACCENT_COLOR_BLUE_NAME -> color = R.color.accentBlue
                AppPreferences.ACCENT_COLOR_GRAY_NAME -> color = R.color.accentGray
            }
            return color
        }

    @JvmStatic
    val themeStyleResID: Int
        get() {
            var theme = R.style.ThemeLight
            val color = preferences.getString("mainAccentColor", "pink")
            when (themeType) {
                THEME_TYPE_LIGHT -> {
                    when (color) {
                        AppPreferences.ACCENT_COLOR_PINK_NAME -> theme = R.style.MainPinkLight
                        AppPreferences.ACCENT_COLOR_BLUE_NAME -> theme = R.style.MainBlueLight
                        AppPreferences.ACCENT_COLOR_GRAY_NAME -> theme = R.style.MainGrayLight
                    }
                }
                THEME_TYPE_DARK -> {
                    when (color) {
                        AppPreferences.ACCENT_COLOR_PINK_NAME -> theme = R.style.MainPinkDark
                        AppPreferences.ACCENT_COLOR_BLUE_NAME -> theme = R.style.MainBlueDark
                        AppPreferences.ACCENT_COLOR_GRAY_NAME -> theme = R.style.MainGrayDark
                    }
                }
                else -> {
                    when (color) {
                        AppPreferences.ACCENT_COLOR_PINK_NAME -> theme = R.style.MainPinkBlack
                        AppPreferences.ACCENT_COLOR_BLUE_NAME -> theme = R.style.MainBlueBlack
                        AppPreferences.ACCENT_COLOR_GRAY_NAME -> theme = R.style.MainGrayBlack
                    }
                }
            }
            return theme
        }

    @JvmStatic
    val prefsThemeStyleResID: Int
        get() {
            var theme = R.style.ThemePrefsLightPink
            val color = preferences.getString("mainAccentColor", "pink")
            when (themeType) {
                THEME_TYPE_LIGHT -> {
                    when (color) {
                        AppPreferences.ACCENT_COLOR_PINK_NAME -> theme = R.style.ThemePrefsLightPink
                        AppPreferences.ACCENT_COLOR_BLUE_NAME -> theme = R.style.ThemePrefsLightBlue
                        AppPreferences.ACCENT_COLOR_GRAY_NAME -> theme = R.style.ThemePrefsLightGray
                    }
                }
                THEME_TYPE_DARK -> {
                    when (color) {
                        AppPreferences.ACCENT_COLOR_PINK_NAME -> theme = R.style.ThemePrefsDarkPink
                        AppPreferences.ACCENT_COLOR_BLUE_NAME -> theme = R.style.ThemePrefsDarkBlue
                        AppPreferences.ACCENT_COLOR_GRAY_NAME -> theme = R.style.ThemePrefsDarkGray
                    }
                }
                else -> {
                    when (color) {
                        AppPreferences.ACCENT_COLOR_PINK_NAME -> theme = R.style.ThemePrefsBlackPink
                        AppPreferences.ACCENT_COLOR_BLUE_NAME -> theme = R.style.ThemePrefsBlackBlue
                        AppPreferences.ACCENT_COLOR_GRAY_NAME -> theme = R.style.ThemePrefsBlackGray
                    }
                }
            }
            return theme
        }

    @JvmStatic
    val themeType: Int
        get() {
            var themeType = THEME_TYPE_LIGHT
            val themeStr = currentTheme
            if (themeStr.length < 3) {
                val theme = themeStr.toInt()
                themeType = if (ArrayUtils.indexOf(theme, LIGHT_THEMES) != -1) THEME_TYPE_LIGHT else if (ArrayUtils.indexOf(theme, DARK_THEMES) != -1) THEME_TYPE_DARK else THEME_TYPE_BLACK
            } else {
                if (themeStr.contains("/dark/")) themeType = THEME_TYPE_DARK else if (themeStr.contains("/black/")) themeType = THEME_TYPE_BLACK
            }
            return themeType
        }

    @JvmStatic
    val themeBackgroundColorRes: Int
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) R.color.app_background_light else if (themeType == THEME_TYPE_DARK) R.color.app_background_dark else R.color.app_background_black
        }

    @JvmStatic
    val themeTextColorRes: Int
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) android.R.color.black else if (themeType == THEME_TYPE_DARK) android.R.color.white else android.R.color.white
        }

    @JvmStatic
    val swipeRefreshBackground: Int
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) R.color.swipe_background_light else if (themeType == THEME_TYPE_DARK) R.color.swipe_background_dark else R.color.swipe_background_black
        }

    @JvmStatic
    val navBarColor: Int
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) R.color.navBar_light else if (themeType == THEME_TYPE_DARK) R.color.navBar_dark else R.color.navBar_black
        }

    @JvmStatic
    val drawerMenuText: Int
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) R.color.drawer_menu_text_light else if (themeType == THEME_TYPE_DARK) R.color.drawer_menu_text_dark else R.color.drawer_menu_text_dark
        }
@JvmStatic
    val themeStyleWebViewBackground: Int
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) Color.parseColor("#eeeeee") else if (themeType == THEME_TYPE_DARK) Color.parseColor("#1a1a1a") else Color.parseColor("#000000")
        }

    @JvmStatic
    val currentBackgroundColorHtml: String
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) "#eeeeee" else if (themeType == THEME_TYPE_DARK) "#1a1a1a" else "#000000"
        }

    @JvmStatic
    val currentTheme: String
        get() = preferences.getString("appstyle", THEME_LIGHT.toString())?:THEME_LIGHT.toString()

    @JvmStatic
    val currentThemeName: String
        get() {
            val themeType = themeType
            return if (themeType == THEME_TYPE_LIGHT) "white" else if (themeType == THEME_TYPE_DARK) "dark" else "black"
        }

    private fun checkThemeFile(themePath: String): String {
        return try {
            if (!File(themePath).exists()) { // Toast.makeText(INSTANCE,"не найден файл темы: "+themePath,Toast.LENGTH_LONG).show();
                defaultCssTheme()
            } else themePath
        } catch (ex: Throwable) {
            defaultCssTheme()
        }
    }

    private fun defaultCssTheme(): String {
        return "/android_asset/forum/css/4pda_light_blue.css"
    }
@JvmStatic
    val themeCssFileName: String
        get() {
            val themeStr = currentTheme
            return getThemeCssFileName(themeStr)
        }

    @JvmStatic
    fun getThemeCssFileName(themeStr: String): String {
        if (themeStr.length > 3) return checkThemeFile(themeStr)
        val path = "/android_asset/forum/css/"
        var cssFile = "4pda_light_blue.css"
        val theme = themeStr.toInt()
        if (theme == -1) return themeStr
        val color = preferences.getString("mainAccentColor", "pink")
        when (theme) {
            THEME_LIGHT -> when (color) {
                AppPreferences.ACCENT_COLOR_PINK_NAME -> cssFile = "4pda_light_blue.css"
                AppPreferences.ACCENT_COLOR_BLUE_NAME -> cssFile = "4pda_light_pink.css"
                AppPreferences.ACCENT_COLOR_GRAY_NAME -> cssFile = "4pda_light_gray.css"
            }
            THEME_DARK -> when (color) {
                AppPreferences.ACCENT_COLOR_PINK_NAME -> cssFile = "4pda_dark_blue.css"
                AppPreferences.ACCENT_COLOR_BLUE_NAME -> cssFile = "4pda_dark_pink.css"
                AppPreferences.ACCENT_COLOR_GRAY_NAME -> cssFile = "4pda_dark_gray.css"
            }
            THEME_BLACK -> when (color) {
                AppPreferences.ACCENT_COLOR_PINK_NAME -> cssFile = "4pda_black_blue.css"
                AppPreferences.ACCENT_COLOR_BLUE_NAME -> cssFile = "4pda_black_pink.css"
                AppPreferences.ACCENT_COLOR_GRAY_NAME -> cssFile = "4pda_black_gray.css"
            }
            THEME_MATERIAL_LIGHT -> cssFile = "material_light.css"
            THEME_MATERIAL_DARK -> cssFile = "material_dark.css"
            THEME_MATERIAL_BLACK -> cssFile = "material_black.css"
            THEME_LIGHT_OLD_HD -> cssFile = "standart_4PDA.css"
            THEME_CUSTOM_CSS -> return Environment.getExternalStorageDirectory().path + "/style.css"
        }
        return path + cssFile
    }

    private val preferences: SharedPreferences
        get() = App.getInstance().preferences
}