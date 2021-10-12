package org.softeg.slartus.forpdaplus.core_di.implementations

import org.softeg.slartus.forpdaplus.core_ui.html.HtmlStylePreferences
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences

import javax.inject.Inject

class HtmlStylePreferencesImpl @Inject constructor() : HtmlStylePreferences {
    override val isAccelerateGif = Preferences.System.isAccelerateGif
    override val onlyCustomScript = Preferences.System.onlyCustomScript
    override val isDevBounds = Preferences.System.isDevBounds
    override val isDevGrid = Preferences.System.isDevGrid
    override val systemDir = Preferences.System.systemDir
    override val isDevStyle = Preferences.System.isDevStyle
}