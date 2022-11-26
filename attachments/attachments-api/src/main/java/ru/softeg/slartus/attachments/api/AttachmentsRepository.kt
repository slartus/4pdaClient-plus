package ru.softeg.slartus.attachments.api

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.softeg.slartus.attachments.api.models.UploadState

interface AttachmentsRepository {
    suspend fun uploadAttaches(uris: List<Uri>): Flow<UploadState>
    suspend fun deleteAttach(attachId: String)
}