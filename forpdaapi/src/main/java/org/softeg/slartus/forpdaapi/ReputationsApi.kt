package org.softeg.slartus.forpdaapi

import android.net.Uri
import org.jsoup.Jsoup
import org.softeg.slartus.forpdaapi.classes.ReputationsListData
import org.softeg.slartus.forpdacommon.BasicNameValuePair
import org.softeg.slartus.forpdacommon.NameValuePair
import org.softeg.slartus.forpdacommon.URIUtils.Companion.createURI
import org.softeg.slartus.hosthelper.HostHelper
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/*
 * Created by slinkin on 23.04.2014.
 */   object ReputationsApi {
    /**
     * Загружает историю репутации пользователя
     *
     * @param self       - действия пользователя с репутацией других пользователей
     */
    @JvmStatic
    @Throws(IOException::class)
    fun loadReputation(httpClient: IHttpClient, userId: String?, self: Boolean,
                       listInfo: ListInfo, plusImage: String?): ReputationsListData {
        val qparams: MutableList<NameValuePair> = ArrayList()
        qparams.add(BasicNameValuePair("act", "rep"))
        qparams.add(BasicNameValuePair("type", "history"))
        qparams.add(BasicNameValuePair("mid", userId))
        qparams.add(BasicNameValuePair("st", listInfo.from.toString()))
        if (self) // свои действия
            qparams.add(BasicNameValuePair("mode", "from"))
        val uri = createURI("http", HostHelper.host, "/forum/index.php",
                qparams, "UTF-8")
        val body = httpClient.performGet(uri).responseBody
        val doc = Jsoup.parse(body)
        var el = doc.select("div.maintitle").first()
        val res = ReputationsListData()
        if (el != null) {
            val userMatcher = Pattern.compile("\\(.*?\\)\\s*(.*?)\\s*(\\S*)\\s*\\[(\\+\\d+\\/-\\d+)\\]",
                    Pattern.CASE_INSENSITIVE).matcher(el.text())
            if (userMatcher.find()) {
                res.title = userMatcher.group(1)
                res.user = userMatcher.group(2)
                res.rep = userMatcher.group(3)
            }
        }
        el = doc.select("div.pagination").first()
        if (el != null) {
            var pel = el.select("a[href=#]").first()
            if (pel != null) {
                val m = Pattern.compile("(\\d+)").matcher(pel.text())
                if (m.find()) res.pagesCount = m.group(1).toInt()
            }
            pel = el.select("span.pagecurrent").first()
            if (pel != null) {
                res.currentPage = pel.text().toInt()
            }
        }
        for (trElement in doc.selectFirst("div.borderwrap table.ipbtable tbody")?.select("tr")
            ?: emptyList()) {
            val tdElements = trElement.select("td")
            if (tdElements.size < 5) continue
            val rep = ReputationEvent()
            var tdElement = tdElements[0]
            var l = tdElement.selectFirst("a")
            if (l != null) {
                val ur = Uri.parse(l.attr("href"))
                rep.userId = ur.getQueryParameter("showuser")
                rep.user = l.text()
            }
            tdElement = tdElements[1]
            l = tdElement.selectFirst("a")
            if (l != null) {
                rep.sourceUrl = l.attr("href")
                rep.source = l.text()
            } else {
                rep.sourceUrl = null
                rep.source = tdElement.text()
            }
            tdElement = tdElements[2]
            rep.setDescription(tdElement.text())
            tdElement = tdElements[3]
            rep.state = if (tdElement.html().contains(plusImage!!)) IListItem.STATE_GREEN else IListItem.STATE_RED
            tdElement = tdElements[4]
            rep.setDate(tdElement.text())
            res.items.add(rep)
        }
        return res
    }

    /**
     * Изменение репутации пользователя
     *
     *
     * @param postId     Идентификатор поста, за который поднимаем репутацию. 0 - "в профиле"
     * @param type       "add" - поднять, "minus" - опустить
     * @return Текст ошибки или пустая строка в случае успеха
     */
    @JvmStatic
    @Throws(IOException::class)
    fun changeReputation(httpClient: IHttpClient, postId: String, userId: String, type: String, message: String,
                         outParams: MutableMap<String?, String?>): Boolean {
        val additionalHeaders: MutableMap<String, String> = HashMap()
        additionalHeaders["act"] = "rep"
        additionalHeaders["p"] = postId
        additionalHeaders["mid"] = userId
        additionalHeaders["type"] = type
        additionalHeaders["message"] = message
        val res = httpClient.performPost("https://"+ HostHelper.host +"/forum/index.php", additionalHeaders).responseBody
        val p = Pattern.compile("<title>(.*?)</title>")
        val m = p.matcher(res)
        if (m.find()) {
            if (m.group(1) != null && m.group(1).contains("Ошибка")) {
                val doc = Jsoup.parse(res)
                val element = doc.select("div.content").first()
                if (element != null) {
                    outParams["Result"] = element.text()
                } else {
                    outParams["Result"] = doc.text()
                }
                return false
            }
            outParams["Result"] = "Репутация: " + m.group(1)
            return true
        }
        outParams["Result"] = "Репутация изменена"
        return true
    }
}