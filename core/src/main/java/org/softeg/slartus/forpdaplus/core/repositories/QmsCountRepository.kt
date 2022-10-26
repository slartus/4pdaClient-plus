package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow

interface QmsCountRepository {
    val countFlow: Flow<Int>
    suspend fun load()
    suspend fun setCount(count: Int)
}