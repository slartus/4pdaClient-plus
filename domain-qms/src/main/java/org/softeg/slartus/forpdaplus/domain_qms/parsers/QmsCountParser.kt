package org.softeg.slartus.forpdaplus.domain_qms.parsers

import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import java.util.regex.Pattern

object QmsCountParser : Parser<Int> {
    override fun parse(page: String): Int {
        listOf(pattern, pattern2).forEach {
            val m = it.matcher(page)
            if (m.find())
                return m.group(1)?.toIntOrNull() ?: 0
        }
        return 0
    }

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