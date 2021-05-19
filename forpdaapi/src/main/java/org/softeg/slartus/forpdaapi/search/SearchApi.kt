package org.softeg.slartus.forpdaapi.search

/*
 * Created by slinkin on 29.04.2014.
 */

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.softeg.slartus.forpdaapi.IHttpClient
import org.softeg.slartus.forpdaapi.ListInfo
import org.softeg.slartus.forpdaapi.Topic
import org.softeg.slartus.forpdaapi.common.ParseFunctions
import org.softeg.slartus.forpdacommon.Functions
import java.io.IOException
import java.net.MalformedURLException
import java.util.*
import java.util.regex.Pattern

object SearchApi {
    /**
     * @param searchUrl - номер страницы из
     * @throws MalformedURLException
     */
    @Throws(IOException::class)
    fun getSearchTopicsResult(client: IHttpClient, searchUrl: String, listInfo: ListInfo): ArrayList<Topic> {
        var st = 0
        val m = Pattern.compile("st=(\\d+)", Pattern.CASE_INSENSITIVE).matcher(searchUrl)
        if (m.find())
            st = Integer.parseInt(m.group(1))
        st += listInfo.from

        val body = client.performGetFullVersion(searchUrl.replace("st=\\d+".toRegex(), "") + "&st=" + st)


        return parse(body.responseBody, listInfo)
    }

    fun parse(body: String, listInfo: ListInfo): ArrayList<Topic> {
        val res = ArrayList<Topic>()
        val today = Functions.getToday()
        val yesterday = Functions.getYesterToday()
        val idPattern = Pattern.compile("showtopic=(\\d+)", Pattern.CASE_INSENSITIVE)
        val forumIdPattern = Pattern.compile("showforum=(\\d+)", Pattern.CASE_INSENSITIVE)

        val doc = Jsoup.parse(ParseFunctions.decodeEmails(body))
        val trElements = doc.select("table:has(th:contains(Название темы)) tr:has(td)")
        var sortOrder = 1000 + listInfo.from + 1
        for (trElement in trElements) {
            if (trElement.children().size < 3)
                continue

            val tdElement = trElement.child(2)
            var el: Element? = tdElement.select("a").last() ?: continue
            var m = idPattern.matcher(el!!.attr("href"))
            if (!m.find())
                continue
            val theme = Topic(m.group(1), el.text())
            el = tdElement.select("span.desc").first()
            theme.description = el?.text() ?: ""
            theme.isNew = tdElement.select("a[href*=view=getnewpost]").first() != null

            theme.sortOrder = (sortOrder++).toString()

            el = trElement.select("span.forumdesc>a").first()
            if (el != null) {
                m = forumIdPattern.matcher(el.attr("href"))
                if (m.find())
                    theme.forumId = m.group(1)
                theme.forumTitle = el.text()
            }
            el = trElement.select("td:has(a[href*=getlastpost])").first()
            if (el != null) {
                theme.lastMessageDate = Functions.parseForumDateTime(el.text(), today, yesterday)
            }
            el = trElement.select("td:has(a[href*=showuser=])").first()
            if (el != null) {
                theme.lastMessageAuthor = el.text()
            }
            res.add(theme)
        }

        val el = doc.select("span.pagelinklast>a").first()
        if (el != null) {
            val pagesCountPattern = Pattern.compile("st=(\\d+)", Pattern.CASE_INSENSITIVE)
            val matcher = pagesCountPattern.matcher(el.attr("href"))
            if (matcher.find()) {
                listInfo.outCount = ((matcher.group(1).toIntOrNull()
                        ?: 0) + 1).coerceAtLeast(listInfo.outCount)
            }
        }
        return res
    }

}
