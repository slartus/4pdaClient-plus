package ru.softeg.slartus.forum.api

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface TopicPostRepository {
    suspend fun uploadPostAttachesFlow(postId: String, uris: List<Uri>): Flow<UploadState>
}

sealed class UploadState {
    object Init : UploadState()

    data class Uploading(
        val index: Int,
        val currentUploadFile: UploadFileState
    ) : UploadState() {
        val percents = currentUploadFile.percents
    }

    class AttachUploaded(val uri: Uri, val postAttach: PostAttach?) : UploadState()
    class AttachError(val uri: Uri, val error: Throwable) : UploadState()

    object Completed : UploadState()
    class Error(val error: Throwable) : UploadState()
}


class UploadFileState(val uri: Uri, val percents: Int)

data class PostAttach(val id: String, val name: String)