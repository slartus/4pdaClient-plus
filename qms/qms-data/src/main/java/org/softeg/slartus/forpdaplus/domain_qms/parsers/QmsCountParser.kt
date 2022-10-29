package org.softeg.slartus.forpdaplus.domain_qms.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsCount
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import java.util.regex.Pattern
import javax.inject.Inject

class QmsCountParser @Inject constructor() : Parser<QmsCount> {
    override val id: String
        get() = QmsCountParser::class.java.simpleName

    private val _data = MutableStateFlow(QmsCount())
    override val data
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return true
    }

    override suspend fun parse(page: String, args: Bundle?): QmsCount {
        var result = QmsCount()
        listOf(pattern, pattern2).forEach {
            val m = it.matcher(page)
            if (m.find()) {
                result = QmsCount(m.group(1)?.toIntOrNull())
                _data.emit(result)
            }
        }
        return result
    }

    companion object {
        private val pattern by lazy {
            Pattern.compile(
                """id="events-count"[^>]*?data-count="(\d+)"""",
                Pattern.CASE_INSENSITIVE
            )
        }

        private val pattern2 by lazy {
            Pattern.compile(
                """id="events-count"[^>]*>[^\d]*?(\d+)<""",
                Pattern.CASE_INSENSITIVE
            )
        }
    }
}