package org.softeg.slartus.forpdaapi.qms

import androidx.core.util.Pair
import org.softeg.slartus.forpdaapi.IHttpClient
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdacommon.*
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.CountingFileRequestBody
import ru.slartus.http.Http
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by slartus on 02.03.14.
 */
object QmsApi {

    val qmsSubscribers: ArrayList<QmsUser>
        get() {
            val pageBody =
                Http.instance.performGet("https://${HostHelper.host}/forum/index.php?&act=qms-xhr&action=userlist").responseBody
            return parseQmsUsers(pageBody)
        }

    @Throws(Throwable::class)
    fun getChatPage(httpClient: IHttpClient, mid: String, themeId: String): String {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["xhr"] = "body"
        return httpClient.performPost(
            "https://${HostHelper.host}/forum/index.php?act=qms&mid=$mid&t=$themeId",
            additionalHeaders
        ).responseBody
    }

    @Throws(Exception::class)
    private fun checkChatError(pageBody: String) {

        val m = Pattern.compile("<div class=\"error\">([\\s\\S]*?)</div>").matcher(pageBody)
        if (m.find()) {
            throw Exception(m.group(1).fromHtml().toString())
        }

    }

    @Throws(Throwable::class)
    fun getChat(httpClient: IHttpClient, mid: String, themeId: String, daysCount: Int?): QmsPage {
        val pageBody = getChatPage(httpClient, mid, themeId)
        checkChatError(pageBody)

        val qmsPage = QmsPage()
        val m =
            Pattern.compile("<[^>]*?\"navbar-title\"[^>]*?>[\\s\\S]*?<a[^>]*?showuser=(\\d+)[^>]*?>(.*?)<\\/a>:<\\/b>\\s*([\\s\\S]*?)\\s*<\\/span>")
                .matcher(pageBody)
        if (m.find()) {
            qmsPage.userId = m.group(1)
            qmsPage.userNick = m.group(2).fromHtml()
            qmsPage.title = m.group(3).fromHtml()
        }

        qmsPage.body = matchChatBody(pageBody, daysCount)
        return qmsPage
    }

    private fun matchChatBody(pageBod: String, daysCount: Int?): String {
        var pageBody = pageBod
        var chatInfo = ""
        var m =
            Pattern.compile("<span class=\"nav-text\"[\\s\\S]*?<a href=\"[^\"]*showuser[^>]*>([^>]*?)</a>:</b>([^<]*)")
                .matcher(pageBody)
        if (m.find())
            chatInfo = "<span id=\"chatInfo\" style=\"display:none;\">" + m.group(1)
                .trim { it <= ' ' } + "|:|" + m.group(2).trim { it <= ' ' } + "</span>"

        if (daysCount != null) {
            val datesMatcher =
                Pattern.compile("(<div class=\"date\">[\\s\\S]*?(?=(?:<div class=\"date\">|<div class=\"form-thread)))")
                    .matcher(pageBody)
            var days = emptyArray<String>()
            while (datesMatcher.find()) {
                val dayBody = datesMatcher.group(1)
                days = days.plus(dayBody)
            }
            if (days.isNotEmpty() || daysCount == 0) {
                if (days.size > daysCount) {
                    chatInfo += "<div class=\"panel\"><div class=\"navi\"><a id=\"chat_more_button\" class=\"button page\" ${
                        HtmlOutUtils.getHtmlout(
                            "loadMore"
                        )
                    } >Загрузить ещё (${daysCount}/${days.size}дн.)</a></div></div>"
                }
                return chatInfo + "<div id=\"thread_form\"><div id=\"thread-inside-top\"></div>" + days.takeLast(
                    daysCount
                ).joinToString(separator = "\n") + "</div>"
            }
        }
        m =
            Pattern.compile("<div id=\"thread-inside-top\"><\\/div>([\\s\\S]*)<div id=\"thread-inside-bottom\">")
                .matcher(pageBody)
        if (m.find())
            return chatInfo + "<div id=\"thread_form\"><div id=\"thread-inside-top\"></div>" + m.group(
                1
            ) + "</div>"

