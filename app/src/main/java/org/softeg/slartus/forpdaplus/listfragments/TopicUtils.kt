package org.softeg.slartus.forpdaplus.listfragments

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.ListCallback
import org.softeg.slartus.forpdaapi.FavTopic
import org.softeg.slartus.forpdaapi.Topic
import org.softeg.slartus.forpdaapi.TopicApi
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.ThemeOpenParams
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable
import org.softeg.slartus.hosthelper.HostHelper
import java.io.IOException

/**
 * Created by slinkin on 20.02.14.
 */
object TopicUtils {
    /**
     * С какими параметрами навигации юзер решил открывать топик:  view=getlastpost и тд
     */
    @JvmStatic
    fun getOpenTopicArgs(topicId: CharSequence?, template: CharSequence?): String? {
        val themeActionPref = getTopicNavigateAction(template)
        return getUrlArgs(topicId, themeActionPref, null)
    }

    /**
     * Какой тип навигации юзер выбрал:  getlastpost, getfirstpost и тд
     */
    private fun getTopicNavigateAction(template: CharSequence?): String? {
        return App.getInstance().preferences.getString(String.format("%s.navigate_action", template), null)
    }

    @JvmStatic
    fun getUrlArgs(topicId: CharSequence?, openParam: String?, defaultUrlParam: String?): String? {
        if (openParam == null) return defaultUrlParam
        if (openParam == ThemeOpenParams.BROWSER) return ""
        if (openParam == Topic.NAVIGATE_VIEW_FIRST_POST) return ""
        if (openParam == Topic.NAVIGATE_VIEW_LAST_POST) return "view=getlastpost"
        if (openParam == Topic.NAVIGATE_VIEW_NEW_POST) return "view=getnewpost"
        return if (openParam == Topic.NAVIGATE_VIEW_LAST_URL) {
            try {
                val historyTopicUrl = TopicsHistoryTable.getTopicHistoryUrl(topicId)
                if (TextUtils.isEmpty(historyTopicUrl)) "view=getlastpost" else Uri.parse(historyTopicUrl).query!!.replace("showtopic=\\d+&?".toRegex(), "")
            } catch (e: IOException) {
                e.printStackTrace()
                "view=getlastpost"
            }
        } else defaultUrlParam
    }

    fun getTopicUrl(topicId: String, urlArgs: String): String {
        return "https://${HostHelper.host}/forum/index.php?showtopic=" + topicId + if (TextUtils.isEmpty(urlArgs)) "" else "&$urlArgs"
    }

    @JvmStatic
    fun showSubscribeSelectTypeDialog(context: Context, handler: Handler,
                                      topic: Topic, action: ((String) -> Unit)? = null) {
        val titles = arrayOf<CharSequence>(context.getString(R.string.no_notify), context.getString(R.string.first_time), context.getString(R.string.every_time), context.getString(R.string.every_day), context.getString(R.string.every_week))
        val values = arrayOf(TopicApi.TRACK_TYPE_NONE, TopicApi.TRACK_TYPE_DELAYED,
                TopicApi.TRACK_TYPE_IMMEDIATE, TopicApi.TRACK_TYPE_DAILY, TopicApi.TRACK_TYPE_WEEKLY)
        var selectedSubscribe: String? = null
        if (topic is FavTopic) {
            selectedSubscribe = topic.trackType
        }
        val selectedId = intArrayOf(ArrayUtils.indexOf(selectedSubscribe, values))
        MaterialDialog.Builder(context)
                .title(R.string.add_to_favorites)
                .items(*titles)
                .itemsCallback(ListCallback { dialog, view, i, text ->
                    selectedId[0] = i
                    if (selectedId[0] == -1) return@ListCallback
                    val emailtype = values[selectedId[0]]
                    Toast.makeText(context, R.string.request_sent, Toast.LENGTH_SHORT).show()
                    if (action != null) {
                        action(emailtype)
                    } else {
                        Thread {
                            var ex: Exception? = null
                            var res: String? = null
                            try {
                                res = TopicApi.changeFavorite(Client.getInstance(), topic.id, emailtype)
                            } catch (e: Exception) {
                                ex = e
                            }
                            val finalEx = ex
                            val finalRes = res
                            handler.post {
                                try {
                                    if (finalEx != null) {
                                        Toast.makeText(context, R.string.error_request, Toast.LENGTH_SHORT).show()
                                        AppLog.e(context, finalEx)
                                    } else {
                                        Toast.makeText(context, finalRes, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (ex: Exception) {
                                    AppLog.e(context, ex)
                                }
                            }
                        }.start()
                    }
                }) //.positiveText("Добавить")
                .negativeText(android.R.string.cancel)
                .show()
    }
}