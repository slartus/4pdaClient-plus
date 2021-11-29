package org.softeg.slartus.forpdaplus.feature_forum.repository

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.feature_forum.entity.Forum

interface ForumRepository {
    val forum: Flow<List<Forum>>
    suspend fun load()
    suspend fun getAll(): List<Forum>
}