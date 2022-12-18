package org.softeg.slartus.forpdaplus.attachments.impl.models

sealed class AttachmentsListEvent {
    object ActionInvoked : AttachmentsListEvent()
}