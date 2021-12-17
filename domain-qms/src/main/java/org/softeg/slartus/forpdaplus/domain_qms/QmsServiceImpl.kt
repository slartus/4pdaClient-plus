package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.domain_qms.parsers.QmsContactsParser
import org.softeg.slartus.forpdaplus.domain_qms.parsers.QmsCountParser
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

class QmsServiceImpl @Inject constructor(private val httpClient: AppHttpClient) : QmsService {
    override suspend fun getContacts(): List<QmsContact> {
        return withContext(Dispatchers.IO) {
            val pageBody =
                httpClient.performGet("https://${HostHelper.host}/forum/index.php?&act=qms-xhr&action=userlist")
            QmsContactsParser.parse(pageBody)
        }
    }

    override suspend fun deleteContact(contactId: String) {
        withContext(Dispatchers.IO) {
            val headers =
                mapOf("act" to "qms-xhr", "action" to "del-member", "del-mid" to contactId)
            httpClient
                .performPost("https://${HostHelper.host}/forum/index.php", headers)
        }
    }

    override suspend fun getQmsCount(): Int {
        return withContext(Dispatchers.IO) {
            val pageBody =
                httpClient.performGet("https://${HostHelper.host}/about")
            QmsCountParser.parse(pageBody)
        }
    }
}