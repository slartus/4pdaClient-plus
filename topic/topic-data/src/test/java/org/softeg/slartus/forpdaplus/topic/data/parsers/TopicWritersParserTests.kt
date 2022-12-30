package org.softeg.slartus.forpdaplus.topic.data.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.TopicWritersParser
import java.nio.charset.Charset

class TopicWritersParserTests {
    @Test
    fun parse() {
        val parser = TopicWritersParser()
        val page =
            javaClass.classLoader?.getResource("TopicWriters_271502.html")
                ?.readText(charset = Charset.forName("windows-1251")) ?: ""

        val writers = runBlocking { parser.parse(page) }
        Assert.assertEquals(writers.size, 1999)
    }
}