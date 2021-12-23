package org.softeg.slartus.forpdaplus.domain_qms.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsThread
import org.softeg.slartus.forpdaplus.core.entities.QmsThreads
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.domain_qms.entities.QmsThreadImpl
import java.util.regex.Pattern
import javax.inject.Inject

class QmsThreadsParser @Inject constructor() : Parser<QmsThreads> {
    override val id: String
        get() = QmsThreadsParser::class.java.simpleName

    private val _data = MutableStateFlow(QmsThreads(emptyList()))
    override val data
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return url.contains("""act=qms&mid=\d+""".toRegex())
    }

    override suspend fun parse(page: String, args: Bundle?): QmsThreads {
        var matcher = listGroupPattern
            .matcher(page)

        if (!matcher.find()) return QmsThreads(emptyList())

        matcher = listGroupItemPattern.matcher(matcher.group(2) ?: "")

        val items = mutableListOf<QmsThread>()
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
        val result = QmsThreads(items)
        _data.emit(result)

        return result
    }

    companion object {
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
    }

}