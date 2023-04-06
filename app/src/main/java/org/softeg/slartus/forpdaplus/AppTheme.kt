package org.softeg.slartus.forpdaplus

import android.content.SharedPreferences
import android.graphics.Color
import kotlinx.coroutines.runBlocking
import ru.softeg.slartus.common.api.AppAccentColor
import ru.softeg.slartus.common.api.AppStyle
import ru.softeg.slartus.common.api.AppStyleType
import ru.softeg.slartus.common.api.htmlBackgroundColor

object AppTheme {
    @JvmStatic
    var appTheme: ru.softeg.slartus.common.api.AppTheme? = null

    @JvmStatic
    val appStyle: AppStyle
        get() {
            // TODO: Change
            return runBlocking {
                requireNotNull(appTheme).getStyle()
            }
        }

    var mainAccent: AppAccentColor
        get() {
            return runBlocking {   // TODO: Remove blocking
                requireNotNull(appTheme).getAccentColor()
            }
        }
        set(value) {
            runBlocking { // TODO: Remove blocking
                requireNotNull(appTheme).updateAccentColor(value)
            }
        }

    @JvmStatic
    val mainAccentColor: Int
        get() {
            return mainAccent.colorResId
        }

    @JvmStatic
    val webViewFont: String?
        get() = preferences.getString("webViewFontName", "")

    @JvmStatic
    fun getStatusBarBackgroundColorResId(): Int {
        return when (appStyle.type) {
            AppStyleType.Light -> R.color.statusBar_light
            AppStyleType.Dark -> R.color.statusBar_dark
            AppStyleType.Black -> R.color.statusBar_black
        }
    }

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
    val themeStyleResID: Int
        get() {
            return when (appStyle.type) {
                AppStyleType.Light -> {
                    when (mainAccent) {
                        AppAccentColor.Blue -> R.style.MainBlueLight
                        AppAccentColor.Gray -> R.style.MainGrayLight
                        AppAccentColor.Pink -> R.style.MainPinkLight
                    }
                }
                AppStyleType.Black -> {
                    when (mainAccent) {
                        AppAccentColor.Blue -> R.style.MainBlueBlack
                        AppAccentColor.Gray -> R.style.MainGrayBlack
                        AppAccentColor.Pink -> R.style.MainPinkBlack
                    }
                }
                AppStyleType.Dark -> {
                    when (mainAccent) {
                        AppAccentColor.Blue -> R.style.MainBlueDark
                        AppAccentColor.Gray -> R.style.MainGrayDark
                        AppAccentColor.Pink -> R.style.MainPinkDark
                    }
                }
            }
        }

    @JvmStatic
    val prefsThemeStyleResID: Int
        get() = when (appStyle.type) {
            AppStyleType.Light -> {
                when (mainAccent) {
                    AppAccentColor.Blue -> R.style.ThemePrefsLightBlue
                    AppAccentColor.Gray -> R.style.ThemePrefsLightGray
                    AppAccentColor.Pink -> R.style.ThemePrefsLightPink
                }
            }
            AppStyleType.Black -> {
                when (mainAccent) {
                    AppAccentColor.Blue -> R.style.ThemePrefsDarkBlue
                    AppAccentColor.Gray -> R.style.ThemePrefsDarkGray
                    AppAccentColor.Pink -> R.style.ThemePrefsDarkPink
                }
            }
            AppStyleType.Dark -> {
                when (mainAccent) {
                    AppAccentColor.Blue -> R.style.ThemePrefsBlackBlue
                    AppAccentColor.Gray -> R.style.ThemePrefsBlackGray
                    AppAccentColor.Pink -> R.style.ThemePrefsBlackPink
                }
            }
        }

    @JvmStatic
    val themeBackgroundColorRes: Int
        get() = when (appStyle.type) {
            AppStyleType.Light -> R.color.app_background_light
            AppStyleType.Dark -> R.color.app_background_dark
            AppStyleType.Black -> R.color.app_background_black
        }

    @JvmStatic
    val themeTextColorRes: Int
        get() = when (appStyle.type) {
            AppStyleType.Light -> android.R.color.black
            AppStyleType.Dark -> android.R.color.white
            AppStyleType.Black -> android.R.color.white
        }

    @JvmStatic
    val swipeRefreshBackground: Int
        get() = when (appStyle.type) {
            AppStyleType.Light -> R.color.swipe_background_light
            AppStyleType.Dark -> R.color.swipe_background_dark
            AppStyleType.Black -> R.color.swipe_background_black
        }

    @JvmStatic
    val navBarColor: Int
        get() = when (appStyle.type) {
            AppStyleType.Light -> R.color.navBar_light
            AppStyleType.Dark -> R.color.navBar_dark
            AppStyleType.Black -> R.color.navBar_black
        }

    @JvmStatic
    val drawerMenuText: Int
        get() = when (appStyle.type) {
            AppStyleType.Light -> R.color.drawer_menu_text_light
            AppStyleType.Dark -> R.color.drawer_menu_text_dark
            AppStyleType.Black -> R.color.drawer_menu_text_dark
        }

    @JvmStatic
    val currentBackgroundColorHtml: String
        get() = appStyle.htmlBackgroundColor

    @JvmStatic
    val themeStyleWebViewBackground: Int
        get() = Color.parseColor(currentBackgroundColorHtml)

    @JvmStatic
    val currentTheme: String
        get() = appStyle.name

    @JvmStatic
    val currentThemeName: String
        get() = when (appStyle.type) {
            AppStyleType.Light -> "white"
            AppStyleType.Dark -> "dark"
            AppStyleType.Black -> "black"
        }

    @JvmStatic
    val themeCssFileName: String
        get() {
            val path = "/android_asset/forum/css/"
            val fileName = when (appStyle) {
                AppStyle.Light -> {
                    when (mainAccent) {
                        AppAccentColor.Blue -> "4pda_light_blue.css"
                        AppAccentColor.Pink -> "4pda_light_pink.css"
                        AppAccentColor.Gray -> "4pda_light_gray.css"
                    }
                }
                AppStyle.Dark -> {
                    when (mainAccent) {
                        AppAccentColor.Blue -> "4pda_dark_blue.css"
                        AppAccentColor.Pink -> "4pda_dark_pink.css"
                        AppAccentColor.Gray -> "4pda_dark_gray.css"
                    }
                }
                AppStyle.Black -> {
                    when (mainAccent) {
                        AppAccentColor.Blue -> "4pda_black_blue.css"
                        AppAccentColor.Pink -> "4pda_black_pink.css"
                        AppAccentColor.Gray -> "4pda_black_gray.css"
                    }
                }
                AppStyle.MaterialLight -> "material_light.css"
                AppStyle.MaterialDark -> "material_dark.css"
                AppStyle.MaterialBlack -> "material_black.css"
                AppStyle.Standard4PDA -> "standart_4PDA.css"
            }
            return "$path$fileName"
        }

    private val preferences: SharedPreferences
        get() = App.getInstance().preferences
}

private val AppAccentColor.colorResId: Int
    get() = when (this) {
        AppAccentColor.Pink -> R.color.accentPink
        AppAccentColor.Blue -> R.color.accentBlue
        AppAccentColor.Gray -> R.color.accentGray
    }