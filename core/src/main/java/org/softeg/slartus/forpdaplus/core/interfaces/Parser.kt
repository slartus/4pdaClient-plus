package org.softeg.slartus.forpdaplus.core.interfaces

import kotlinx.coroutines.flow.Flow

interface Parser<T> {
    val data: Flow<T>
    suspend fun parse(page: String): T?
}