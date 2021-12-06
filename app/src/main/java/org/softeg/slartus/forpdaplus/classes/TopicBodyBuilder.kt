package org.softeg.slartus.forpdaplus.classes

import android.content.Context
import android.text.TextUtils
import org.softeg.slartus.forpdacommon.HtmlOutUtils
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic
import org.softeg.slartus.forpdaplus.emotic.Smiles
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl.Companion.instance
import org.softeg.slartus.hosthelper.HostHelper
import java.util.*

/**
 * User: slinkin
 * Date: 26.03.12
 * Time: 16:50
 */
class TopicBodyBuilder(context: Context?, logined: Boolean, topic: ExtTopic?, urlParams: String?,
                       isWebviewAllowJavascriptInterface: Boolean) : HtmlBuilder() {
    private val m_Logined: Boolean
    private val m_IsWebviewAllowJavascriptInterface: Boolean
    var topic: ExtTopic?
        private set
    private val m_UrlParams: String?
    private val m_HtmlPreferences: HtmlPreferences = HtmlPreferences()
    private val m_EmoticsDict: Hashtable<String, String>
    var isMMod = false
    private val m_IsLoadImages: Boolean
    private val m_IsShowAvatars: Boolean
    override fun addScripts() {
        if (m_UrlParams != null) m_Body.append("<script type=\"text/javascript\">window.FORPDA_POST = \"").append(m_UrlParams.replaceFirst("(?:^|\\n)[\\s\\S]*?(#.*|anchor=.*)".toRegex(), "$1")).append("\";</script>\n")
        super.addScripts()
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/topic.js\"></script>\n")
    }

    fun beginTopic() {
        val desc = if (TextUtils.isEmpty(topic!!.description)) "" else ", " + topic!!.description
        super.beginHtml(topic!!.title + desc)
        super.beginBody("topic", null, m_IsLoadImages)
        m_Body.append("<div id=\"topMargin\" style=\"height:").append(ACTIONBAR_TOP_MARGIN).append(";\"></div>")
        m_Body.append("<div class=\"panel top\">")
        if (topic!!.pagesCount > 1) {
            addButtons(m_Body, topic!!.currentPage, topic!!.pagesCount,
                    m_IsWebviewAllowJavascriptInterface, useSelectTextAsNumbers = false, top = true)
        }
        m_Body.append(titleBlock).append("</div>")
    }


    fun openPostsList() {
        m_Body.append("<div class=\"posts_list\">")
    }

    fun endTopic() {
        m_Body.append("</div>")
        m_Body.append("<div name=\"entryEnd\" id=\"entryEnd\"></div>\n")
        m_Body.append("<div class=\"panel bottom\">")
        if (topic!!.pagesCount > 1) {
            addButtons(m_Body, topic!!.currentPage, topic!!.pagesCount,
                    m_IsWebviewAllowJavascriptInterface, useSelectTextAsNumbers = false, top = false)
        }
        if (Preferences.Topic.readersAndWriters) {
            m_Body.append("<div class=\"who\"><a id=\"viewers\" ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showReadingUsers"))
                    .append("><span>Кто читает тему</span></a>\n")
            m_Body.append("<a id=\"writers\" ").append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showWriters"))
                    .append("><span>Кто писал сообщения</span></a></div>\n")
        }
        m_Body.append(titleBlock).append("</div><div id=\"bottomMargin\"></div>")
        //m_Body.append("<div style=\"padding-top:"+ACTIONBAR_TOP_MARGIN+"\"></div>\n");
        super.endBody()
        super.endHtml()
    }

    private fun getSpoiler(title: String, body: String, opened: Boolean): String {
        val stateClass = if (opened) "open" else "close"
        val spoilByButtonTemplate = "<div class='hidetop $stateClass' style='cursor:pointer;'><input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility(this)\"/>"
        val spoilDivTemplate = "<div class='hidetop $stateClass' style='cursor:pointer;' onclick=\"openHat(this);\">"
        val spoilTemplate = if (m_HtmlPreferences.isSpoilerByButton) spoilByButtonTemplate else spoilDivTemplate
        return "<div class=\"hat\">$spoilTemplate$title</div><div class='hidemain'" + (if (opened) " " else " style=\"display:none\"") + ">$body</div></div>"
    }

    fun addPost(post: Post, spoil: Boolean) {
        m_Body.append("<div name=\"entry${post.id}\" ")
                .append(" class=\"jump\" ")
                .append(" style=\"position: absolute; width: 100%; margin-top:-$ACTIONBAR_TOP_MARGIN;left: 0;\" ")
                .append(" id=\"entry${post.id}\" ")
                .append(" post-author-id=\"${post.userId}\" ")
                .append(" post-author=\"${post.nick}\" ")
                .append(" post-date=\"${post.date}\" ")
                .append("></div>\n")
        m_Body
                .append("<div class=\"post_container\" name=\"del${post.id}\"")
                .append(" post-id=\"${post.id}\" ")
                .append(" post-author-id=\"${post.userId}\" ")
                .append(" post-author=\"${post.nick}\" ")
                .append(" post-date=\"${post.date}\" ")
                .append(">")
        addPostHeader(m_Body, post)
        //m_Body.append("<div id=\"msg").append(post.getId()).append("\" name=\"msg").append(post.getId()).append("\">");
        var postBody = post.body.trim { it <= ' ' }
        if (m_HtmlPreferences.isSpoilerByButton) postBody = HtmlPreferences.modifySpoiler(postBody)
        if (spoil) m_Body.append(getSpoiler("<b><span>Показать шапку</span></b>", postBody, false)) else m_Body.append(postBody)
        //m_Body.append("</div>\n\n");
        addFooter(m_Body, post)
        m_Body.append("<div class=\"between_messages\"></div>")
        m_Body.append("</div>")
    }

    val body: String
        get() {
            var res: String
            res = HtmlPreferences.modifyStyleImagesBody(m_Body.toString())
            res = HtmlPreferences.modifyEmoticons(res, m_EmoticsDict)
            if (!m_IsLoadImages) res = HtmlPreferences.modifyAttachedImagesBody(m_IsWebviewAllowJavascriptInterface, res)
            return res
        }

    fun addPoll(value: String, openSpoil: Boolean) {
        m_Body.append("<div class=\"poll\">").append(getSpoiler("<b><span>Опрос</span></b>", value, openSpoil)).append("</div>")
    }

    fun clear() {
        topic = null
        m_Body = null
    }

    private val titleBlock: String
        get() {
            val desc = if (TextUtils.isEmpty(topic!!.description)) "" else "<span class=\"comma\">, </span>" + topic!!.description
            return ("<div class=\"topic_title_post\"><a href=\"https://${HostHelper.host}/forum/index.php?showtopic="
                    + topic!!.id
                    + (if (TextUtils.isEmpty(m_UrlParams)) "" else "&$m_UrlParams") + "\">"
                    + "<span class=\"name\">" + topic!!.title + "</span>"
                    + (if (HtmlPreferences.isFullThemeTitle()) "<span class=\"description\">$desc</span>" else "") + "</a></div>\n")
        }

    private fun addPostHeader(sb: StringBuilder, msg: Post) {
        val nick = msg.nick
        val nickParam = msg.nickParam
        sb.append("<div class=\"post_header\"><div class=\"header_wrapper\">\n")
        //Аватарка
        sb.append("<div class=\"avatar ").append(if (App.getInstance().preferences.getBoolean("isSquareAvarars", false)) "" else "circle ").append(if (m_IsShowAvatars) "\"" else "disable\"")
                .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showUserMenu", arrayOf(msg.id, msg.userId, nickParam)))
        var avatar = msg.avatarFileName
        if (TextUtils.isEmpty(avatar)) {
            avatar = "file:///android_asset/profile/av.png"
        }
        sb.append("><div class=\"img\" style=\"background-image:url(").append(if (m_IsShowAvatars) avatar else "file:///android_asset/profile/av.png").append(");\"></div></div>")
        //Ник
        sb.append("<a class=\"inf nick ")
                .append(if (msg.userState) "online " else "")
                .append(if (msg.isCurator) "curator\"" else "\" ")
                .append(if (!TextUtils.isEmpty(msg.userId)) getHtmlout(m_IsWebviewAllowJavascriptInterface, "showUserMenu", arrayOf(msg.id, msg.userId, nickParam)) else "")
                .append("><span><b>").append(nick).append("</b></span></a>")
        //Группа
        sb.append("<div class=\"inf group\">").append(if (msg.userGroup == null) "" else msg.userGroup).append("</div>")
        //Репутация
        if (!TextUtils.isEmpty(msg.userId)) {
            sb.append("<a class=\"inf reputation\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showRepMenu", arrayOf(msg.id, msg.userId, msg.nickParam, if (msg.canPlusRep) "1" else "0", if (msg.canMinusRep) "1" else "0")))
                    .append("><span>").append(msg.userReputation).append("</span></a>")
        }
        //Дата
        sb.append("<div class=\"date-link\"><span class=\"inf date\"><span>").append(msg.date).append("</span></span>")
        //Ссылка на пост
        sb.append("<a class=\"inf link\" ")
                .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostLinkMenu", msg.id))
                .append("><span><span class=\"sharp\">#</span>").append(msg.number).append("</span></a></div>")
        //Меню
        if (Client.getInstance().logined) {
            sb.append("<a class=\"inf menu\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostMenu", arrayOf(msg.id, msg.date, msg.userId, nickParam, if (msg.canEdit) "1" else "0", if (msg.canDelete) "1" else "0")))
                    .append("><span>Меню</span></a>")
        }
        sb.append("</div></div>\n")
    }

    private fun addFooter(sb: StringBuilder, post: Post) {
        sb.append("<div class=\"post_footer")
                .append(if (post.canDelete) " delete" else "")
                .append(if (post.canEdit) " edit" else "")
                .append(if (topic!!.isPostVote) "" else " nopostvote")
                .append("\">")
        if (m_Logined) {
            val nickParam = post.nickParam
            var postNumber = post.number
            try {
                postNumber = (post.number.toInt() - 1).toString()
            } catch (ignored: Throwable) {
            }
            sb.append(String.format("<a class=\"link button claim\" href=\"/forum/index.php?act=report&t=%s&p=%s&st=%s\"><span>Жалоба</span></a>",
                    topic!!.id, post.id, postNumber))
            sb.append("<a class=\"button nick\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "insertTextToPost", String.format("[SNAPBACK]%s[/SNAPBACK] [B]%s,[/B] \\n", post.id, nickParam)))
                    .append("><span>Ник</span></a>")
            sb.append("<a class=\"button quote\" ")
                    .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "quote", arrayOf(topic!!.forumId, topic!!.id, post.id, post.date, post.userId, nickParam)))
                    .append("><span>Цитата</span></a>")
            if ((instance.getId() != post.userId) and topic!!.isPostVote) {
                sb.append("<a class=\"button vote bad\" ")
                        .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "postVoteBad", post.id))
                        .append("><span>Плохо</span></a>")
                sb.append("<a class=\"button vote good\" ")
                        .append(getHtmlout(m_IsWebviewAllowJavascriptInterface, "postVoteGood", post.id))
                        .append("><span>Хорошо</span></a>")
            }
            if (post.canEdit) sb.append(String.format("<a class=\"button edit\" id=\"edit-but-%s\" href=\"/forum/index.php?act=post&do=edit_post&f=%s&t=%s&p=%s&st=%s\"><span>Изменить</span></a>",
                    post.id, topic!!.forumId, topic!!.id, post.id, postNumber))
            if (post.canDelete) sb.append(String.format("<a class=\"button delete\" href=\"/forum/index.php?act=Mod&CODE=04&f=%s&t=%s&p=%s&st=%s&auth_key=%s\"><span>Удалить</span></a>",
                    topic!!.forumId, topic!!.id, post.id, postNumber, topic!!.authKey))
        }
        sb.append("</div>\n\n")
    }

    companion object {
        const val NICK_SNAPBACK_TEMPLATE = "[SNAPBACK]%s[/SNAPBACK] [B]%s,[/B] \n"

        @JvmStatic
        fun addButtons(sb: StringBuilder, currentPage: Int, pagesCount: Int, isUseJs: Boolean,
                       useSelectTextAsNumbers: Boolean, top: Boolean) {
            val prevDisabled = currentPage == 1
            val nextDisabled = currentPage == pagesCount
            sb.append("\n<div class=\"navi ").append(if (top) "top" else "bottom").append("\">\n")
            sb.append("<a class=\"button first").append(if (prevDisabled) " disable\"" else "\"" + getHtmlout(isUseJs, "firstPage")).append("><span>&lt;&lt;</span></a>\n")
            sb.append("<a class=\"button prev").append(if (prevDisabled) " disable\"" else "\"" + getHtmlout(isUseJs, "prevPage")).append("><span>&lt;</span></a>\n")
            sb.append("<a class=\"button page\" ").append(getHtmlout(isUseJs, "jumpToPage")).append("><span>").append(if (useSelectTextAsNumbers) "$currentPage/$pagesCount" else "Выбор").append("</span></a>\n")
            sb.append("<a class=\"button next").append(if (nextDisabled) " disable\"" else "\"" + getHtmlout(isUseJs, "nextPage")).append("><span>&gt;</span></a>\n")
            sb.append("<a class=\"button last").append(if (nextDisabled) " disable\"" else "\"" + getHtmlout(isUseJs, "lastPage")).append("><span>&gt;&gt;</span></a>\n")
            sb.append("</div>\n")
        }

        @JvmStatic
        fun getHtmlout(webViewAllowJs: Boolean, methodName: String, val1: String?, val2: String?): String? {
            return getHtmlout(webViewAllowJs, methodName, arrayOf(val1, val2))
        }

        private fun getHtmlout(webViewAllowJs: Boolean, methodName: String, val1: String): String? {
            return getHtmlout(webViewAllowJs, methodName, arrayOf(val1))
        }

        private fun getHtmlout(webViewAllowJs: Boolean, methodName: String): String? {
            return getHtmlout(webViewAllowJs, methodName, arrayOfNulls(0))
        }

        private fun getHtmlout(webViewAllowJs: Boolean, methodName: String, paramValues: Array<String?>): String? {
            return getHtmlout(webViewAllowJs, methodName, paramValues, true)
        }

        @JvmStatic
        fun getHtmlout(@Suppress("UNUSED_PARAMETER") webViewAllowJs: Boolean?, methodName: String?, paramValues: Array<String?>?, modifyParams: Boolean?): String? {
            return HtmlOutUtils.getHtmlout(methodName, paramValues!!, modifyParams!!)
        }
    }

    init {
        m_HtmlPreferences.load(context)
        m_EmoticsDict = Smiles.getSmilesDict()
        m_IsWebviewAllowJavascriptInterface = isWebviewAllowJavascriptInterface
        m_Logined = logined
        m_UrlParams = urlParams
        this.topic = topic
        m_IsLoadImages = WebViewExternals.isLoadImages("theme")
        m_IsShowAvatars = Preferences.Topic.isShowAvatars
    }
}