package org.softeg.slartus.forpdaplus.prefs

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.annotation.ColorRes
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.AppPreferences.Companion.LANGUAGE_DEFAULT
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core.ListPreferences
import org.softeg.slartus.forpdaplus.core.QmsPreferences
import org.softeg.slartus.forpdaplus.core_lib.utils.appPreference
import org.softeg.slartus.forpdaplus.core_res.R
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import javax.inject.Inject

class AppPreferencesImpl @Inject constructor(preferences: SharedPreferences) : AppPreferences {
    override var language: String by appPreference(preferences, "lang", LANGUAGE_DEFAULT)
    override val accentColor: String by appPreference(preferences, "mainAccentColor", "pink")
    override val screenOrientation: Int by appPreference(
        preferences,
        "theme.ScreenOrientation",
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    )
    override val titleMarquee: Boolean by appPreference(preferences, "titleMarquee", false)

    @ColorRes
    override fun getAccentColorRes(): Int {
        return when (accentColor) {
            AppPreferences.ACCENT_COLOR_BLUE_NAME -> R.color.accentBlue
            AppPreferences.ACCENT_COLOR_GRAY_NAME -> R.color.accentGray
            AppPreferences.ACCENT_COLOR_PINK_NAME -> R.color.accentPink
            else -> R.color.accentPink
        }
    }
}

class ForumPreferencesImpl @Inject constructor(preferences: SharedPreferences) :
    ForumPreferences {
    override val showImages: Boolean by appPreference(preferences, "forum.list.show_images", true)

    override var startForumId: String? by appPreference(
        preferences,
        ForumBrickInfo.NAME + ".start_forum_id",
        null
    )
}

class QmsPreferencesImpl @Inject constructor(preferences: SharedPreferences) :
    QmsPreferences {
    override val encoding: String by appPreference(preferences, "qms.chat.encoding", "UTF-8")
    override val squareAvatars: Boolean by appPreference(preferences, "isSquareAvarars", true)

    // TODO: переделать без использования Preferences
    override val showAvatars: Boolean
        get() = Preferences.Topic.isShowAvatars
}

class ListPreferencesImpl @Inject constructor(preferences: SharedPreferences) : ListPreferences {
    override val refreshListOnTab: Boolean by appPreference(
        preferences,
        "lists.refresh_on_tab",
        true
    )
}