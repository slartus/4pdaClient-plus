package ru.softeg.slartus.forum.api

interface Forum {
    val id: String?
    val title: String?
    val description: String?
    val isHasTopics: Boolean
    val isHasForums: Boolean
    val iconUrl: String?
    val parentId: String?
}