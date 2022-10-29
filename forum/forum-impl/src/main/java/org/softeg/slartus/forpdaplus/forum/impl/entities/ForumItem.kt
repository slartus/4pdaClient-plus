package org.softeg.slartus.forpdaplus.forum.impl.entities

import ru.softeg.slartus.forum.api.Forum


data class ForumItem(
    override val id: String?,
    override val title: String?,
    override val description: String? = null,
    override val isHasTopics: Boolean = false,
    override val isHasForums: Boolean = false,
    override val iconUrl: String? = null,
    override val parentId: String? = null
) : Forum