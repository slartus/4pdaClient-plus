package org.softeg.slartus.forpdaplus.core

interface AppPreferences {
    var language: String

    val accentColor: String

    companion object {
        const val LANGUAGE_DEFAULT = "default"
    }
}

interface ForumPreferences {
    /**
     * Отображать картинки в разделе "Форум"
     */
    val showImages: Boolean

    /**
     * стартовый форум при открытии раздела "Форум"
     */
    var startForumId: String?
}

interface QmsPreferences {
    /**
     * Квадратные аватарки в списке контактов
     */
    val squareAvatars: Boolean

    /**
     * Отображать или нет аватары
     */
    val showAvatars: Boolean
}