        m = Pattern.compile("<div class=\"list_item\" t_id=([\\s\\S]*?)</form>").matcher(pageBody)
        if (m.find())
            return chatInfo + "<div id=\"thread_form\"><div class=\"list_item\" t_id=" + m.group(1) + "</div>"

        // ни одного сообщения
        m = Pattern.compile("</form>\\s*<div class=\"form\">").matcher(pageBody)
        if (m.find())
            return "<div id=\"thread_form\"></div>"
        m =
            Pattern.compile("<script>try\\{setTimeout \\( function\\(\\)\\{ updateScrollbar \\( \\$\\(\"#thread_container>.scrollbar_wrapper\"\\), \"bottom\" \\); \\}, 1 \\);\\}catch\\(e\\)\\{\\}</script>\\s*</div>")
                .matcher(pageBody)
        if (m.find())
            return "<div id=\"thread_form\"></div>"
        else
            pageBody += ""
        return pageBody
    }

    @Throws(Throwable::class)
    fun sendMessage(
        httpClient: IHttpClient, mid: String, tid: String, message: String, encoding: String,
        attachs: ArrayList<EditAttach>, daysCount: Int?
    ): QmsPage {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["action"] = "send-message"
        additionalHeaders["mid"] = mid
        additionalHeaders["t"] = tid
        additionalHeaders["message"] = message
        if (attachs.any())
            additionalHeaders["attaches"] = attachs.joinToString { it.id }
        val response = httpClient.performPost(
            "https://${HostHelper.host}/forum/index.php?act=qms-xhr",
            additionalHeaders, encoding
        )
        parseError(response.responseBody)
        return getChat(httpClient, mid, tid, daysCount)
    }

    @Throws(IOException::class)
    fun createThread(
        httpClient: IHttpClient, userID: String, userNick: String, title: String, message: String,
        outParams: MutableMap<String, String>, encoding: String
    ): String {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["action"] = "create-thread"
        additionalHeaders["username"] = userNick
        additionalHeaders["title"] = title
        additionalHeaders["message"] = message
        val pageBody = httpClient.performPost(
            "https://${HostHelper.host}/forum/index.php?act=qms&mid=$userID&xhr=body&do=1",
            additionalHeaders,
            encoding
        )

        var m =
            Pattern.compile("<input\\s*type=\"hidden\"\\s*name=\"mid\"\\s*value=\"(\\d+)\"\\s*/>")
                .matcher(pageBody.responseBody)
        if (m.find())
            outParams["mid"] = m.group(1)
        m = Pattern.compile("<input\\s*type=\"hidden\"\\s*name=\"t\"\\s*value=\"(\\d+)\"\\s*/>")
            .matcher(pageBody.responseBody)
        if (m.find())
            outParams["t"] = m.group(1)
        //  m = Pattern.compile("<strong>(.*?):\\s*</strong></a>\\s*(.*?)\\s*?</span>").matcher(pageBody);
        //if (m.find()) {
        outParams["user"] = userNick
        outParams["title"] = title
        //}

        parseError(pageBody.responseBody)

        return matchChatBody(pageBody.responseBody, 0)
    }

    private fun parseError(pageBody: String) {
        val errorRegexes = listOf(
            "<div class=\"form-error\">(.*?)</div>",
            "<div class=\"list-group-item msgbox error\">([\\s\\S]*?)</div>"
        )
        errorRegexes.forEach { r ->
            val m = Pattern.compile(r)
                .matcher(pageBody)
            if (m.find())
                throw NotReportException(m.group(1)?.fromHtml()?.toString()?.trim())
        }
    }

    @Throws(IOException::class)
    fun deleteDialogs(httpClient: IHttpClient, mid: String, ids: List<String>) {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["action"] = "delete-threads"
        additionalHeaders["title"] = ""
        additionalHeaders["message"] = ""
        for (id in ids) {
            additionalHeaders["thread-id[$id]"] = id
        }
        httpClient.performPost(
            "https://${HostHelper.host}/forum/index.php?act=qms&xhr=body&do=1&mid=$mid",
            additionalHeaders
        )
    }

    @Throws(IOException::class)
    fun deleteMessages(
        httpClient: IHttpClient, mid: String, threadId: String, ids: List<String>,
        encoding: String, daysCount: Int?
    ): String {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["act"] = "qms"
        additionalHeaders["mid"] = mid
        additionalHeaders["t"] = threadId
        additionalHeaders["xhr"] = "body"
        additionalHeaders["do"] = "1"
        additionalHeaders["action"] = "delete-messages"
        additionalHeaders["forward-messages-username"] = ""
        additionalHeaders["forward-thread-username"] = ""
        additionalHeaders["message"] = ""
        for (id in ids) {
            additionalHeaders["message-id[$id]"] = id
        }

        return matchChatBody(
            httpClient.performPost(
                "https://${HostHelper.host}/forum/index.php?act=qms&mid$mid&t=$threadId&xhr=body&do=1",
                additionalHeaders, encoding
            ).responseBody, daysCount
        )
    }

    private fun parseQmsUsers(pageBody: String?): ArrayList<QmsUser> {
        val res = ArrayList<QmsUser>()
        val m = Pattern.compile(
            "<a class=\"list-group-item[^>]*=(\\d*)\">[^<]*<div class=\"bage\">([^<]*)[\\s\\S]*?src=\"([^\"]*)\" title=\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE
        ).matcher(pageBody!!)
        var count: String
        var qmsUser: QmsUser
        while (m.find()) {
            qmsUser = QmsUser()
            qmsUser.setId(m.group(1))
            var avatar = m.group(3)
            if (avatar.substring(0, 2) == "//") {
                avatar = "https:$avatar"
            }
            qmsUser.setAvatarUrl()
            qmsUser.nick = m.group(4).fromHtml().toString().trim { it <= ' ' }
            count = m.group(2).trim { it <= ' ' }
            if (count != "")
                qmsUser.newMessagesCount = count.replace("(", "").replace(")", "")

            res.add(qmsUser)
        }
        return res
    }

    @Throws(Throwable::class)
    fun getQmsUserThemes(
        mid: String,
        outUsers: ArrayList<QmsUser>, parseNick: Boolean?
    ): QmsUserThemes {
        val res = QmsUserThemes()
        val pageBody =
            Http.instance.performGet("https://${HostHelper.host}/forum/index.php?act=qms&mid=$mid").responseBody
        val newCountPattern = Pattern.compile("([\\s\\S]*?)\\((\\d+)\\s*\\/\\s*(\\d+)\\)\\s*$")
        val countPattern = Pattern.compile("([\\s\\S]*?)\\((\\d+)\\)\\s*$")
        val strongPattern = Pattern.compile("<strong>([\\s\\S]*?)</strong>")
        var matcher =
            Pattern.compile("<div class=\"list-group\">([\\s\\S]*)<form [^>]*>([\\s\\S]*?)<\\/form>")
                .matcher(pageBody)
        if (matcher.find()) {
            outUsers.addAll(parseQmsUsers(matcher.group(1)))
            matcher =
                Pattern.compile("<a class=\"list-group-item[^>]*-(\\d*)\">[\\s\\S]*?<div[^>]*>([\\s\\S]*?)<\\/div>([\\s\\S]*?)<\\/a>")
                    .matcher(matcher.group(2))
            var item: QmsUserTheme
            var m: Matcher
            var info: String
            while (matcher.find()) {
                item = QmsUserTheme()
                item.Id = matcher.group(1)
                item.Date = matcher.group(2)

                info = matcher.group(3)
                m = strongPattern.matcher(info)
                if (m.find()) {
                    m = newCountPattern.matcher(m.group(1))
                    if (m.find()) {
                        item.Title = m.group(1).trim { it <= ' ' }
                        item.Count = m.group(2)
                        item.NewCount = m.group(3)
                    } else
                        item.Title = m.group(2).trim { it <= ' ' }
                } else {
                    m = countPattern.matcher(info)
                    if (m.find()) {
                        item.Title = m.group(1).trim { it <= ' ' }
                        item.Count = m.group(2).trim { it <= ' ' }
                    } else
                        item.Title = info.trim { it <= ' ' }
                }
                res.add(item)
            }
            if (parseNick!!) {
                matcher =
                    Pattern.compile("<div class=\"nav\">[\\s\\S]*?showuser[^>]*>([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/div>")
                        .matcher(pageBody)
                if (matcher.find()) {
                    res.Nick = matcher.group(1)
                }
            }
        }


        return res
    }

    fun getNewQmsCount(pageBody: String): Int {
        val qms20Pattern = PatternExtensions.compile("id=\"events-count\"[^>]*>[^\\d]*?(\\d+)<")
        val m = qms20Pattern.matcher(pageBody)
        return if (m.find()) {
            Integer.parseInt(m.group(1))
        } else 0
    }

    @Throws(IOException::class)
    fun getNewQmsCount(client: IHttpClient): Int {
        val body = client.performGet("https://${HostHelper.host}/forum/index.php?showforum=200")
        return getNewQmsCount(body.responseBody)
    }

    private fun fromCharCode(vararg codePoints: Int): String {
        val builder = StringBuilder(codePoints.size)
        for (codePoint in codePoints) {
            builder.append(Character.toChars(codePoint))
        }
        return builder.toString()
    }

    fun attachFile(
        pathToFile: String,
        progress: ProgressState,
        code: String = "check"
    ): EditAttach {
        var nameValue = "file"
        try {
            nameValue = FileUtils.getFileNameFromUrl(pathToFile)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val finalNameValue = nameValue
        val params = ArrayList<Pair<String, String>>()
        params.add(Pair("name", nameValue))
        params.add(Pair("code", code))
        params.add(Pair("relType", "MSG"))
        params.add(Pair("index", "1"))
        val (_, _, responseBody) = Http.instance.uploadFile("https://${HostHelper.host}/forum/index.php?act=attach",
            nameValue,
            pathToFile,
            "FILE_UPLOAD",
            params,
            CountingFileRequestBody.ProgressListener { num ->
                progress.update(
                    finalNameValue,
                    num
                )
            })

        val body = ("" + responseBody).replace("(^\\x03|\\x03$)".toRegex(), "")

        val parts =
            body.split(fromCharCode(2).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val k = parts[0].toInt()

        if (k == 0) {
            Thread.sleep(1000)
            return attachFile(pathToFile, progress, "upload")
        }
        val error = when (k) {
            -1 -> " no access on server."
            -2 -> " too big."
            -3 -> " has invalid mime type"
            -4 -> " is banned on server."
            else -> null
        }
        if (error != null)
            throw NotReportException(error)

        val id = parts[0]
        val name = parts[1]
        val ext = parts[2]
//        val url = parts[3]
//        val length = parts[4]
//        val md5 = parts[5]

        return EditAttach(id, "$name.$ext")
    }

    fun deleteAttach(attachId: String): Boolean {

        val params = ArrayList<Pair<String, String>>()

        params.add(Pair("code", "remove"))
        params.add(Pair("relType", "MSG"))
        params.add(Pair("relId", "0"))
        params.add(Pair("index", "1"))
        params.add(Pair("id", attachId))
        Http.instance.performPost("https://${HostHelper.host}/forum/index.php?act=attach", params)

        return true
    }
}
