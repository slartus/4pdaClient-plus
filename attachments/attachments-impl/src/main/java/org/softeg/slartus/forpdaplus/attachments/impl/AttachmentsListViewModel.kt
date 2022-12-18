package org.softeg.slartus.forpdaplus.attachments.impl

import dagger.hilt.android.lifecycle.HiltViewModel
import org.softeg.slartus.forpdaplus.attachments.impl.models.AttachmentsListAction
import org.softeg.slartus.forpdaplus.attachments.impl.models.AttachmentsListEvent
import org.softeg.slartus.forpdaplus.attachments.impl.models.AttachmentsListState
import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import ru.softeg.slartus.attachments.api.AttachmentsRepository
import javax.inject.Inject

@HiltViewModel
class AttachmentsListViewModel @Inject constructor(
    private val attachmentsRepository: AttachmentsRepository
) : BaseViewModel<AttachmentsListState, AttachmentsListAction, AttachmentsListEvent>(
    AttachmentsListState()
) {
    override fun obtainEvent(viewEvent: AttachmentsListEvent) = when (viewEvent) {
        AttachmentsListEvent.ActionInvoked -> viewAction = null
    }
}