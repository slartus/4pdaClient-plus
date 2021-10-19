package org.softeg.slartus.forpdaplus.core_ui.navigation

sealed class AppScreen {
//    object Preferences : AppScreen()
    data class Topic(val topicId: String) : AppScreen()
    data class Note(val noteId: Int) : AppScreen()
    data class NewNote(
        val title: String? = null,
        val body: String? = null,
        val url: String? = null,
        val topicId: String? = null,
        val topicTitle: String? = null,
        val postId: String? = null,
        val userId: String? = null,
        val userName: String? = null
    ) : AppScreen()

    data class ChooseFileDialog(val resultKey: String) : AppScreen()
}