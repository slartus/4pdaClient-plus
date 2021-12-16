package org.softeg.slartus.forpdaplus.feature_qms_contacts.entities

import org.softeg.slartus.forpdaplus.core.entities.QmsContact

data class QmsContactImpl(
    override val id: String?,
    override val nick: String?,
    override val avatarUrl: String?,
    override val newMessagesCount: Int?
) : QmsContact