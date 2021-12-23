package org.softeg.slartus.forpdaapi

import android.content.SharedPreferences
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import org.jsoup.Jsoup
import org.softeg.slartus.forpdacommon.DateTimeExternals
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.PatternExtensions
import org.softeg.slartus.forpdacommon.UrlExtensions
import org.softeg.slartus.hosthelper.HostHelper
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.*
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.ceil

/*
* Created by slinkin on 20.02.14.
*/
object NewsApi {
    @JvmStatic

    fun like(httpClient: IHttpClient, newsId: String): Boolean {
        val res = httpClient.performGet("https://${HostHelper.host}/wp-content/plugins/karma/ajax.php?p=$newsId&c=0&v=1", false, false).responseBody
        return res != null
    }

    @JvmStatic

    fun likeComment(httpClient: IHttpClient, newsId: String, postId: String): Boolean {
        val res = httpClient.performGet("https://${HostHelper.host}/wp-content/plugins/karma/ajax.php?p=$newsId&c=$postId&v=1", false, false).responseBody
        return res != null
    }

    @JvmStatic
    fun parseNewsBody(newsPageBody: String): String {
        var newsPageBody = newsPageBody
        val doc = Jsoup.parse(newsPageBody, "https://${HostHelper.host}")
        val bodyElement = doc.selectFirst("div.article:has(div.article-header)")
                ?: doc.selectFirst("div.container:has(meta[itemprop=datePublished])")
                ?: doc.selectFirst("div.container")
        if (bodyElement != null) {
            newsPageBody = bodyElement.parent()?.html() ?: ""
        } else {
            var m = PatternExtensions.compile("<article[^>]*>([\\s\\S]*?)</article>").matcher(newsPageBody)
            if (m.find()) newsPageBody = m.group(1) else {
                m = PatternExtensions.compile("<body[^>]*>([\\s\\S]*?)</body>").matcher(newsPageBody)
                newsPageBody = if (m.find()) m.group(1) else {
                    newsPageBody.replace("[\\s\\S]*?<body[^>]*>".toRegex(), "<body><div id=\"main\">")
                }
            }
        }

        val commentsBody = doc
                .selectFirst("div.comment-box")
                ?.selectFirst("ul.comment-list")
                ?.outerHtml()
        if (!commentsBody.isNullOrEmpty()) {
            newsPageBody += "<div class=\"comment-box\">${commentsBody}</div>"
        }

        return newsPageBody
    }

    private fun msg(message: String) {
        Log.e("TEST", message)
    }

    @JvmStatic

    fun getNews(httpClient: IHttpClient, url: String, listInfo: ListInfo, preferences: SharedPreferences): ArrayList<News> {
        //https://4pda.ru/2013/page/7/
        //https://4pda.ru/2013/2/page/7/
        //https://4pda.ru/2013/2/2/page/7/
        //https://4pda.ru/tag/programs-for-ios/page/3
        //https://4pda.ru/page/5/
        //https://4pda.ru/page/5/?s=ios - поиск
        //https://4pda.ru/?s=%EF%EB%E0%ED%F8%E5%F2
        //https://4pda.ru/page/6/?s=%EF%EB%E0%ED%F8%E5%F2
        val NEWS_PER_PAGE = 28 // 30 новостей на страницу выводит форум
        var pageNum = 1
        var justUrl = url // урл без страницы и параметров
        var params = "" // параметры, например, s=%EF%EB%E0%ED%F8%E5%F2
        // сначала проверим на поисковой урл
        var m = Pattern.compile("(.*?)(?:page/+(\\d+)/+)?\\?(.*?)$", Pattern.CASE_INSENSITIVE)
                .matcher(url)
        if (m.find()) {
            justUrl = m.group(1)
            if (!TextUtils.isEmpty(m.group(2))) pageNum = m.group(2).toInt()
            if (!TextUtils.isEmpty(m.group(3))) params = m.group(3)
        } else {
            m = Pattern.compile("(.*?)(?:page/+(\\d+)/+)?$", Pattern.CASE_INSENSITIVE)
                    .matcher(url)
            if (m.find()) {
                justUrl = m.group(1)
                if (!TextUtils.isEmpty(m.group(2))) pageNum = m.group(2).toInt()
            }
        }
        val page: Int
        page = if (listInfo.from == 0) {
            NEWS_PER_PAGE
        } else {
            preferences.getInt("lm", NEWS_PER_PAGE)
        }
        pageNum += ceil((listInfo.from / page).toDouble()).toInt()
        val requestUrl = "$justUrl/page/$pageNum/$params"
        val res = ArrayList<News>()
        val dailyNewsPage = parseNewsListPage(httpClient, requestUrl, res)

        if (res.size == 0 && pageNum == 1 && listInfo.from == 0) return getNewsFromRss(httpClient, UrlExtensions.removeDoubleSplitters("$url/feed/"))
        val lastPageNum = lastPageNum(dailyNewsPage)
        listInfo.outCount = res.size * lastPageNum
        if (listInfo.from == 0 && res.size > 0) {
            preferences.edit().putInt("lm", res.size).apply()
        }
        return res
    }

