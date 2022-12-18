package org.softeg.slartus.forpdaplus.attachments.impl.models

import android.net.Uri

data class AttachmentsListState(
    val attachments: List<AttachmentState> = emptyList()
)

data class AttachmentState(val uri: Uri)