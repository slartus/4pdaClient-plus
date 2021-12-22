package org.softeg.slartus.forpdaplus.core.interfaces

import android.os.Bundle
import kotlinx.coroutines.flow.Flow

interface Parser<T> {
    val id: String
    val data: Flow<T>
    fun isOwn(url: String, args: Bundle? = null): Boolean
    suspend fun parse(page: String, args: Bundle? = null): T?
}