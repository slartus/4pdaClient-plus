package org.softeg.slartus.forpdaplus.qms.data.screens.thread

import ru.softeg.slartus.qms.api.models.QmsThreadPage

fun buildHtml(
    page: QmsThreadPage,
    messagesCount: Int,
    addHtmlOut: (methodName: String) -> String
): String {
    val days = page.days
    if (days.isEmpty())
        return "<div id=\"thread_form\"></div>"

    return buildString {
        appendLine("<span id=\"chatInfo\" style=\"display:none;\">${page.userNick}|:|${page.title}</span>")
        if (page.totalMessagesCount > messagesCount) {
            appendLoadMoreButton(addHtmlOut, messagesCount, page.totalMessagesCount)
        }
        if (days.isNotEmpty()) {
            appendMessages(days, messagesCount)
        }
    }
}

private fun StringBuilder.appendMessages(
    days: List<QmsThreadPage.Day>,
    messagesCount: Int
) {
    appendHtml("<div id=\"thread_form\"><div id=\"thread-inside-top\"></div>", "</div>") {
        val messages = days.takeHtmlLast(messagesCount)
        messages.forEach { html ->
            appendLine(html)
        }
    }
}

private fun List<QmsThreadPage.Day>.takeHtmlLast(messagesCount: Int): List<String> {
    val days = this
    var messagesCounter = messagesCount
    return buildList {
        days.reversed().takeWhile { day ->
            day.messagesHtml.reversed().takeWhile {
                messagesCounter--
                messagesCounter >= 0
            }.forEach { msg ->
                add(msg)
            }
            add(day.headerHtml)
            messagesCounter > 0
        }
    }.reversed()
}

private fun StringBuilder.appendLoadMoreButton(
    addHtmlOut: (methodName: String) -> String,
    messagesCount: Int,
    totalMessagesCount: Int
) {
    val loadMoreJs = addHtmlOut("loadMore")
    val loadAllJs = addHtmlOut("loadAll")
    appendHtml("<div class=\"panel\"><div class=\"navi\">", "</div></div>") {
        appendLine("<a id=\"chat_more_button\" class=\"button page\" $loadAllJs >все ($totalMessagesCount сообщ.)</a>")
        appendLine("<a id=\"chat_more_button\" class=\"button page\" $loadMoreJs >ещё ($messagesCount сообщ.)</a>")
    }
}

fun java.lang.StringBuilder.appendHtml(
    beginBody: String,
    endBody: String,
    body: StringBuilder.() -> Unit
) {
    this.appendLine(beginBody).apply { body() }.appendLine(endBody)
}