package org.softeg.slartus.forpdaplus.qms.data.screens.thread.parsers

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.softeg.slartus.forpdacommon.NotReportException
import ru.softeg.slartus.qms.api.models.QmsThreadPage
import javax.inject.Inject

class QmsThreadParser @Inject constructor() {
    suspend fun parse(page: String): QmsThreadPage =
        withContext(Dispatchers.Default) {
            val document = Jsoup.parse(page)
            document.selectFirst("div.error")?.let { element ->
                throw NotReportException(element.text())
            }
            document.selectFirst("div.form-error")?.let { element ->
                throw NotReportException(element.text())
            }

            var userId: String? = null
            var userNick: String? = null
            var title: String? = null
            document.selectFirst("span#navbar-title")?.let { element ->
                element.selectFirst("a[href~=showuser=\\d+]")?.let { userElement ->
                    userId = Uri.parse(userElement.attr("href")).getQueryParameter("showuser")
                    userNick = userElement.text().trim()
                }
                title = element.ownText().trim()
            }

            val days = mutableListOf<QmsThreadPage.Day>()
            var dayHeader = ""
            var dayMessages = mutableListOf<String>()
            document.select("div.date, div.list-group-item").map { element ->
                val isDate = "date" in element.classNames()
                if (isDate && dayHeader.isNotEmpty()) {
                    days.add(QmsThreadPage.Day(headerHtml = dayHeader, messagesHtml = dayMessages))
                    dayHeader = ""
                    dayMessages= mutableListOf()
                }
                if (isDate) {
                    dayHeader = element.outerHtml()
                } else {
                    dayMessages.add(element.outerHtml())
                }
            }
            if (dayHeader.isNotEmpty()) {
                days.add(QmsThreadPage.Day(headerHtml = dayHeader, messagesHtml = dayMessages))
            }

            return@withContext QmsThreadPage(
                userId = userId,
                userNick = userNick,
                title = title,
                days = days
            )
        }
}