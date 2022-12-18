package org.softeg.slartus.mentions

import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaapi.parsers.MentionsParser
import java.nio.charset.Charset

class MentionsTests {
    @Test
    fun mentionsTest() {
        val page = javaClass.classLoader?.getResource("mentions/mentions_1.html")?.readText(Charset.forName("windows-1251")) ?: ""
        val mentions = MentionsParser.instance.parseMentions(page)
        Assert.assertEquals(mentions.mentions.size, 3)
    }
}