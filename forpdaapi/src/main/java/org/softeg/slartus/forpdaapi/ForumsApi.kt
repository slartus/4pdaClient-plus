package org.softeg.slartus.forpdaapi

import android.net.Uri
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.softeg.slartus.forpdaapi.classes.ForumsData
import org.softeg.slartus.forpdacommon.BasicNameValuePair
import org.softeg.slartus.forpdacommon.NameValuePair
import org.softeg.slartus.forpdacommon.URIUtils
import ru.slartus.http.Http
import java.util.*

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:41
 */
class ForumsApi : ArrayList<Forum>() {
    companion object {

        /**
         * Загрузка дерева разделов форума
         */
        @Throws(Exception::class)
        fun loadForums(progressState: ProgressState): ForumsData {
            val res = ForumsData()

            val pageBody = Http.instance
                    .performGetFull("https://4pda.ru/forum/index.php?act=idx").responseBody
            val doc = Jsoup.parse(pageBody!!, "https://4pda.ru")
            val categoryElements = doc.select("div.borderwrap[id~=fo_\\d+]")

            for (catElement in categoryElements) {
                progressState.update("Обновление структуры форума...",
                        res.items.size.toLong())
                val el = catElement.select("div.maintitle a[href~=showforum=\\d+]").first()
                        ?: continue

                val uri = Uri.parse(el.attr("href"))
                val forum = Forum(uri.getQueryParameter("showforum"), el.text())


                forum.isHasTopics = false

                forum.description = null
                res.items.add(forum)
                val c = res.items.size

                loadCategoryForums(catElement.select("table.ipbtable>tbody").first(), forum,
                        res, progressState)
                if (res.items.size > c)
                    forum.iconUrl = res.items[c].iconUrl
            }


            return res
        }

        @Throws(Exception::class)
        private fun loadCategoryForums(boardForumRowElement: Element?, parentForum: Forum,
                                       data: ForumsData, progressState: ProgressState) {
            if (boardForumRowElement == null)
                return

            val categoryElements = boardForumRowElement.select("tr:has(td)")
            if (categoryElements.size > 0)
                parentForum.isHasForums = true
            for (trElement in categoryElements) {
                progressState.update("Обновление структуры форума...",
                        data.items.size.toLong())

                val tdElements = trElement.children()

                if (tdElements.size < 5) continue


                var tdElement = tdElements[0]
                var el: Element? = tdElement.select("img").first()
                var iconUrl: String? = null
                if (el != null)
                    iconUrl = el.attr("src")

                tdElement = tdElements[1]

                el = tdElement.select("b>a").first()
                if (el == null)
                    continue
                val uri = Uri.parse(el.absUrl("href"))
                val forum = Forum(uri.getQueryParameter("showforum"), el.text())
                forum.iconUrl = iconUrl
                forum.isHasTopics = true
                forum.parentId = parentForum.id
                data.items.add(forum)

                el = tdElement.select("span.forumdesc").first()
                if (el != null) {
                    forum.description = el.ownText()
                    if (el.select("a[href~=showforum=\\d+]").size > 0) {
                        loadSubForums(uri.toString(), forum, data, progressState)
                    }
                }
            }
        }

        @Throws(Exception::class)
        private fun loadSubForums(url: String, parentForum: Forum,
                                  data: ForumsData, progressState: ProgressState) {
            val pageBody = Http.instance.performGetFull(url).responseBody
            val doc = Jsoup.parse(pageBody!!, "https://4pda.ru")
            val catElement = doc.select("div.borderwrap[id~=fo_\\d+]").first() ?: return
            val boardForumRowElement = catElement.select("table.ipbtable>tbody").first() ?: return


            val categoryElements = boardForumRowElement.select("tr:has(td)")
            if (categoryElements.size > 0)
                parentForum.isHasForums = true
            for (trElement in categoryElements) {
                progressState.update("Обновление структуры форума...",
                        data.items.size.toLong())
                val tdElements = trElement.children()
                if (tdElements.size < 5) continue

                var tdElement = tdElements[0]
                var el: Element? = tdElement.select("img").first()
                var iconUrl: String? = null
                if (el != null)
                    iconUrl = el.attr("src")

                tdElement = tdElements[1]

                el = tdElement.select("b>a").first()
                if (el == null)
                    continue
                val uri = Uri.parse(el.absUrl("href"))
                val forum = Forum(uri.getQueryParameter("showforum"), el.text())
                forum.iconUrl = iconUrl
                forum.isHasTopics = true
                forum.parentId = parentForum.id
                data.items.add(forum)

                el = tdElement.select("span.forumdesc").first()
                if (el != null) {
                    forum.description = el.ownText()
                    if (el.select("a[href~=showforum=\\d+]").size > 0) {
                        loadSubForums(uri.toString(), forum, data, progressState)
                    }
                }

            }
        }

        @Throws(Throwable::class)
        fun markAllAsRead(httpClient: IHttpClient) {
            httpClient.performGet("https://4pda.ru/forum/index.php?act=Login&CODE=05", true, false)
        }

        @Throws(Throwable::class)
        fun markForumAsRead(httpClient: IHttpClient, forumId: CharSequence) {

            val qparams = ArrayList<NameValuePair>()
            qparams.add(BasicNameValuePair("act", "login"))
            qparams.add(BasicNameValuePair("CODE", "04"))
            qparams.add(BasicNameValuePair("f", forumId.toString()))
            qparams.add(BasicNameValuePair("fromforum", forumId.toString()))


            val uri = URIUtils.createURI("http", "4pda.ru", "/forum/index.php", qparams, "UTF-8")

            httpClient.performGet(uri.toString())
        }
    }
}
