package org.softeg.slartus.forpdaplus.domain_qms

import androidx.core.os.bundleOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

class QmsServiceImpl @Inject constructor(
    private val httpClient: AppHttpClient,
    private val parseFactory: ParseFactory
) : QmsService {
    override suspend fun getContacts(resultParserId: String): List<QmsContact> =
        withContext(Dispatchers.IO) {
            val url = "https://${HostHelper.host}/forum/index.php?&act=qms-xhr&action=userlist"
            val pageBody = httpClient.performGet(url)
            parseFactory.parse(url, pageBody, resultParserId) ?: emptyList()
        }

    override suspend fun deleteContact(contactId: String) =
        withContext(Dispatchers.IO) {
            val headers =
                mapOf("act" to "qms-xhr", "action" to "del-member", "del-mid" to contactId)
            val url = "https://${HostHelper.host}/forum/index.php"
            val pageBody = httpClient.performPost(url, headers)
            parseFactory.parse<Any?>(url, pageBody)
            return@withContext
        }

    override suspend fun getQmsCount(resultParserId: String): Int = withContext(Dispatchers.IO) {
        val url = "https://${HostHelper.host}/about"
        val pageBody = httpClient.performGet(url)
        parseFactory.parse(url, pageBody, resultParserId) ?: 0
    }

    override suspend fun getContactThreads(
        contactId: String,
        resultParserId: String
    ): List<QmsThread> =
        withContext(Dispatchers.IO) {
            val url = "https://${HostHelper.host}/forum/index.php?act=qms&mid=$contactId"
            val pageBody = httpClient.performGet(url)
            parseFactory.parse(url, pageBody, resultParserId) ?: emptyList()
        }

    override suspend fun getContact(contactId: String, resultParserId: String): QmsContact? =
        withContext(Dispatchers.IO) {
            val url = "https://${HostHelper.host}/forum/index.php?&act=qms-xhr&action=userlist"
            val pageBody = httpClient.performGet(url)
            parseFactory.parse(
                url,
                pageBody,
                resultParserId,
                bundleOf(QmsService.ARG_CONTACT_ID to contactId)
            )
            //QmsContactParser(contactId).parse(pageBody)
        }

}