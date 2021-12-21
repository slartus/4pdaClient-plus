package org.softeg.slartus.forpdaplus.feature_forum.entities

import org.softeg.slartus.forpdaplus.core.entities.Forum

data class ForumItem(
    override val id: String?,
    override val title: String?,
    override val description: String? = null,
    override val isHasTopics: Boolean = false,
    override val isHasForums: Boolean = false,
    override val iconUrl: String? = null,
    override val parentId: String? = null
) : Forum