package org.softeg.slartus.forpdaplus.qms.data.screens.thread

import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

class RemoteQmsThreadDataSource @Inject constructor(private val httpClient: AppHttpClient) {
    suspend fun getThread(
        userId: String,
        threadId: String
    ): String {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["xhr"] = "body"
        val url = "https://${HostHelper.host}/forum/index.php?act=qms&mid=$userId&t=$threadId"
        return httpClient.performPost(url, additionalHeaders)
    }

    suspend fun sendMessage(
        userId: String,
        threadId: String,
        message: String,
        attachIds: List<String>
    ): String {
        val additionalHeaders = buildMap {
            put("action", "send-message")
            put("mid", userId)
            put("t", threadId)
            put("message", message)
            if (attachIds.any())
                put("attaches", attachIds.joinToString())
        }
        val url = "https://${HostHelper.host}/forum/index.php?act=qms-xhr"

        return httpClient.performPost(
            url, additionalHeaders
        )
    }

    suspend fun deleteMessages(userId: String, threadId: String, postIds: List<String>): String {
        val additionalHeaders = buildMap {
            put("act", "qms")
            put("mid", userId)
            put("t", threadId)
            put("xhr", "body")
            put("do", "1")
            put("action", "delete-messages")
            put("forward-messages-username", "")
            put("forward-thread-username", "")
            put("message", "")
            postIds.forEach { id ->
                put("message-id[$id]", id)
            }
        }

        return httpClient.performPost(
                "https://${HostHelper.host}/forum/index.php?act=qms&mid$userId&t=$threadId&xhr=body&do=1",
                additionalHeaders
            )
    }
}