package ru.softeg.slartus.attachments.api

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.softeg.slartus.attachments.api.models.UploadState

interface AttachmentsRepository {
    suspend fun uploadQmsAttachesFlow(uris: List<Uri>): Flow<UploadState>
    suspend fun uploadTopicAttachesFlow(
        topicId: String,
        attachedFileIds: List<String>,
        uris: List<Uri>
    ): Flow<UploadState>

    suspend fun uploadPostAttachesFlow(postId: String, uris: List<Uri>): Flow<UploadState>

    suspend fun deleteAttach(attachId: String)
}