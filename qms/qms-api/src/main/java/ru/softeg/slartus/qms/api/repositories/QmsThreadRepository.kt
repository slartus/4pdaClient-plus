package ru.softeg.slartus.qms.api.repositories

import ru.softeg.slartus.qms.api.models.QmsThreadPage

interface QmsThreadRepository {
    suspend fun getQmsThread(mid: String, themeId: String, daysCount: Int?): QmsThreadPage
}