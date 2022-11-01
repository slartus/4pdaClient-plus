package org.softeg.slartus.forpdaplus.topic.data.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.TopicAttachmentsParser

class TopicAttachmentsParserTests {
    @Test
    fun parse() {
        val parser = TopicAttachmentsParser()

        Assert.assertEquals(
            runBlocking {
                parser.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                        "<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "<head>\n" +
                        "    <title>Прикрепление</title>\n" +
                        "    <link rel=\"stylesheet\" href=\"//ds-assets.cdn.devapps.ru/jiObBMLT4VgrdtmKGz0Bs62Yz1SG.css?_=1651149564\" media=\"all,handheld\"/></head>\n" +
                        "    <body>\n" +
                        "    <div style=\"text-align:left\">\n" +
                        "    <div class=\"borderwrap\">\n" +
                        "    <div class=\"maintitle\">Прикрепления: ForPDA</div>\n" +
                        "    <table class=\"ipbtable\" cellspacing=\"1\">\n" +
                        "    <tr><th width=\"2%\">&nbsp;</th><th width=\"73%\"><b>Прикрепление</b></th><th width=\"5%\">Размер:</b></a></th><th width=\"15%\"><b>Сообщение №</b></th></tr>\n" +
                        "    \n" +
                        "   <script type=\"text/javascript\">\n" +
                        "function openPost(t, event){\n" +
                        "\tevent = event || window.event;\n" +
                        "\tif(opener) {\n" +
                        "\t\tif(event.preventDefault) event.preventDefault();\n" +
                        "\t\topener.location = t.href;\n" +
                        "\t\treturn false;\n" +
                        "\t}\n" +
                        "}\n" +
                        "</script>\n" +
                        "\n" +
                        "<tr id=\"26748492\"><td align=\"center\" class=\"row1\"><img src=\"https://ds-assets.cdn.devapps.ru/jiOb36TRBVtAVRvz0cpKUbknRn1vvT8wlvAMvBeELz1mY8KEXdwI41mDdcn9vf.gif\" alt=\"Прикрепленный файл\"/></td><td class=\"row2\"><a href=\"https://4pda.to/forum/dl/post/26748492/Screenshot_20221031-202002_ForPDA.jpg\" target=\"_blank\">Screenshot_20221031-202002_ForPDA.jpg</a><div class=\"desc\">( Добавлено Вчера, 22:23 )</div></td><td align=\"center\" class=\"row1\">477.51 КБ</td><td class=\"row2\" align=\"center\"><a href=\"https://4pda.to/forum/index.php?act=findpost&pid=118364022\" onclick=\"return openPost(this,event);\" target=\"_blank\">118364022</a></td></tr>\n" +
                        "\n" +
                        "<tr id=\"26748162\"><td align=\"center\" class=\"row1\"><img src=\"https://ds-assets.cdn.devapps.ru/jiOb36TRBVtAVRvz0cpKUbknRn1vvT8wlvAMvBeELz1mY8KEXdwI41mDdcn9vf.gif\" alt=\"Прикрепленный файл\"/></td><td class=\"row2\"><a href=\"https://4pda.to/forum/dl/post/4231498/4pda_v2_0beta5.apk\" target=\"_blank\">4pda_v2_0beta5.apk</a><div class=\"desc\">( Добавлено 25.02.14, 15:59 )</div></td><td align=\"center\" class=\"row1\">4.39 МБ</td><td class=\"row2\" align=\"center\"><a href=\"https://4pda.to/forum/index.php?act=findpost&pid=29644795\" onclick=\"return openPost(this,event);\" target=\"_blank\">29644795</a></td></tr>\n" +
                        "</table></div></div><script data-cfasync=\"false\" src=\"/cdn-cgi/scripts/5c5dd728/cloudflare-static/email-decode.min.js\"></script></body></html>")
                    ?.size
            },
            2
        )
    }
}