package org.softeg.slartus.forpdaplus.qms.data.parsers

import android.os.Bundle
import ru.softeg.slartus.qms.api.models.QmsContact
import org.softeg.slartus.forpdaplus.qms.data.screens.contacts.QmsContactsParser
import ru.softeg.slartus.qms.api.QmsService
import javax.inject.Inject

class QmsContactParser @Inject constructor(private val parser: QmsContactsParser) {
    suspend fun parse(page: String, args: Bundle?): QmsContact? {
        return parser.parse(page)
            .firstOrNull { it.id == args?.getString(QmsService.ARG_CONTACT_ID) }
    }
}