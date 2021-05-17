package org.softeg.slartus.forpdaplus.mainnotifiers

import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaplus.TopicParser

class TopicParserTests {

    @Test
    fun parsePostNickTest() {
        var topicBody =
            "<span class=\"post_nick\"><font color=\"green\">●</font> \n" +
                    "  <div class=\"dropdown\">\n" +
                    "   <a href=\"#\" title=\"Вставить ник\" onclick=\"return insertText(&quot;[SNAPBACK]9233031[/SNAPBACK] [B]slartus,[/B] &quot;)\" data-av=\"13/236113-29386826.jpg\" role=\"button\" arial-label=\"Вставить ник в форму ответа\">slartus</a><a data-toggle=\"dropdown\" href=\"#\" role=\"button\" arial-label=\"Меню пользователя\"><i class=\"icon as-text\">ᵥ</i></a>\n" +
                    "   <ul class=\"dropdown-menu\">\n" +
                    "    <li><a href=\"//4pda.to/forum/index.php?showuser=236113\" title=\"Профиль пользователя\" role=\"button\" aria-label=\"Открыть профиль пользователя\">Профиль</a></li>\n" +
                    "    <li><a href=\"//4pda.to/forum/index.php?act=qms&amp;mid=236113\" target=\"qms_236113\" role=\"button\" aria-label=\"Открыть личные сообщения с пользователем\">Сообщения</a></li>\n" +
                    "   </ul>\n" +
                    "  </div>" +
                    "</span>\n"

        var doc = Jsoup.parse(topicBody)
        var user = TopicParser.parsePostNick(doc)
        Assert.assertEquals(user.id, null)
        Assert.assertEquals(user.nick, "slartus")
        Assert.assertEquals(user.state, "green")
        Assert.assertEquals(user.avatar, "13/236113-29386826.jpg")

        topicBody = "<div class=\"post_header\">\n" +
                " <span class=\"post_date\">09.01.21, 22:14&nbsp;| <a href=\"//4pda.to/forum/index.php?showtopic=1014585&amp;view=findpost&amp;p=103244184\" onclick=\"link_to_post(103244184); return false;\">#261</a></span>\n" +
                " <br><span class=\"post_nick\"><font color=\"red\">●</font> \n" +
                "  <div class=\"dropdown\">\n" +
                "   <a href=\"#\" title=\"Вставить ник\" onclick=\"return insertText(&quot;[SNAPBACK]103244184[/SNAPBACK] [B]Network@Wolk,[/B] &quot;)\" data-av=\"13/2566513-2462157.jpg\" role=\"button\" arial-label=\"Вставить ник в форму ответа\"><span class=\"__cf_email__\" data-cfemail=\"bbf5decfccd4c9d0fbecd4d7d0\">[email&nbsp;protected]</span></a><a data-toggle=\"dropdown\" href=\"#\" role=\"button\" arial-label=\"Меню пользователя\"><i class=\"icon as-text\">ᵥ</i></a>\n" +
                "   <ul class=\"dropdown-menu\">\n" +
                "    <li><a href=\"//4pda.to/forum/index.php?showuser=2566513\" title=\"Профиль пользователя\" role=\"button\" aria-label=\"Открыть профиль пользователя\">Профиль</a></li>\n" +
                "    <li><a href=\"//4pda.to/forum/index.php?act=qms&amp;mid=2566513\" target=\"qms_2566513\" role=\"button\" aria-label=\"Открыть личные сообщения с пользователем\">Сообщения</a></li>\n" +
                "   </ul>\n" +
                "  </div></span>\n" +
                " <br><span class=\"post_user_info\"><span style=\"font-weight:bold;color:#008B8B\">Кураторы</span></span>\n" +
                " <br>Реп: <a href=\"//4pda.to/forum/index.php?act=rep&amp;view=win_minus&amp;mid=2566513&amp;p=103244184\" onclick=\"rep_change_window_open(this.href);return false;\" title=\"Опустить репутацию\"><i class=\"icon as-text\">－</i></a> (<a href=\"//4pda.to/forum/index.php?act=rep&amp;view=history&amp;mid=2566513\" title=\"Просмотреть репутацию\"><span data-member-rep=\"2566513\">1148</span></a>) <a href=\"//4pda.to/forum/index.php?act=rep&amp;view=win_add&amp;mid=2566513&amp;p=103244184\" onclick=\"rep_change_window_open(this.href);return false;\" title=\"Поднять репутацию\"><i class=\"icon as-text\">＋</i></a>\n" +
                " <br><span class=\"post_action\"><a href=\"//4pda.to/forum/index.php?act=report&amp;t=1014585&amp;p=103244184&amp;st=260\" target=\"_blank\">Жалоба</a>&nbsp;<a data-quote-link=\"103244184\" href=\"javascript:pasteQ();\" onmouseover=\"copyQ(&quot;Network@Wolk&quot;,'09.01.21, 22:14','103244184')\">Цитата</a></span>\n" +
                "</div>"
        doc = Jsoup.parse(topicBody)
        user = TopicParser.parsePostNick(doc)
        Assert.assertEquals(user.id, null)
        Assert.assertEquals(user.nick, "[email&nbsp;protected]")
        Assert.assertEquals(user.state, "red")
        Assert.assertEquals(user.avatar, "13/2566513-2462157.jpg")
    }
}