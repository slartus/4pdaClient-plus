package ru.softeg.slartus.qms.api.repositories

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.softeg.slartus.qms.api.models.QmsThreadPage
import ru.softeg.slartus.qms.api.models.UploadState

interface QmsThreadRepository {
    suspend fun getThread(userId: String, threadId: String): QmsThreadPage
    suspend fun sendMessage(
        userId: String,
        threadId: String,
        message: String,
        attachIds: List<String>
    )

    suspend fun deleteMessages(userId: String, threadId: String, postIds: List<String>)

    suspend fun deleteAttach(attachId: String)

    suspend fun uploadAttachesFlow(uris: List<Uri>): Flow<UploadState>
}