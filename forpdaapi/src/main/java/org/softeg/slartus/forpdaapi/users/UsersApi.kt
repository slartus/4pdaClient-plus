package org.softeg.slartus.forpdaapi.users

import org.jsoup.Jsoup
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.IHttpClient
import org.softeg.slartus.hosthelper.HostHelper.Companion.host
import java.io.IOException
import java.util.regex.Pattern

/*
 * Created by slinkin on 10.04.2014.
 */
object UsersApi {
    /**
     * Администрация: Админы, суперы,модеры
     *
     */
    @JvmStatic
    @Throws(IOException::class)
    fun getLeaders(client: IHttpClient): ArrayList<LeadUser> {
        val page =
            client.performGet("https://$host/forum/index.php?act=Stats&CODE=leaders").responseBody
        val doc = Jsoup.parse(page)
        val res = ArrayList<LeadUser>()
        val p = Pattern.compile("showuser=(\\d+)", Pattern.CASE_INSENSITIVE)
        for (groupElement in doc.select("div.borderwrap")) {
            val group = groupElement.select("div.maintitle").firstOrNull()?.text()?.trim()
            for (trElement in groupElement.select("table.ipbtable").firstOrNull()?.select("tr")?: emptyList()) {
                val tds = trElement.select("td.row1")
                if (tds.size == 0) continue
                val el = tds[0].select("a").firstOrNull()?: continue
                val m = p.matcher(el.attr("href"))
                if (m.find()) {
                    val user = LeadUser(m.group(1), el.text())
                    user.group = group
                    val forumElements = tds[1].select("option")
                    if (forumElements.size == 0 && "Все форумы" == tds[1].text()) {
                        user.forums.add(Forum("-1", "Все форумы"))
                    } else {
                        for (forumEl in forumElements) {
                            if ("-1" == forumEl.attr("value")) continue
                            user.forums.add(Forum(forumEl.attr("value"), forumEl.text()))
                        }
                    }
                    res.add(user)
                }
            }
        }
        return res
    }
}