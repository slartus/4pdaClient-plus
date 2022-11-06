package ru.softeg.slartus.qms.api.repositories

import ru.softeg.slartus.qms.api.models.QmsThreadPage

interface QmsThreadRepository {
    suspend fun getThread(userId: String, threadId: String): QmsThreadPage
    suspend fun sendMessage(
        userId: String,
        threadId: String,
        message: String,
        attachIds: List<String>
    ): QmsThreadPage
}