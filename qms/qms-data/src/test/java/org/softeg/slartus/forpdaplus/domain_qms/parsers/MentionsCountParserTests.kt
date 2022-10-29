package org.softeg.slartus.forpdaplus.domain_qms.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class MentionsCountParserTests {
    @Test
    fun parse() {
        val parser = MentionsCountParser()
        Assert.assertEquals(
            runBlocking {
                parser.parse("<li><a href=\"//4pda.to/forum/index.php?act=mentions\" data-count=\"1\" title=\"Открыть список упоминаний\">Упоминания</a></li>").count
            },
            1
        )
    }
}