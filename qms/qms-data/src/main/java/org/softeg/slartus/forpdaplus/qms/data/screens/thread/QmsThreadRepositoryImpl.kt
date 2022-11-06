package org.softeg.slartus.forpdaplus.qms.data.screens.thread

import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.hosthelper.HostHelper
import ru.softeg.slartus.qms.api.models.QmsThreadPage
import ru.softeg.slartus.qms.api.repositories.QmsThreadRepository
import java.util.HashMap
import javax.inject.Inject

class QmsThreadRepositoryImpl @Inject constructor(
    private val httpClient: AppHttpClient,
    private val parseFactory: ParseFactory,
    private val qmsThreadParser: QmsThreadParser
) : QmsThreadRepository {
    override suspend fun getQmsThread(
        mid: String,
        themeId: String,
        daysCount: Int?
    ): QmsThreadPage {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["xhr"] = "body"
        val url = "https://${HostHelper.host}/forum/index.php?act=qms&mid=$mid&t=$themeId"
        val page = httpClient.performPost(url, additionalHeaders)
        parseFactory.parseAsync(url, page)
        return qmsThreadParser.parse(page)
    }
}