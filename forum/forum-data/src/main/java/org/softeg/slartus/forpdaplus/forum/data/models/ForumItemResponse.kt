package org.softeg.slartus.forpdaplus.forum.data.models

import ru.softeg.slartus.forum.api.ForumItem

class ForumItemResponse(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val isHasTopics: Boolean? = null,
    val isHasForums: Boolean? = null,
    val iconUrl: String? = null,
    val parentId: String? = null
)

fun ForumItemResponse.mapToForumItemOrNull(): ForumItem? {
    return ForumItem(
        id = id ?: return null,
        title = title?: "Not Found",
        description = description.orEmpty(),
        isHasTopics = isHasTopics ?: false,
        isHasForums = isHasForums ?: false,
        iconUrl = iconUrl,
        parentId = parentId
    )
}