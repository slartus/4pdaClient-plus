package org.softeg.slartus.forpdaplus.domain_qms

import androidx.core.os.bundleOf
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.hosthelper.HostHelper.Companion.endPoint
import org.softeg.slartus.hosthelper.HostHelper.Companion.schemedHost
import javax.inject.Inject

class QmsServiceImpl @Inject constructor(
    private val httpClient: AppHttpClient,
    private val parseFactory: ParseFactory
) : QmsService {
    override suspend fun getContacts(resultParserId: String): List<QmsContact> {
        val url = "${endPoint}?&act=qms-xhr&action=userlist"
        val pageBody = httpClient.performGet(url)
        return parseFactory.parse(url, pageBody, resultParserId) ?: emptyList()
    }

    override suspend fun deleteContact(contactId: String) {
        val headers =
            mapOf("act" to "qms-xhr", "action" to "del-member", "del-mid" to contactId)
        val url = endPoint
        val pageBody = httpClient.performPost(url, headers)
        parseFactory.parse<Any?>(url, pageBody)
    }

    override suspend fun getQmsCount(resultParserId: String): Int {
        val url = "${schemedHost}/about"
        val pageBody = httpClient.performGet(url)
        return parseFactory.parse(url, pageBody, resultParserId) ?: 0
    }

    override suspend fun getContactThreads(
        contactId: String,
        resultParserId: String?
    ): List<QmsThread> {
        val url = "$endPoint?act=qms&mid=$contactId"
        val pageBody = httpClient.performGet(url)
        return parseFactory.parse(url, pageBody, resultParserId) ?: emptyList()
    }

    override suspend fun getContact(contactId: String, resultParserId: String?): QmsContact? {
        val url = "$endPoint?&act=qms-xhr&action=userlist"
        val pageBody = httpClient.performGet(url)
        return parseFactory.parse(
            url,
            pageBody,
            resultParserId,
            bundleOf(QmsService.ARG_CONTACT_ID to contactId)
        )
        //QmsContactParser(contactId).parse(pageBody)
    }

    override suspend fun deleteThreads(contactId: String, threadIds: List<String>) {
        val additionalHeaders = hashMapOf(
            "action" to "delete-threads",
            "title" to "",
            "message" to ""
        )
        for (id in threadIds) {
            additionalHeaders["thread-id[$id]"] = id
        }
        // not usable response (maybe error check)
        httpClient.performPost(
            "$endPoint?act=qms&xhr=body&do=1&mid=$contactId",
            additionalHeaders
        )
    }

    override suspend fun createNewThread(
        contactId: String,
        userNick: String,
        subject: String,
        message: String, resultParserId: String?
    ): String? {
        val additionalHeaders = mapOf(
            "action" to "create-thread",
            "username" to userNick,
            "title" to subject,
            "message" to message,
        )

        val url = "$endPoint?act=qms&mid=$contactId&xhr=body&do=1"
        val pageBody = httpClient.performPost(url, additionalHeaders)
        return parseFactory.parse(
            url,
            pageBody,
            resultParserId,
            bundleOf(QmsService.ARG_CONTACT_ID to contactId)
        )
    }

}