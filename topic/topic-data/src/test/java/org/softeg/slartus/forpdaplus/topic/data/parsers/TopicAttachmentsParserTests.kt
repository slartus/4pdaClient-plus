package org.softeg.slartus.forpdaplus.topic.data.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.TopicAttachmentsParser

class TopicAttachmentsParserTests {
    @Test
    fun parse() {
        val parser = TopicAttachmentsParser()
        val page =
            javaClass.classLoader?.getResource("TopicAttachments_271502.html")?.readText() ?: ""

        Assert.assertEquals(runBlocking { parser.parse(page)?.size }, 9777)
    }
}