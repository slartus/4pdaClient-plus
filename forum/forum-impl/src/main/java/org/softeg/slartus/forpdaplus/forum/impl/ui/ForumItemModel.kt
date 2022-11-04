package org.softeg.slartus.forpdaplus.forum.impl.ui

data class ForumItemModel(
    val id: String?,
    val title: String,
    val description: String = "",
    val isHasTopics: Boolean = false,
    val isHasForums: Boolean = false,
    val iconUrl: String? = null,
    val parentId: String? = null
)