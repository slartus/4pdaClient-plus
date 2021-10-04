package org.softeg.slartus.forpdaplus.core_ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import java.io.File
import java.lang.ref.WeakReference

object AppTheme {
    private const val THEME_LIGHT = 0
    const val DEFAULT_APP_THEME = THEME_LIGHT
    private const val THEME_DARK = 1
    private const val THEME_BLACK = 6
    private const val THEME_MATERIAL_LIGHT = 2
    private const val THEME_MATERIAL_DARK = 3
    private const val THEME_MATERIAL_BLACK = 5
    private const val THEME_LIGHT_OLD_HD = 4
    private const val THEME_CUSTOM_CSS = 99
    private const val THEME_TYPE_LIGHT = 0
    private const val THEME_TYPE_DARK = 2
    private const val THEME_TYPE_BLACK = 3
    private val LIGHT_THEMES = arrayOf(THEME_LIGHT, THEME_LIGHT_OLD_HD, THEME_MATERIAL_LIGHT)
    private val DARK_THEMES = arrayOf(THEME_MATERIAL_DARK, THEME_DARK)

    @JvmStatic
    val mainAccentColor: Int
        get() {
            var color = R.color.accentPink
            when (preferences.getString("mainAccentColor", "pink")) {
                "pink" -> color = R.color.accentPink
                "blue" -> color = R.color.accentBlue
                "gray" -> color = R.color.accentGray
            }
            return color
        }


    @JvmStatic
    val prefsThemeStyleResID: Int
        get() {
            var theme = R.style.ThemePrefsLightPink
            val color = preferences.getString("mainAccentColor", "pink")
            when (themeType) {
                THEME_TYPE_LIGHT -> {
                    when (color) {
                        "pink" -> theme = R.style.ThemePrefsLightPink
                        "blue" -> theme = R.style.ThemePrefsLightBlue
                        "gray" -> theme = R.style.ThemePrefsLightGray
                    }
                }
                THEME_TYPE_DARK -> {
                    when (color) {
                        "pink" -> theme = R.style.ThemePrefsDarkPink
                        "blue" -> theme = R.style.ThemePrefsDarkBlue
                        "gray" -> theme = R.style.ThemePrefsDarkGray
                    }
                }
                else -> {
                    when (color) {
                        "pink" -> theme = R.style.ThemePrefsBlackPink
                        "blue" -> theme = R.style.ThemePrefsBlackBlue
                        "gray" -> theme = R.style.ThemePrefsBlackGray
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
                themeType =
                    if (LIGHT_THEMES.indexOf(theme) != -1) THEME_TYPE_LIGHT else if (DARK_THEMES.indexOf(
                            theme
                        ) != -1
                    ) THEME_TYPE_DARK else THEME_TYPE_BLACK
            } else {
                if (themeStr.contains("/dark/")) themeType =
                    THEME_TYPE_DARK else if (themeStr.contains("/black/")) themeType =
                    THEME_TYPE_BLACK
            }
            return themeType
        }

    @JvmStatic
    val currentTheme: String
        get() = preferences.getString("appstyle", DEFAULT_APP_THEME.toString()) ?: DEFAULT_APP_THEME.toString()

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
                "pink" -> cssFile = "4pda_light_blue.css"
                "blue" -> cssFile = "4pda_light_pink.css"
                "gray" -> cssFile = "4pda_light_gray.css"
            }
            THEME_DARK -> when (color) {
                "pink" -> cssFile = "4pda_dark_blue.css"
                "blue" -> cssFile = "4pda_dark_pink.css"
                "gray" -> cssFile = "4pda_dark_gray.css"
            }
            THEME_BLACK -> when (color) {
                "pink" -> cssFile = "4pda_black_blue.css"
                "blue" -> cssFile = "4pda_black_pink.css"
                "gray" -> cssFile = "4pda_black_gray.css"
            }
            THEME_MATERIAL_LIGHT -> cssFile = "material_light.css"
            THEME_MATERIAL_DARK -> cssFile = "material_dark.css"
            THEME_MATERIAL_BLACK -> cssFile = "material_black.css"
            THEME_LIGHT_OLD_HD -> cssFile = "standart_4PDA.css"
            THEME_CUSTOM_CSS -> return Environment.getExternalStorageDirectory().path + "/style.css"
        }
        return path + cssFile
    }

    private var contextRef: WeakReference<Context>? = null
    @JvmStatic
    fun init(context: Context) {
        contextRef = WeakReference(context)
    }

    val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(contextRef?.get())
}