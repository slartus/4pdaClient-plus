package org.softeg.slartus.forpdaplus.domain_qms.entities

import org.softeg.slartus.forpdaplus.core.entities.QmsThread

class QmsThreadImpl(
    override val id: String?,
    override val title: String?,
    override val messagesCount: Int?,
    override val newMessagesCount: Int?,
    override val lastMessageDate: String?,
) : QmsThread