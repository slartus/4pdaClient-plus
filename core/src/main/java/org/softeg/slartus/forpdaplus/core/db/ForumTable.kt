package org.softeg.slartus.forpdaplus.core.db

import org.softeg.slartus.forpdaplus.core.entities.Forum

interface ForumTable {
    suspend fun getAll(): List<Forum>
    suspend fun merge(forums: List<Forum>)
}