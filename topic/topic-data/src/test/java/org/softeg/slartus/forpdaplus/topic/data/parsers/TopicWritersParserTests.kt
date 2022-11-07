package org.softeg.slartus.forpdaplus.topic.data.parsers

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.TopicWritersParser

class TopicWritersParserTests {
    @Test
    fun parse() {
        val parser = TopicWritersParser()

        val writers = runBlocking {
            parser.parse(
                "<!DOCTYPE html><html lang=\"ru-RU\"><head></head><body><div class=\"borderwrap\">\n" +
                        "\t<div class=\"maintitle\" align=\"center\">Кто писал сообщения в: ForPDA</div>\n" +
                        "\t<table class=\"ipbtable\" cellspacing=\"1\">\n" +
                        "\t\t<tr>\n" +
                        "\t\t\t<th width=\"70%\" valign=\"middle\">Автор</th>\n" +
                        "\t\t\t<th width=\"30%\" align=\"center\" valign=\"middle\">Сообщений</th>\n" +
                        "\t\t</tr><tr><td class=\"row1\" valign=\"middle\" align=\"left\"><a href=\"//4pda.to/forum/index.php?showuser=236113\" target=\"_blank\">slartus</a></td><td class=\"row1\" align=\"center\" valign=\"middle\">4663</td></tr><tr><td class=\"row1\" valign=\"middle\" align=\"left\"><a href=\"//4pda.to/forum/index.php?showuser=96664\" target=\"_blank\">Морфий</a></td><td class=\"row1\" align=\"center\" valign=\"middle\">2052</td></tr><tr><td class=\"row1\" valign=\"middle\" align=\"left\"><a href=\"//4pda.to/forum/index.php?showuser=1460862\" target=\"_blank\">gar_alex</a></td><td class=\"row1\" align=\"center\" valign=\"middle\">1654</td></tr><tr><td class=\"row1\" valign=\"middle\" align=\"left\"><a href=\"//4pda.to/forum/index.php?showuser=2556269\" target=\"_blank\">radiation15</a></td><td class=\"row1\" align=\"center\" valign=\"middle\">1048</td></tr>" +
                        "\t\t\t<td class=\"formbuttonrow\" colspan=\"2\"><a href=\"javascript:opener.location=&quot;//4pda.to/forum/index.php?showtopic=271502&quot;;self.close();\">Закрыть окно и открыть тему</a></td>\n" +
                        "\t\t</tr>\n" +
                        "\t\t<tr>\n" +
                        "\t\t\t<td class=\"catend\" colspan=\"2\"><!-- no content --></td>\n" +
                        "\t\t</tr>\n" +
                        "\t</table></div><script data-cfasync=\"false\" src=\"/cdn-cgi/scripts/5c5dd728/cloudflare-static/email-decode.min.js\"></script></body></html>"
            )
        }
        Assert.assertEquals(writers.size, 4)
    }
}