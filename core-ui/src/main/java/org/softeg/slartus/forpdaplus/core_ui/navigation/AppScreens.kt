package org.softeg.slartus.forpdaplus.core_ui.navigation

sealed class AppScreen {
    data class Topic(val topicId: String) : AppScreen()
    data class Note(val noteId: Int) : AppScreen()
    data class NewNote constructor(
        val title: String? = null,
        val body: String? = null,
        val url: String? = null,
        val topicId: String? = null,
        val topicTitle: String? = null,
        val postId: String? = null,
        val userId: String? = null,
        val userName: String? = null
    ) : AppScreen()
}