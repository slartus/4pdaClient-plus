package org.softeg.slartus.forpdaplus.topic.data.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.TopicAttachmentsParser
import java.nio.charset.Charset

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
        Assert.assertEquals(null, firstAttachment.count)
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
        Assert.assertEquals(null, lastAttachment.count)
        Assert.assertEquals(
            "https://4pda.to/forum/index.php?act=findpost&pid=9243638",
            lastAttachment.postUrl
        )
    }

    @Test
    fun parseModeratorPage() {
        val parser = TopicAttachmentsParser()
        val page =
            javaClass.classLoader?.getResource("TopicAttachments_1057753_moderator.html")
                ?.readText(Charset.forName("windows-1251")) ?: ""

        val attachments = runBlocking { parser.parse(page) ?: emptyList() }
        Assert.assertEquals(54, attachments.size)

        val firstAttachment = attachments.first()
        Assert.assertEquals("27116522", firstAttachment.id)
        Assert.assertEquals(
            "https://ds-assets.cdn.devapps.ru/vLlQICWAy1u9cUCNugN3lrNVaAN9QEfQtR1c4Qlz0b069eYpn2qdCo23dDbBF.gif",
            firstAttachment.iconUrl
        )
        Assert.assertEquals(
            "https://4pda.to/forum/dl/post/27116522/OpenBank_2.83_4425_AppGallery.apk",
            firstAttachment.url
        )
        Assert.assertEquals("OpenBank_2.83_4425_AppGallery.apk", firstAttachment.name)
        Assert.assertEquals("19.12.22, 15:08", firstAttachment.date)
        Assert.assertEquals("121 МБ", firstAttachment.size)
        Assert.assertEquals(
            "https://4pda.to/forum/index.php?act=findpost&pid=119536287",
            firstAttachment.postUrl
        )
        Assert.assertEquals("56", firstAttachment.count)

        val lastAttachment = attachments.last()
        Assert.assertEquals("23790778", lastAttachment.id)
        Assert.assertEquals(
            "https://ds-assets.cdn.devapps.ru/vLlQ7toREG2np3HBz1a1cEwsUFz1LcsANz2Kox71KGLlddP4Qlz05mz15sHjYiz2LnthaRBA3x.gif",
            lastAttachment.iconUrl
        )
        Assert.assertEquals(
            "https://4pda.to/forum/dl/post/23790778/icon.png",
            lastAttachment.url
        )
        Assert.assertEquals("icon.png", lastAttachment.name)
        Assert.assertEquals("12.09.21, 12:56", lastAttachment.date)
        Assert.assertEquals("0", lastAttachment.count)
        Assert.assertEquals("13.8 КБ", lastAttachment.size)
        Assert.assertEquals(
            "https://4pda.to/forum/index.php?act=findpost&pid=109384025",
            lastAttachment.postUrl
        )
    }
}