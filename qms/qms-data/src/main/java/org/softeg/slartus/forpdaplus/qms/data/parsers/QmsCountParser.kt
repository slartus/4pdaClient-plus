package org.softeg.slartus.forpdaplus.qms.data.parsers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import ru.softeg.slartus.qms.api.models.QmsCount
import java.util.regex.Pattern
import javax.inject.Inject

class QmsCountParser @Inject constructor() : Parser<QmsCount> {
    private val _data = MutableStateFlow(QmsCount())
    override val data
        get() = _data.asStateFlow()

    override suspend fun parse(page: String): QmsCount = withContext(Dispatchers.Default) {
        var result = QmsCount()
        listOf(pattern, pattern2).forEach {
            val m = it.matcher(page)
            if (m.find()) {
                result = QmsCount(m.group(1)?.toIntOrNull())
                _data.emit(result)
            }
        }
        return@withContext result
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