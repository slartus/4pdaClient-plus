package ru.softeg.slartus.qms.api.repositories

import ru.softeg.slartus.qms.api.models.QmsThread

interface QmsThreadsRepository {
    suspend fun load(contactId: String):List<QmsThread>
    suspend fun delete(contactId: String, threadIds: List<String>)
}