package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.StateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsContact

interface QmsContactsRepository {
    val contacts: StateFlow<List<QmsContact>>
    suspend fun load()
}