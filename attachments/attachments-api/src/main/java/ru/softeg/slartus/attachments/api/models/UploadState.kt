package ru.softeg.slartus.attachments.api.models

import android.net.Uri

data class Attachment(
    val id: String,
    val name: String
)

sealed class UploadState {
    object Init : UploadState()

    data class Uploading(
        val index: Int,
        val currentUploadFile: UploadFileState
    ) : UploadState() {
        val percents = currentUploadFile.percents
    }

    class AttachUploaded(val uri: Uri, val postAttach: Attachment) : UploadState()
    class AttachError(val uri: Uri, val error: Throwable) : UploadState()

    object Completed : UploadState()
    class Error(val error: Throwable) : UploadState()
}


class UploadFileState(val uri: Uri, val percents: Int)