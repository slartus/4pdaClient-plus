package org.softeg.slartus.forpdaplus.prefs

import android.content.SharedPreferences
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.AppPreferences.Companion.LANGUAGE_DEFAULT
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core.QmsPreferences
import org.softeg.slartus.forpdaplus.core_lib.utils.appPreference
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import javax.inject.Inject

class AppPreferencesImpl @Inject constructor(preferences: SharedPreferences) : AppPreferences {
    override var language: String by appPreference(preferences, "lang", LANGUAGE_DEFAULT)
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
    override val squareAvatars: Boolean by appPreference(preferences, "isSquareAvarars", true)

    // TODO: переделать без использования Preferences
    override val showAvatars: Boolean
        get() = Preferences.Topic.isShowAvatars
}