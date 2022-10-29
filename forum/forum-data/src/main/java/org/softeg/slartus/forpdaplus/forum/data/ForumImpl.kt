package org.softeg.slartus.forpdaplus.forum.data

import org.softeg.slartus.forpdaplus.core.entities.Forum

data class ForumImpl(
    override val id: String?,
    override val title: String?,
    override val description: String?,
    override val isHasTopics: Boolean,
    override val isHasForums: Boolean,
    override val iconUrl: String?,
    override val parentId: String?
) : Forum