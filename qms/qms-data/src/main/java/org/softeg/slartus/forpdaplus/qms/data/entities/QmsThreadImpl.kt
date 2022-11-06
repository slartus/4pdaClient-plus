package org.softeg.slartus.forpdaplus.qms.data.entities

import ru.softeg.slartus.qms.api.models.QmsThread

class QmsThreadImpl(
    override val id: String?,
    override val title: String?,
    override val messagesCount: Int?,
    override val newMessagesCount: Int?,
    override val lastMessageDate: String?,
) : QmsThread