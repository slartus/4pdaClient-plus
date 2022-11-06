package ru.softeg.slartus.qms.api.models

data class QmsThreadPage(
    val userId: String? = null,
    val userNick: String? = null,
    val title: String? = null,
    val days: List<Day> = emptyList()
) {
    data class Day(val headerHtml: String, val messagesHtml: List<String>)
}

