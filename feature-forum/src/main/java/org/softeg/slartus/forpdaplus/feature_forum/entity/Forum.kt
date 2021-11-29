package org.softeg.slartus.forpdaplus.feature_forum.entity

data class Forum(
    val id: String?,
    val title: String?,
    val description: String? = null,
    val isHasTopics: Boolean = false,
    val isHasForums: Boolean = false,
    val iconUrl: String? = null,
    val parentId: String? = null
)