package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.core.entities.QmsThread

interface QmsThreadsRepository {
    val threads: Flow<List<QmsThread>?>
    suspend fun load(contactId: String)
}