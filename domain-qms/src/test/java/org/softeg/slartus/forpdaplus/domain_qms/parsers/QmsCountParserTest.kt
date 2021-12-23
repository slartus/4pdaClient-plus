package org.softeg.slartus.forpdaplus.domain_qms.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class QmsCountParserTest {
    @Test
    fun parse() {
        val parser = QmsCountParser()
        assertEquals(
            runBlocking {
                parser.parse("<a href=\"//4pda.to/forum/index.php?act=qms&amp;\" id=\"events-count\" data-count=\"3\">Сообщений: 3</a>")
            },
            3
        )

        assertEquals(
            runBlocking {
                parser.parse("<a href=\"//4pda.to/forum/index.php?act=qms&amp;\" id=\"events-count\">Сообщений: 3</a>")
            },
            3
        )

        assertEquals(
            runBlocking {
                parser.parse("<span id=\"events-count\" href=\"//4pda.to/forum/index.php?act=qms&amp;code=no\" target=\"_blank\" data-count=\"3\">3</span>")
            },
            3
        )
    }
}