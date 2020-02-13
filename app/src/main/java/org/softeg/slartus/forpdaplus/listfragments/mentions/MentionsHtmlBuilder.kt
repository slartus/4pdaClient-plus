package org.softeg.slartus.forpdaplus.listfragments.mentions

import org.softeg.slartus.forpdaapi.vo.MentionsResult
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder
import org.softeg.slartus.forpdaplus.classes.Post
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder
import org.softeg.slartus.forpdaplus.classes.common.Functions
import org.softeg.slartus.forpdaplus.emotic.Smiles

class MentionsHtmlBuilder(private val mentionsResult: MentionsResult) : HtmlBuilder() {
    private var spoilerByButton = false
    private var emoticsDict = Smiles.getSmilesDict()

    init {
        val prefs = App.getInstance().preferences
        spoilerByButton = prefs.getBoolean("theme.SpoilerByButton", false)
    }

    fun build(): String {
        val isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface()

        beginHtml(App.getContext().getString(R.string.search_result))
        beginTopic(mentionsResult)

        m_Body.append("<div class=\"posts_list search-results\">")


        mentionsResult.mentions.forEach { mentionItem ->
            m_Body.append("<div class=\"post_container\">")
            m_Body.append("<div class=\"topic_title_post\"><a href=\"${mentionItem.postUrl}\">${mentionItem.topicTitle}</a></div>\n")
            val dateTime = mentionItem.dateTime
            val userState = mentionItem.userState
            val userId = mentionItem.userId
            val userName = mentionItem.userName
            val postBody = mentionItem.body

            val menu=TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu", userId, userName)
            val user = "<a class=\"s_inf nick $userState\" $menu><span>$userName</span></a>"
            m_Body.append("<div class=\"post_header\">").append(user).append("<div class=\"s_inf date\"><span>").append(dateTime).append("</span></div></div>")
            m_Body.append("<div class=\"post_body emoticons\">")
            if (spoilerByButton) {
                val find = "(<div class='hidetop' style='cursor:pointer;' )" +
                        "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                        "(Спойлер \\(\\+/-\\).*?</div>)" +
                        "(\\s*<div class='hidemain' style=\"display:none\">)"
                val replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4"
                m_Body.append(Post.modifyBody(postBody, emoticsDict).replace(find.toRegex(), replace))
            } else {
                m_Body.append(Post.modifyBody(postBody, emoticsDict))
            }
            m_Body.append("</div>").append("</div><div class=\"between_messages\"></div>")
        }

        m_Body.append("</div>")
        endTopic(mentionsResult)
        return m_Body.toString()
    }


    private fun beginTopic(mentionsResult: MentionsResult) {
        beginBody("search")
        m_Body.append("<div id=\"topMargin\"></div>\n<div class=\"panel top\">")
        if (mentionsResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, mentionsResult.getCurrentPage(), mentionsResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(), true, true)
        }
        m_Body.append("</div>")
        //m_Body.append("<br/><br/>");

    }

    private fun endTopic(mentionsResult: MentionsResult) {
        m_Body.append("<div id=\"entryEnd\"></div>\n")
        m_Body.append("<div class=\"panel bottom\">")
        if (mentionsResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, mentionsResult.getCurrentPage(), mentionsResult.getPagesCount(),
                    Functions.isWebviewAllowJavascriptInterface(), true, false)
        }
        m_Body.append("</div><div id=\"bottomMargin\"></div>")
        endBody()
        endHtml()
    }
}