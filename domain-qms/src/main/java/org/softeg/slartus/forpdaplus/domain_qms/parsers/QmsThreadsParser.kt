package org.softeg.slartus.forpdaplus.domain_qms.parsers

import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.domain_qms.entities.QmsThreadImpl
import java.util.regex.Pattern

object QmsThreadsParser : Parser<List<QmsThread>> {
    private val newCountPattern by lazy {
        Pattern.compile(
            """([\s\S]*?)\((\d+)\s*/\s*(\d+)\)\s*$""",
            Pattern.CASE_INSENSITIVE
        )
    }
    private val countPattern by lazy {
        Pattern.compile(
            """([\s\S]*?)\((\d+)\)\s*$""",
            Pattern.CASE_INSENSITIVE
        )
    }
    private val strongPattern by lazy {
        Pattern.compile(
            """<strong>([\s\S]*?)</strong>""",
            Pattern.CASE_INSENSITIVE
        )
    }
    private val listGroupPattern by lazy {
        Pattern.compile(
            """<div class="list-group">([\s\S]*)<form [^>]*>([\s\S]*?)</form>""",
            Pattern.CASE_INSENSITIVE
        )
    }

    private val listGroupItemPattern by lazy {
        Pattern.compile(
            """<a class="list-group-item[^>]*-(\d*)">[\s\S]*?<div[^>]*>([\s\S]*?)</div>([\s\S]*?)</a>""",
            Pattern.CASE_INSENSITIVE
        )
    }

    override fun parse(page: String): List<QmsThread> {
        var matcher = listGroupPattern
            .matcher(page)
        val items = mutableListOf<QmsThread>()
        if (!matcher.find()) return items

        matcher = listGroupItemPattern.matcher(matcher.group(2) ?: "")

        while (matcher.find()) {
            val id = matcher.group(1)
            val date = matcher.group(2)
            var title: String?
            var messagesCount: Int? = null
            var newMessagesCount: Int? = null

            val info = matcher.group(3) ?: ""
            var m = strongPattern.matcher(info)
            if (m.find()) {
                m = newCountPattern.matcher(m.group(1) ?: "")
                if (m.find()) {
                    title = m.group(1)?.trim { it <= ' ' }
                    messagesCount = m.group(2)?.filter { it.isDigit() }?.toIntOrNull()
                    newMessagesCount = m.group(3)?.filter { it.isDigit() }?.toIntOrNull()
                } else
                    title = m.group(2)?.trim { it <= ' ' }
            } else {
                m = countPattern.matcher(info)
                if (m.find()) {
                    title = m.group(1)?.trim { it <= ' ' }
                    messagesCount =
                        m.group(2)?.trim { it <= ' ' }?.filter { it.isDigit() }?.toIntOrNull()
                } else
                    title = info.trim { it <= ' ' }
            }
            items.add(
                QmsThreadImpl(
                    id = id,
                    title = title,
                    messagesCount = messagesCount,
                    newMessagesCount = newMessagesCount,
                    lastMessageDate = date
                )
            )
        }

        return items
    }

}