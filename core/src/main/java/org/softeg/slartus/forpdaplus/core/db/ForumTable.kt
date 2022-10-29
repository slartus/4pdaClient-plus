package org.softeg.slartus.forpdaplus.core.db

import ru.softeg.slartus.forum.api.Forum

interface ForumTable {
    suspend fun getAll(): List<Forum>
    suspend fun merge(forums: List<Forum>)
}