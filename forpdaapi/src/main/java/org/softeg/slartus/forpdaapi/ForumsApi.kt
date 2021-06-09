package org.softeg.slartus.forpdaapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.softeg.slartus.forpdacommon.BasicNameValuePair
import org.softeg.slartus.forpdacommon.NameValuePair
import org.softeg.slartus.forpdacommon.URIUtils
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.Http
import java.util.*
import javax.net.ssl.SSLHandshakeException

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:41
 */
class ForumsApi : ArrayList<Forum>() {
    companion object {

        fun loadForumsList(): List<Forum> {
            val response =  try {
                Http.instance
                    .performGet("https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/forum_struct.json")

            }catch(ex:SSLHandshakeException) {
                Http.instance
                    .performGet("http://slartus.ru/4pda/forum_struct.json")
            }
            val itemsListType = object : TypeToken<List<Forum>>() {}.type
            return Gson().fromJson(response.responseBody, itemsListType)
        }

        @Throws(Throwable::class)
        fun markAllAsRead(httpClient: IHttpClient) {
            httpClient.performGet(
                "https://${HostHelper.host}/forum/index.php?act=Login&CODE=05",
                true,
                false
            )
        }

        @Throws(Throwable::class)
        fun markForumAsRead(httpClient: IHttpClient, forumId: CharSequence) {

            val qparams = ArrayList<NameValuePair>()
            qparams.add(BasicNameValuePair("act", "login"))
            qparams.add(BasicNameValuePair("CODE", "04"))
            qparams.add(BasicNameValuePair("f", forumId.toString()))
            qparams.add(BasicNameValuePair("fromforum", forumId.toString()))


            val uri =
                URIUtils.createURI("http", HostHelper.host, "/forum/index.php", qparams, "UTF-8")

            httpClient.performGet(uri.toString())
        }
    }
}
