package org.softeg.slartus.forpdaplus.prefs

import android.content.SharedPreferences
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.AppPreferences.Companion.LANGUAGE_DEFAULT
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core_lib.utils.appPreference
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import javax.inject.Inject

class AppPreferencesImpl @Inject constructor(
    preferences: SharedPreferences,
    forumPreferences: ForumPreferences
) : AppPreferences {
    override var language: String by appPreference(preferences, "lang", LANGUAGE_DEFAULT)
    override val forum: ForumPreferences = forumPreferences
}

class ForumPreferencesImpl @Inject constructor(private val preferences: SharedPreferences) :
    ForumPreferences {
    override fun setStartForum(id: String?, title: String?) {
        preferences.edit().apply {
            putString(ForumBrickInfo.NAME + ".start_forum_id", id)
            putString(ForumBrickInfo.NAME + ".start_forum_title", title)
        }.apply()
    }

    override val showImages: Boolean by appPreference(preferences, "forum.list.show_images", true)

    override val startForumId: String? by appPreference(
        preferences,
        ForumBrickInfo.NAME + ".start_forum_id",
        null
    )

}