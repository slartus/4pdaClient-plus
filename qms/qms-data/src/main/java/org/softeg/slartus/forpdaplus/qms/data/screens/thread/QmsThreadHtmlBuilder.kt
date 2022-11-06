package org.softeg.slartus.forpdaplus.qms.data.screens.thread

import ru.softeg.slartus.qms.api.models.QmsThreadPage

fun buildHtml(
    page: QmsThreadPage,
    daysCount: Int,
    addHtmlOut: (methodName: String) -> String
): String {
    val days = page.days
    if (days.isEmpty())
        return "<div id=\"thread_form\"></div>"

    return buildString {
        appendLine("<span id=\"chatInfo\" style=\"display:none;\">${page.userNick}|:|${page.title}</span>")
        if (days.size > daysCount) {
            appendLoadMoreButton(addHtmlOut, daysCount, days)
        }
        if (days.isNotEmpty()) {
            appendMessages(days, daysCount)
        }
    }
}

private fun StringBuilder.appendMessages(
    days: List<QmsThreadPage.Day>,
    daysCount: Int
) {
    appendHtml("<div id=\"thread_form\"><div id=\"thread-inside-top\"></div>", "</div>") {
        days.takeLast(daysCount).forEach { day ->
            appendLine(day.headerHtml)
            day.messagesHtml.forEach { msg ->
                appendLine(msg)
            }
        }
    }
}

private fun StringBuilder.appendLoadMoreButton(
    addHtmlOut: (methodName: String) -> String,
    daysCount: Int,
    days: List<QmsThreadPage.Day>
) {
    val loadMoreJs = addHtmlOut("loadMore")
    appendHtml("<div class=\"panel\"><div class=\"navi\">", "</div></div>") {
        appendLine("<a id=\"chat_more_button\" class=\"button page\" $loadMoreJs >Загрузить ещё (${daysCount}/${days.size}дн.)</a>")
    }
}

fun java.lang.StringBuilder.appendHtml(
    beginBody: String,
    endBody: String,
    body: StringBuilder.() -> Unit
) {
    this.appendLine(beginBody).apply { body() }.appendLine(endBody)
}