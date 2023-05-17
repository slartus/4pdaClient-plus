package org.softeg.slartus.forpdaplus.common.data

import ru.softeg.slartus.common.api.*
import javax.inject.Inject

class AppThemeImpl @Inject constructor(private val settings: Settings) : AppTheme {
    override suspend fun getStyle(): AppStyle {
        return settings.getString(APP_STYLE_OLD_SETTINGS_KEY)
            ?.let { AppStyle.of(it) } ?: AppStyle.Light
    }

    override suspend fun getAccentColor(): AppAccentColor {
        return settings.getEnum<AppAccentColor>(APP_STYLE_ACCENT_COLOR) ?: AppAccentColor.Blue
    }

    override suspend fun updateAccentColor(color: AppAccentColor) {
        settings.putString(APP_STYLE_ACCENT_COLOR, color.name.lowercase())
    }

    companion object {
        const val APP_STYLE_SETTINGS_KEY = "apptheme.style"
        const val APP_STYLE_OLD_SETTINGS_KEY = "appstyle"
        const val APP_STYLE_ACCENT_COLOR = "mainAccentColor"
    }
}