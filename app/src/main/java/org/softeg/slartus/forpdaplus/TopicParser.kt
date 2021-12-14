@file:Suppress("RegExpRedundantEscape")

package org.softeg.slartus.forpdaplus

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.softeg.slartus.forpdaapi.common.ParseFunctions
import org.softeg.slartus.forpdacommon.CollectionUtils
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.PatternExtensions
import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdaplus.classes.Post
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder
import org.softeg.slartus.forpdaplus.classes.common.Functions
import org.softeg.slartus.forpdaplus.fragments.topic.ForPdaWebInterface
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl.Companion.instance
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

object TopicParser {
    private val beforePostsPattern = PatternExtensions.compile("^([\\s\\S]*?)<div data-post")
    private val pollFormPattern = Pattern.compile(
        "<form[^>]*action=\"[^\"]*addpoll=1[^\"]*\"[^>]*>([\\s\\S]*?)</form>",
        Pattern.CASE_INSENSITIVE
    )
    private val pollTitlePattern = Pattern.compile("<b>(.*?)</b>")
    private val pollQuestionsPattern =
        Pattern.compile("strong>(.*?)</strong[\\s\\S]*?table[^>]*?>([\\s\\S]*?)</table>")
    private val pollVotedPattern = Pattern.compile("(<input[^>]*?>)&nbsp;<b>([^>]*)</b>")
    private val pollNotVotedPattern =
        Pattern.compile("<td[^>]*>([^<]*?)</td><td[^\\[]*\\[ <b>(.*?)</b>[^\\[]*\\[([^\\]]*)")
    private val pollBottomPattern =
        Pattern.compile("<td class=\"row1\" colspan=\"3\" align=\"center\"><b>([^<]*?)</b>[\\s\\S]*?class=\"formbuttonrow\">([\\s\\S]*?)</td")


    @JvmStatic
    @Throws(IOException::class)
    fun loadTopic(
        context: Context,
        id: String?, topicBody: String, spoilFirstPost: Boolean,
        logined: Boolean, urlParams: String?
    ): TopicBodyBuilder {
        var topicBody = ParseFunctions.decodeEmails(topicBody)
        val mainMatcher = beforePostsPattern.matcher(topicBody)
        if (!mainMatcher.find()) {
            var errorMatcher = Pattern.compile(
                "<div class=\"wr va-m text\">([\\s\\S]*?)</div>",
                Pattern.CASE_INSENSITIVE
            )
                .matcher(topicBody)
            if (errorMatcher.find()) {
                throw NotReportException(errorMatcher.group(1))
            }
            val errorPattern =
                PatternExtensions.compile("<div class=\"errorwrap\">([\\s\\S]*?)</div>")
            errorMatcher = errorPattern.matcher(topicBody)
            if (errorMatcher.find()) {
                val errorReasonPattern = PatternExtensions.compile("<p>(.*?)</p>")
                val errorReasonMatcher = errorReasonPattern.matcher(errorMatcher.group(1) ?: "")
                if (errorReasonMatcher.find()) {
                    throw NotReportException(errorReasonMatcher.group(1))
                }
            }
            if (TextUtils.isEmpty(topicBody)) throw NotReportException(context.getString(R.string.server_return_empty_page))
            if (topicBody.startsWith("<h1>")) throw NotReportException(
                context.getString(R.string.site_response) + topicBody.fromHtml()
            )
            throw IOException(context.getString(R.string.error_parsing_page) + " id=" + id)
        }

        val topic = Client.createTopic(id, mainMatcher.group(1))
        topicBody = topicBody.replace("^[\\s\\S]*?<div data-post", "<div data-post")
            .replace("<div class=\"topic_foot_nav\">[\\s\\S]*", "<div class=\"topic_foot_nav\">")
        val topicBodyBuilder = TopicBodyBuilder(
            context, logined, topic, urlParams,
            Functions.isWebviewAllowJavascriptInterface()
        )

        topicBodyBuilder.beginTopic()

        //<<опрос
        parsePoll(topicBodyBuilder, mainMatcher.group(1) ?: "", logined, urlParams)

        parsePosts(topicBodyBuilder, topicBody, spoilFirstPost)
        topicBodyBuilder.endTopic()
        return topicBodyBuilder
    }

