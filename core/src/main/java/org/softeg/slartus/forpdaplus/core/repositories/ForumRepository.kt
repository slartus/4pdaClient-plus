package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow

interface ForumRepository {
    val forum: Flow<List<Forum>>
    suspend fun load()
    suspend fun getAll(): List<Forum>
}

data class Forum(
    val id: String?,
    val title: String?,
    val description: String? = null,
    val isHasTopics: Boolean = false,
    val isHasForums: Boolean = false,
    val iconUrl: String? = null,
    val parentId: String? = null
)