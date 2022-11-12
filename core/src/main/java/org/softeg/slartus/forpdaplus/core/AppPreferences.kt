package org.softeg.slartus.forpdaplus.core

import androidx.annotation.ColorRes

interface AppPreferences {
    var language: String

    val accentColor: String

    val screenOrientation: Int

    /**
     * Прокрутка длинного текста в тулбаре
     */
    val titleMarquee: Boolean

    @ColorRes
    fun getAccentColorRes(): Int

    companion object {
        const val ACCENT_COLOR_PINK_NAME = "pink"
        const val ACCENT_COLOR_BLUE_NAME = "blue"
        const val ACCENT_COLOR_GRAY_NAME = "gray"
        const val LANGUAGE_DEFAULT = "default"
    }
}

interface ListPreferences {
    /**
     * Обновлять списки при выборе вкладки
     */
    val refreshListOnTab: Boolean
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
     * Кодировка чата и отправки формы отправки новых сообщений
     */
    val encoding: String

    /**
     * Квадратные аватарки в списке контактов
     */
    val squareAvatars: Boolean

    /**
     * Отображать или нет аватары
     */
    val showAvatars: Boolean
}