package org.softeg.slartus.forpdaplus.topic.data.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.TopicAttachmentsParser

class TopicAttachmentsParserTests {
    @Test
    fun parseUserPage() {
        val parser = TopicAttachmentsParser()
        val page =
            javaClass.classLoader?.getResource("TopicAttachments_271502.html")?.readText() ?: ""

        val attachments = runBlocking { parser.parse(page) ?: emptyList() }
        Assert.assertEquals(9777, attachments.size)

        val firstAttachment = attachments.first()
        Assert.assertEquals("26748492", firstAttachment.id)
        Assert.assertEquals(
            "https://ds-assets.cdn.devapps.ru/jiOb36TRBVtAVRvz0cpKUbknRn1vvT8wlvAMvBeELz1mY8KEXdwI41mDdcn9vf.gif",
            firstAttachment.iconUrl
        )
        Assert.assertEquals(
            "https://4pda.to/forum/dl/post/26748492/Screenshot_20221031-202002_ForPDA.jpg",
            firstAttachment.url
        )
        Assert.assertEquals("Screenshot_20221031-202002_ForPDA.jpg", firstAttachment.name)
        Assert.assertEquals("Вчера, 22:23", firstAttachment.date)
        Assert.assertEquals("477.51 КБ", firstAttachment.size)
        Assert.assertEquals(
            "https://4pda.to/forum/index.php?act=findpost&pid=118364022",
            firstAttachment.postUrl
        )

        val lastAttachment = attachments.last()
        Assert.assertEquals("1196339", lastAttachment.id)
        Assert.assertEquals(
            "https://ds-assets.cdn.devapps.ru/jiOb36TRBVtAVRvz0cpKUbknRn1vvT8wlvAMvBeELz1mY8KEXdwI41mDdcn9vf.gif",
            lastAttachment.iconUrl
        )
        Assert.assertEquals(
            "https://4pda.to/forum/dl/post/1196339/1.jpg",
            lastAttachment.url
        )
        Assert.assertEquals("1.jpg", lastAttachment.name)
        Assert.assertEquals("03.10.11, 02:52", lastAttachment.date)
        Assert.assertEquals("36.33 КБ", lastAttachment.size)
        Assert.assertEquals(
            "https://4pda.to/forum/index.php?act=findpost&pid=9243638",
            lastAttachment.postUrl
        )
    }
}