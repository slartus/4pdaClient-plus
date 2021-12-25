package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow

interface QmsCountRepository {
    val count: Flow<Int?>
    suspend fun load()
    suspend fun setCount(count: Int)
}