    private fun parsePoll(
        topicBodyBuilder: TopicBodyBuilder,
        body: String,
        logined: Boolean,
        urlParams: String?
    ) {
        // TODO!: переделать на jsoup
        var pollMatcher = pollFormPattern.matcher(body)
        if (pollMatcher.find()) {
            val pollSource = pollMatcher.group(1) ?: ""
            val pollBuilder = StringBuilder()
            var percent: String
            var temp: Matcher
            pollBuilder.append("<form action=\"modules.php\" method=\"get\">")
            pollMatcher = pollTitlePattern.matcher(pollSource)
            if (pollMatcher.find()) {
                if (pollMatcher.group(1) != "-") pollBuilder.append("<div class=\"poll_title\"><span>")
                    .append(pollMatcher.group(1)).append("</span></div>")
            }
            pollBuilder.append("<div class=\"poll_body\">")
            var voted = false
            pollMatcher = pollQuestionsPattern.matcher(pollSource)
            while (pollMatcher.find()) {
                if (pollMatcher.group(2)?.contains("input") != true) voted = true
                pollBuilder.append("<div class=\"poll_theme\">")
                pollBuilder.append("<div class=\"theme_title\"><span>").append(pollMatcher.group(1))
                    .append("</span></div>")
                pollBuilder.append("<div class=\"items").append(if (voted) " voted" else "")
                    .append("\">")
                if (voted) {
                    temp = pollNotVotedPattern.matcher(pollMatcher.group(2) ?: "")
                    while (temp.find()) {
                        pollBuilder.append("<div class=\"item\">")
                        pollBuilder.append("<span class=\"name\"><span>").append(temp.group(1))
                            .append("</span></span>")
                        pollBuilder.append("<span class=\"num_votes\"><span>").append(temp.group(2))
                            .append("</span></span>")
                        pollBuilder.append("<div class=\"range\">")
                        percent = temp.group(3)?.replace(",", ".") ?: ""
                        pollBuilder.append("<div class=\"range_bar\" style=\"width:")
                            .append(percent).append(";\"></div>")
                        pollBuilder.append("<span class=\"value\"><span>").append(percent)
                            .append("</span></span>")
                        pollBuilder.append("</div>")
                        pollBuilder.append("</div>")
                    }
                } else {
                    temp = pollVotedPattern.matcher(pollMatcher.group(2) ?: "")
                    while (temp.find()) {
                        pollBuilder.append("<label class=\"item\">")
                        pollBuilder.append(temp.group(1))
                        pollBuilder.append("<span class=\"icon\"></span>")
                        pollBuilder.append("<span class=\"item_body\"><span class=\"name\">")
                            .append(temp.group(2)).append("</span></span>")
                        pollBuilder.append("</label>")
                    }
                }
                pollBuilder.append("</div>")
                pollBuilder.append("</div>")
            }
            pollBuilder.append("</div>")
            pollMatcher = pollBottomPattern.matcher(pollSource)
            if (pollMatcher.find()) {
                pollBuilder.append("<div class=\"votes_info\"><span>").append(pollMatcher.group(1))
                    .append("</span></div>")
                if (logined) {
                    pollBuilder.append("<div class=\"buttons\">").append(pollMatcher.group(2))
                        .append("</div>")
                }
            }
            pollBuilder.append("<input type=\"hidden\" name=\"addpoll\" value=\"1\" /></form>")
            topicBodyBuilder.addPoll(
                pollBuilder.toString()
                    .replace("go_gadget_show()", ForPdaWebInterface.NAME + ".go_gadget_show()")
                    .replace("go_gadget_vote()", ForPdaWebInterface.NAME + ".go_gadget_vote()"),
                urlParams != null && urlParams.contains("poll_open=true")
            )
        }
    }