    fun parseNewsListPage(httpClient: IHttpClient, requestUrl: String, res: ArrayList<News>): String {
        val dailyNewsPage = httpClient.performGet(UrlExtensions.removeDoubleSplitters(requestUrl)).responseBody
        val doc = Jsoup.parse(dailyNewsPage, "https://${HostHelper.host}")
        val articleElements = doc.select("article.post")
        for (articleElement in articleElements) {
            val id = articleElement?.attr("itemId")
            val titleElement = articleElement.selectFirst("a")
            val title = titleElement?.attr("title")
            if (title.isNullOrEmpty())
                continue
            val urlId = titleElement.attr("href")
            if (urlId.isNullOrEmpty())
                continue
            val labelElement = articleElement.selectFirst("a.label")

            res.add(News().apply {
                this.setId(urlId)
                this.setTitle(title)
                this.imgUrl = articleElement.selectFirst("img[itemprop=image][id=hb${id}]")?.attr("src")
                        ?: ""
                this.description = articleElement.selectFirst("div[itemprop=description]")?.text()
                this.tagLink = labelElement?.attr("href")
                this.tagName = labelElement?.text()
                this.commentsCount = articleElement.selectFirst(".v-count")?.text()?.toIntOrNull()
                        ?: 0
                this.newsDate = articleElement.selectFirst("meta[itemprop=datePublished]")
                        ?.attr("content")?.take(10)
                this.author = articleElement.selectFirst("span.autor")?.text()
                        ?: articleElement.selectFirst("meta[itemprop=author]")?.attr("content")
            })
        }
        return dailyNewsPage
    }

    private fun normalizeRss(body: String): String {
        return body.replace("&(?!.{1,4};)".toRegex(), "&amp;")
    }


    fun getNewsFromRss(httpClient: IHttpClient, url: String?): ArrayList<News> {
        val res = ArrayList<News>()
        try {
            var body = httpClient.performGet(url).responseBody
            if (TextUtils.isEmpty(body)) throw NotReportException("Сервер вернул пустую страницу!")
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            body = normalizeRss(body)
            val document = db.parse(InputSource(StringReader(body)))
            val element = document.documentElement
            val nodeList = element.getElementsByTagName("item")
            if (nodeList.length > 0) {
                for (i in 0 until nodeList.length) {
                    val entry = nodeList.item(i) as Element
                    val _titleE = entry.getElementsByTagName("title").item(0) as Element
                    val _descriptionE = entry.getElementsByTagName("description").item(0) as Element
                    val _pubDateE = entry.getElementsByTagName("pubDate").item(0) as Element
                    val _linkE = entry.getElementsByTagName("link").item(0) as Element
                    val _title = StringBuilder()
                    var nodes = _titleE.childNodes
                    var nodesLength = nodes.length
                    for (c in 0 until nodesLength) {
                        _title.append(nodes.item(c).nodeValue)
                    }


                    //String _description = _descriptionE.getFirstChild().getNodeValue();
                    val _description = StringBuilder()
                    nodes = _descriptionE.childNodes
                    nodesLength = nodes.length
                    for (c in 0 until nodesLength) {
                        _description.append(nodes.item(c).nodeValue.replace("\n", " "))
                    }
                    val _pubDate = Date(_pubDateE.firstChild.nodeValue)
                    val _link = _linkE.firstChild.nodeValue
                    val author = entry.getElementsByTagName("dc:creator").item(0).childNodes.item(0).nodeValue
                    val news = News(Uri.parse(_link).path, _title.toString())
                    news.newsDate = DateTimeExternals.getDateString(_pubDate)
                    news.author = author
                    news.description = _description.toString().replace("(<img.*?/>)".toRegex(), "")
                    res.add(news)
                }
            }
        } catch (ex: Throwable) {
            Log.e("NewsApi", ex.toString())
        }
        return res
    }

    private fun lastPageNum(pagebody: String): Int {
        val m = Pattern.compile("<ul class=\"page-nav\">.*href=\"[\\s\\S]*/+page/+(\\d+)/+\">\\d+.*?</ul>").matcher(pagebody)
        return if (m.find()) {
            m.group(1).toInt()
        } else 1
    }
}