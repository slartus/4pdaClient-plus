package org.softeg.slartus.forpdaapi.parsers

import org.jsoup.Jsoup
import org.softeg.slartus.forpdaapi.vo.MentionsResult
import java.lang.StringBuilder
import java.util.regex.Pattern

/**
 * Парсер кол-ва упоминаний
 */
class MentionsParser private constructor() {
    private object Holder {
        val INSTANCE = MentionsParser()
    }

    private val pattern = Pattern.compile("\\Wact=mentions[^\"]*\"[^\"]*\\sdata-count=\"(\\d+)\"", Pattern.CASE_INSENSITIVE)

    companion object {

        const val TAG = "MentionsParser"
        val instance by lazy { Holder.INSTANCE }
    }

    fun parseCount(page: String): Int? {
        val matcher = pattern.matcher(page)
        if (matcher.find())
            return matcher.group(1)?.toString()?.toIntOrNull()
        return null
    }

    fun parseMentions(page: String): MentionsResult {
        val mentionsResult=parsePages(page)

        return mentionsResult
    }

    fun parseBody(body: String, isWebviewAllowJavascriptInterface:Boolean): String {
        var posts = 0
        val document=Jsoup.parse(body)

        val reultBody=StringBuilder()
        reultBody.append("<div class=\"posts_list search-results\">")
        for(element in document.select("div.borderwrap")){
            reultBody.append("<div class=\"post_container\">")

            var el=element.selectFirst("div.maintitle")
            reultBody.append("<div class=\"topic_title_post\">").append(el.html()).append("</div>\n")

            reultBody.append("</div>").append("</div><div class=\"between_messages\"></div>")
        }

        var userId: String
        var userName: String
        var user: String
        var dateTime: String
        var userState: String

        val matcher = Pattern.compile("<div class=\"cat_name\" style=\"margin-bottom:0\">([\\s\\S]*?)<\\/div>[\\s\\S]*?post_date\">([^\\|&]*)[\\s\\S]*?<font color=\"([^\"]*?)\"[\\s\\S]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)(?:<i[\\s\\S]*?\\/i>)?<\\/a>[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div class=\"cat_name\"|<div><div class=\"pagination\">)").matcher(body)
        while (matcher.find()) {
            m_Body.append("<div class=\"post_container\">")
            m_Body.append("<div class=\"topic_title_post\">").append(matcher.group(1)).append("</div>\n")
            dateTime = matcher.group(2)
            userState = if (matcher.group(3) == "red") "" else "online"
            userId = matcher.group(4)
            userName = matcher.group(5)
            user = "<a class=\"s_inf nick " + userState + "\" " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu", userId, userName) + "><span>" + userName + "</span></a>"
            m_Body.append("<div class=\"post_header\">").append(user).append("<div class=\"s_inf date\"><span>").append(dateTime).append("</span></div></div>")
            m_Body.append("<div class=\"post_body emoticons\">")
            if (m_SpoilerByButton) {
                val find = "(<div class='hidetop' style='cursor:pointer;' )" +
                        "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                        "(Спойлер \\(\\+/-\\).*?</div>)" +
                        "(\\s*<div class='hidemain' style=\"display:none\">)"
                val replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4"
                m_Body.append(Post.modifyBody(matcher.group(6), m_EmoticsDict).replace(find.toRegex(), replace))
            } else {
                m_Body.append(Post.modifyBody(matcher.group(6), m_EmoticsDict))
            }
            m_Body.append("</div>").append("</div><div class=\"between_messages\"></div>")
            posts++
        }

        m_Body.append("</div>")
        return reultBody
    }

    private fun parsePages(page: String): MentionsResult {
        val pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);")
        // http://4pda.ru/forum/index.php?act=search&source=all&result=posts&sort=rel&subforums=1&query=pda&forums=281&st=90
        //final Pattern paginationPattern = Pattern.compile("<div class=\"pagination\">([\\s\\S]*?)<\\/div><\\/div><br");

        val lastPageStartPattern = Pattern.compile("(http://4pda.ru)?/forum/index.php\\?act=search.*?st=(\\d+)")
        val currentPagePattern = Pattern.compile("<span class=\"pagecurrent\">(\\d+)</span>")

        val searchResult = MentionsResult()

        var m = pagesCountPattern.matcher(page)
        if (m.find()) {
            searchResult.setPagesCount(m.group(1))
        }

        m = lastPageStartPattern.matcher(page)
        while (m.find()) {
            searchResult.setLastPageStartCount(m.group(2))
        }

        m = currentPagePattern.matcher(page)
        if (m.find()) {
            searchResult.setCurrentPage(m.group(1))
        } else
            searchResult.setCurrentPage("1")
        return searchResult
    }
}