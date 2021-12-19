package org.softeg.slartus.forpdaplus.domain_qms.parsers

import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.interfaces.Parser

data class QmsContactParser(private val contactId: String) : Parser<QmsContact?> {
    override fun parse(page: String): QmsContact? {
        return QmsContactsParser.parse(page).firstOrNull { it.id == contactId }
    }
}