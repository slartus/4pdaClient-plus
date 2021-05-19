package org.softeg.slartus.forpdaplus.mainnotifiers

import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Test
import org.softeg.slartus.forpdaapi.common.ParseFunctions
import org.softeg.slartus.forpdaplus.TopicParser

class TopicParserTests {

    @Test
    fun parsePostNickTest() {
        // standart
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

        // [email@protected]
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
        Assert.assertEquals(user.nick, "Network@Wolk")
        Assert.assertEquals(user.state, "red")
        Assert.assertEquals(user.avatar, "13/2566513-2462157.jpg")

        // как видит модератор
        topicBody = "<div class=\"post_header\">" +
                "<label class=\"secheck\"><input type=\"checkbox\" name=\"selectedpids[]\" data-lskey=\"modpids\" data-var=\"ipb_input_t\" data-form=\"form[name=&quot;modform&quot;]\" data-field=\"selectedpids\" value=\"106765412\" onchange=\"ipb.frmChkToggle(this,event)\"><i class=\"check\"></i></label>&nbsp;" +
                "<span class=\"post_date\">Сегодня, 08:44&nbsp;| <a href=\"//4pda.to/forum/index.php?showtopic=673847&view=findpost&p=106765412\" onclick=\"link_to_post(106765412); return false;\">#16902</a>&nbsp;| IP: <a href=\"//4pda.to/forum/index.php?act=profile-xhr&action=mod-ips&iptool=members&ip=5.164.6.97\" target=\"_blank\">5.164.6.97</a></span>" +
                "<br /><span class=\"post_nick\"><font color=\"red\">&#9679;</font> " +
                "<div class=\"dropdown\">" +
                "<a href=\"#\" title=\"Вставить ник\" onclick=\"return insertText(&quot;[SNAPBACK]106765412[/SNAPBACK] [B]vm7,[/B] &quot;)\" data-av=\"04/2346204-13898591.jpg\" role=\"button\" arial-label=\"Вставить ник в форму ответа\">vm7</a><a data-toggle=\"dropdown\" href=\"#\" role=\"button\" arial-label=\"Меню пользователя\"><i class=\"icon as-text\">&#x1d65;</i></a>" +
                "<ul class=\"dropdown-menu\">" +
                "<li><a href=\"//4pda.to/forum/index.php?showuser=2346204\" title=\"Профиль пользователя\" role=\"button\" aria-label=\"Открыть профиль пользователя\">Профиль</a></li>" +
                "<li><a href=\"//4pda.to/forum/index.php?act=qms&mid=2346204\" target=\"qms_2346204\" role=\"button\" aria-label=\"Открыть личные сообщения с пользователем\">Сообщения</a></li>" +
                "</ul>" +
                "</div></span>" +
                "<br/><span class=\"post_user_info\"><span style='color:#FF9900'>Друзья 4PDA</span> | у.п.: &nbsp;(0%)&nbsp;<a href=\"//4pda.to/forum/index.php?act=warn&view=warn&mid=2346204&t=673847&st=16900&pid=106765412\" title=\"Предупреждение/наказание\">+</a></span>" +
                "<br />Реп: <a href=\"//4pda.to/forum/index.php?act=rep&view=win_minus&mid=2346204&p=106765412\" onclick=\"rep_change_window_open(this.href);return false;\" title=\"Опустить репутацию\"><i class=\"icon as-text\">&#xff0d;</i></a> (<a href=\"//4pda.to/forum/index.php?act=rep&view=history&mid=2346204\" title=\"Просмотреть репутацию\"><span data-member-rep=\"2346204\">1160</span></a>) <a href=\"//4pda.to/forum/index.php?act=rep&view=win_add&mid=2346204&p=106765412\" onclick=\"rep_change_window_open(this.href);return false;\" title=\"Поднять репутацию\"><i class=\"icon as-text\">&#xff0b;</i></a>" +
                "<br/><span class=\"post_action\"><a href=\"//4pda.to/forum/index.php?act=report&t=673847&p=106765412&st=16900\" target=\"_blank\">Жалоба</a>&nbsp;<a href=\"//4pda.to/forum/index.php?act=zmod&f=828&t=673847&p=106765412&st=16900&auth_key=ebe0813f4c35b9cd6c870a2fa721d8d5&CODE=pinpost\" title=\"Закрепить пост\" onclick=\"return confirm('Вы действительно хотите поднять данный пост?');\">Поднять</a>&nbsp;<a id=\"edit-but-106765412\" href=\"//4pda.to/forum/index.php?act=post&do=edit_post&f=828&t=673847&p=106765412&st=16900\">Ред.</a>&nbsp;<a href=\"//4pda.to/forum/index.php?act=zmod&auth_key=ebe0813f4c35b9cd6c870a2fa721d8d5&code=postchoice&tact=delete&selectedpids=106765412\" onclick=\"window[&quot;--seMODdel&quot;](this,event,106765412)\" title=\"Удалить пост\">Удал.</a>&nbsp;<a data-quote-link=\"106765412\" href=\"javascript:pasteQ();\" onmouseover=\"copyQ(&quot;vm7&quot;,'16.05.21, 08:44','106765412')\">Цитата</a></span>" +
                "</div>"
        doc = Jsoup.parse(topicBody)
        user = TopicParser.parsePostNick(doc)
        Assert.assertEquals(user.id, null)
        Assert.assertEquals(user.nick, "vm7")
        Assert.assertEquals(user.state, "red")
        Assert.assertEquals(user.avatar, "04/2346204-13898591.jpg")
    }

    @Test
    fun cfDecodeEmailTest() {
        Assert.assertEquals(
            ParseFunctions.cfDecodeEmail("bbf5decfccd4c9d0fbecd4d7d0"),
            "Network@Wolk"
        );
        Assert.assertEquals(
            ParseFunctions.cfDecodeEmail("394a4c4949564b4d79585d5e4c584b5d175a5654"),
            "support@adguard.com"
        );
    }

    @Test
    fun decodeEmailsTest() {
        var text =
            "<span class=\"__cf_email__\" data-cfemail=\"bff1dacbc8d0cdd4ffe8d0d3d4\">[email&#160;protected]</span>"
        Assert.assertEquals(
            ParseFunctions.decodeEmails(text),
            "Network@Wolk"
        )

        text = "<a data-cfemail=\"bff1dacbc8d0cdd4ffe8d0d3d4\">[email&#160;protected]</a>"
        Assert.assertEquals(
            ParseFunctions.decodeEmails(text),
            "Network@Wolk"
        )
        text =
            "<a href=\"/cdn-cgi/l/email-protection\" class=\"__cf_email__\" data-cfemail=\"f983b98a92969a92\">[email&#160;protected]</a>"
        Assert.assertEquals(
            ParseFunctions.decodeEmails(text),
            "z@skock"
        )
    }
}