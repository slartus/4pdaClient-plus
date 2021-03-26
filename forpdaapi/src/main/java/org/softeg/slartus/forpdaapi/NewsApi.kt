package org.softeg.slartus.forpdaapi

import android.content.SharedPreferences
import android.net.Uri
import android.text.Html
import android.text.TextUtils
import android.util.Log
import org.jsoup.Jsoup
import org.softeg.slartus.forpdacommon.DateTimeExternals
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.PatternExtensions
import org.softeg.slartus.forpdacommon.UrlExtensions
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

/*
* Created by slinkin on 20.02.14.
*/
object NewsApi {
    @kotlin.Throws(IOException::class)
    fun like(httpClient: IHttpClient, newsId: String): Boolean {
        val res = httpClient.performGet("https://4pda.ru/wp-content/plugins/karma/ajax.php?p=$newsId&c=0&v=1", false, false).responseBody
        return res != null
    }

    @kotlin.Throws(IOException::class)
    fun likeComment(httpClient: IHttpClient, newsId: String, postId: String): Boolean {
        val res = httpClient.performGet("https://4pda.ru/wp-content/plugins/karma/ajax.php?p=$newsId&c=$postId&v=1", false, false).responseBody
        return res != null
    }

    fun parseNewsBody(newsPageBody: String): String {
        var newsPageBody = newsPageBody
        val doc = Jsoup.parse(newsPageBody, "https://4pda.ru")
        val bodyElement = doc.select("div.container").first()
        if (bodyElement != null) return bodyElement.parent().html()
        var m = PatternExtensions.compile("<article[^>]*>([\\s\\S]*?)</article>").matcher(newsPageBody)
        if (m.find()) newsPageBody = m.group(1) else {
            m = PatternExtensions.compile("<body[^>]*>([\\s\\S]*?)</body>").matcher(newsPageBody)
            newsPageBody = if (m.find()) m.group(1) else {
                newsPageBody.replace("[\\s\\S]*?<body[^>]*>".toRegex(), "<body><div id=\"main\">")
            }
        }
        return newsPageBody
    }

    private fun msg(message: String) {
        Log.e("TEST", message)
    }

    @kotlin.Throws(Exception::class)
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
        pageNum = Math.ceil((listInfo.from / page).toDouble()).toInt() + pageNum
        val requestUrl = "$justUrl/page/$pageNum/$params"
        val res = ArrayList<News>()
        val dailyNewsPage = httpClient.performGet(UrlExtensions.removeDoubleSplitters(requestUrl)).responseBody
        val doc = Jsoup.parse(dailyNewsPage)
        val articleElements = doc.select("article.post")
        val articlesPattern = Pattern.compile("(<article class=\"post[^\"]*?\"[^>]*?>[^<]*?<div[^>]*?>[^<]*?(?:<div[^>]*?>[^<]*?<\\/div>)?[^<]*?<a[^>]*?href=\"([^\"]*)\" title[\\s\\S]*?src=\"([^\"]*)\" alt=\"([^\"]*?)\"[\\s\\S]*?<\\/article>)|(<li itemscope[^>]*>[\\s\\S]*?itemprop=\"url\" href=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\" alt=\"([^\"]*?)\"[\\s\\S]*?<\\/div>[^<]*<\\/li>)")
        val descriptionPattern = Pattern.compile("(<div itemprop=\"description\">[\\s\\S]*?<p [^>]*>([\\s\\S]*)<\\/p>[^<]*)|(<div itemprop=\"description\">([\\s\\S]*?)<\\/div>)")
        val labelPattern = Pattern.compile("<a href=\"([^\"]*)\" class=\"label[^>]*>([\\s\\S]*?)<\\/a>")
        val countPattern = Pattern.compile("class=\"v-count\"[^>]*>(\\d*)</a>")
        val datePattern = Pattern.compile("<meta itemprop=\"datePublished\" content=\"(\\d+-\\d+-\\d+)[\\s\\S]*?\"\\/>")
        val authorPattern = Pattern.compile("(<span class=\"autor\"><a [^>]*>([^<]*)</a>)|(<meta itemprop=\"author\" content=\"([^\"]*)\"/>)")
        m = articlesPattern.matcher(dailyNewsPage)
        var matcher: Matcher? = null

        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        var news: News
        var group: Int
        var childGroup: Int
        while (m.find()) {
            news = News()
            group = 0
            childGroup = 0
            if (m.group(1) == null) group = 4
            news.setId(m.group(group + 2).replace("https://4pda.ru", ""))
            news.setTitle(Html.fromHtml(m.group(group + 4).replace("&amp;".toRegex(), "&")).toString())
            news.imgUrl = m.group(group + 3)
            if (matcher == null) matcher = descriptionPattern.matcher(m.group(group + 1)) else matcher.usePattern(descriptionPattern).reset(m.group(group + 1))
            if (matcher!!.find()) {
                if (matcher.group(1) == null) childGroup = 2
                news.description = Html.fromHtml(matcher.group(childGroup + 2).replace("<a [^>]*>([^<]*)</a>".toRegex(), "$1")).toString().trim { it <= ' ' }
            }
            childGroup = 0
            matcher.usePattern(labelPattern).reset(m.group(group + 1))
            if (matcher.find()) {
                news.tagLink = matcher.group(1)
                news.tagTitle = Html.fromHtml(matcher.group(2).trim { it <= ' ' })
            } else {
                news.tagTitle = ""
            }
            matcher.usePattern(countPattern).reset(m.group(group + 1))
            if (matcher.find()) {
                news.commentsCount = matcher.group(1).toInt()
            }
            matcher.usePattern(datePattern).reset(m.group(group + 1))
            if (matcher.find()) {
                news.newsDate = matcher.group(1)
            }
            matcher.usePattern(authorPattern).reset(m.group(group + 1))
            if (matcher.find()) {
                if (matcher.group(1) == null) childGroup = 2
                news.author = matcher.group(childGroup + 2)
            }
            res.add(news)
        }
        if (res.size == 0 && pageNum == 1 && listInfo.from == 0) return getNewsFromRss(httpClient, UrlExtensions.removeDoubleSplitters("$url/feed/"))
        val lastPageNum = lastPageNum(dailyNewsPage)
        listInfo.outCount = res.size * lastPageNum
        if (listInfo.from == 0 && res.size > 0) {
            preferences.edit().putInt("lm", res.size).apply()
        }
        return res
    }

    private fun normalizeRss(body: String): String {
        return body.replace("&(?!.{1,4};)".toRegex(), "&amp;")
    }

    @kotlin.Throws(Exception::class)
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