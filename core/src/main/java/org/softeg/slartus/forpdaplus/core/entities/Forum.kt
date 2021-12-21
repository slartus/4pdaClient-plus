package org.softeg.slartus.forpdaplus.core.entities

interface Forum {
    val id: String?
    val title: String?
    val description: String?
    val isHasTopics: Boolean
    val isHasForums: Boolean
    val iconUrl: String?
    val parentId: String?
}