    private fun parsePosts(
        topicBodyBuilder: TopicBodyBuilder, topicBody: String,
        spoilFirstPost: Boolean
    ) {
        val doc = Jsoup.parse(topicBody)
        topicBodyBuilder.openPostsList()
        var post: Post
        var spoil = spoilFirstPost
        for (postEl in doc.select("div[data-post]")) {
            // id поста-обязателен
            val postId = postEl.attr("data-post")
            var postDate = ""
            var postNumber = ""
            val postHeaderEl = postEl.selectFirst("div.post_header") ?: continue
            // дата и номер поста не обязательны
            var el = postHeaderEl.selectFirst("span.post_date")
            if (el != null) {
                try {
                    val dateChildIndex =
                        el.childNodes()
                            .indexOfFirst { c -> c is TextNode && c.text().contains("|") }
                    postDate = (el.childNode(dateChildIndex) as TextNode).text().replace("|", "")
                        .trim { it <= ' ' }
                    postNumber =
                        (el.childNode(dateChildIndex + 1) as Element).text().replace("#", "")
                            .trim { it <= ' ' }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
            // тело поста-обязательно
            el = postEl.selectFirst("div.post_body")
            if (el == null) continue
            val postBody = "<div class=\"" + CollectionUtils.join(
                " ",
                el.classNames()
            ) + "\">" + el.html() + "</div>"
            post = Post(postId, postDate, postNumber)
            post.body = postBody

            // всё остальное не обязательно - читать темы можно по крайней мере

            // пользователь
            val user = parsePostNick(postHeaderEl)
            post.userId = user.id
            post.setAuthor(user.nick)
            post.setUserState(user.state)
            post.avatarFileName = user.avatar

            // информация о пользователе
            el = postHeaderEl.selectFirst("span.post_user_info")
            if (el != null) {
                val el1 = el.selectFirst("span>span") // группа пользователя
                if (el1 != null) post.userGroup = el1.outerHtml()
                if (el.selectFirst("strong[class=t-cur-title]") != null) // куратор
                    post.setCurator()
            }

            // репутация
            el = postHeaderEl.selectFirst("a[href*=act=rep&view=history]")
            if (el != null) post.userReputation = el.text()
            post.canPlusRep = postHeaderEl.selectFirst("a[href*=act=rep&view=win_add]") != null
            post.canMinusRep = postHeaderEl.selectFirst("a[href*=act=rep&view=win_minus]") != null

            // операции над постом
            el = postHeaderEl.selectFirst("span.post_action")
            if (el != null) {
                post.canEdit = el.selectFirst("a[href*=do=edit_post]") != null
                post.canDelete = el.selectFirst("a[href*=tact=delete]") != null
                if (post.canDelete) {
                    // если автор поста не совпадает с текущим пользователем и есть возможность удалить-значит, модератор
                    if (post.userId != null && post.userId != instance.getId()) {
                        topicBodyBuilder.isMMod = true
                    }
                }
            }
            topicBodyBuilder.addPost(post, spoil)
            spoil = false
        }
    }

    fun parsePostNick(postHeaderEl: Element): User {
        val result = User()
        val el = postHeaderEl.selectFirst("span.post_nick")
        if (el != null) {
            // статус автора
            var el1 = el.selectFirst("font")
            if (el1 != null) result.state = el1.attr("color")
            // ник и аватар автора
            el1 = el.selectFirst("a[data-av]")
            if (el1 != null) {
                result.avatar = el1.attr("data-av") // аватар
                val textNodes = el1.textNodes()
                val nick = if (textNodes.size > 0) el1.textNodes()[0].toString() // ник
                else if (
                    el1.childrenSize() > 0
                    && el1.child(0) is Element
                    && el1.child(0).classNames().contains("__cf_email__")
                ) {
                    try {
                        ParseFunctions.cfDecodeEmail(el1.child(0).attr("data-cfemail"))
                    } catch (ex: Throwable) {
                        el1.child(0).textNodes()[0].toString()
                    }
                } else el1.html() // ник
                result.nick = nick
            }
            el1 = el.selectFirst("a[href*=showuser]")
            if (el1 != null) {
                result.id = Uri.parse(el1.attr("href"))?.getQueryParameter("showuser")
                if (TextUtils.isEmpty(result.nick)) result.nick = el1.html()
                if (TextUtils.isEmpty(result.avatar)) result.avatar =
                    el1.attr("data-av")
            }
        }
        return result
    }


    class User {
        var id: String? = null
        var avatar: String? = null
        var nick: String? = null
        var state: String? = null
    }
}