package org.softeg.slartus.forpdaplus.core

interface AppPreferences {
    var language: String

    val forum: ForumPreferences

    companion object {
        const val LANGUAGE_DEFAULT = "default"
    }
}

interface ForumPreferences {
    fun setStartForum(id: String?, title: String?)
    val showImages: Boolean
    val startForumId: String?
}