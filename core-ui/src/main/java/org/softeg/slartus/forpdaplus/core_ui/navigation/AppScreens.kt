package org.softeg.slartus.forpdaplus.core_ui.navigation

sealed class AppScreen {
    data class Topic(val topicId: String) : AppScreen()
}