package org.softeg.slartus.forpdanotifyservice.favorites

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import org.softeg.slartus.forpdaapi.FavTopic
import org.softeg.slartus.forpdaapi.ListInfo
import org.softeg.slartus.forpdaapi.TopicsApi
import org.softeg.slartus.forpdacommon.ExtPreferences
import org.softeg.slartus.forpdanotifyservice.MainService
import org.softeg.slartus.forpdanotifyservice.NotifierBase
import java.util.*

/**
 * Created by slinkin on 26.08.13.
 */
class FavoritesNotifier(context: Context?) : NotifierBase(context) {
    private var m_PinnedOnly = false
    private fun readConfig() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        m_PinnedOnly = preferences.getBoolean("FavoritesNotifier.service.pinned_only", false)
    }

    override fun checkUpdates() {
        try {
            Log.i(LOG_TAG, "checkFavorites.start")
            if (!isUse(context)) return
            loadCookiesPath() ?: return

            // Log.d(LOG_TAG, "CookiesPath " + cookiesPath);
            val unreadTopics = getUnreadTopics(TopicsApi.getFavTopics(ListInfo()))

            val hasNewUnread = hasNewUnreadTopic(unreadTopics)
            updateNotification(context, unreadTopics, hasNewUnread)
            Log.i(LOG_TAG, "checkFavorites.end")
        } catch (throwable: Throwable) {
            Log.e(LOG_TAG, throwable.toString())
        }
    }

    private fun getUnreadTopics(topics: ArrayList<FavTopic>) = topics
            .filter { it.isNew && (!m_PinnedOnly || it.isPinned) }
            .sortedByDescending { it.lastMessageDate }


    private fun hasNewUnreadTopic(unreadTopics: List<FavTopic>): Boolean {
        if(unreadTopics.isEmpty())return false
        val lastPostedTopic = unreadTopics.first()
        val lastPostedTopicCalendar = GregorianCalendar()
        lastPostedTopicCalendar.time = lastPostedTopic.lastMessageDate
        val lastDateTime = loadLastDate(LAST_DATETIME_KEY)
        if (lastDateTime == null || lastDateTime.before(lastPostedTopicCalendar)) {
            saveLastDate(lastPostedTopicCalendar, LAST_DATETIME_KEY)
            return true
        }
        return false
    }

    override fun readSettings(context: Context, intent: Intent) {
        if (intent.extras?.containsKey(TIME_OUT_KEY) == true) {
            val timeOut = intent.extras!!.getFloat(TIME_OUT_KEY, 5f)
            // Log.d(LOG_TAG, "timeOutExtras " + timeOut);
            saveTimeOut(context, timeOut, TIME_OUT_KEY)
        }
    }

    override fun restartTask(context: Context) {
        restartTaskStatic(context)
    }

    override fun cancel(context: Context) {
        try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            alarm?.cancel(getAlarmPendingIntent(context, 0))
        } catch (ex: Throwable) {
            Log.e(LOG_TAG, ex.toString())
        }
    }

    private fun updateNotification(context: Context, unreadTopics: List<FavTopic>, hasNewUnread: Boolean) {
        Log.i(LOG_TAG, "favotires sendNotify")
        if (hasNewUnread) {
            Log.i(LOG_TAG, "favotires notify!")
            var message = "Избранное (" + unreadTopics.size + ")"
            if (unreadTopics.size == 1) {
                message = String.format("%s ответил в тему \"%s\", на которую вы подписаны",
                        unreadTopics.first().lastMessageAuthor, unreadTopics.first().title)
            }
            var url = "https://4pda.ru/forum/index.php?autocom=favtopics"
            if (unreadTopics.size == 1) url = "https://4pda.ru/forum/index.php?showtopic=" + unreadTopics.first().id + "&view=getnewpost"
            sendNotify(context, message, "Непрочитанные сообщения в темах", url, MY_NOTIFICATION_ID)
        } else if (unreadTopics.isEmpty()) {
            cancelNotification(context, MY_NOTIFICATION_ID)
        }
    }

    companion object {
        private const val LOG_TAG = "FavoritesNotifier"
        const val TIME_OUT_KEY = "FavoritesNotifier.service.timeout"
        private const val LAST_DATETIME_KEY = "FavoritesNotifier.service.lastdatetime"


        @JvmStatic
        fun isUse(context: Context?): Boolean {
            return ExtPreferences.getBoolean(context, "FavoritesNotifier.service.use", false)
        }

        private const val REQUEST_CODE_START = 839264722
        private fun getAlarmPendingIntent(context: Context, flag: Int): PendingIntent {
            val downloader = Intent(context, FavoritesAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(context, REQUEST_CODE_START, downloader, flag)
        }

        private fun restartTaskStatic(context: Context) {
            val timeOut = loadTimeOut(context, TIME_OUT_KEY)
            Log.i(LOG_TAG, "checkFavorites.TimeOut: $timeOut")
            val pendingIntent = getAlarmPendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            alarm?.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (timeOut * 60000).toLong(), pendingIntent)
        }

        @JvmStatic
        fun restartTask(context: Context, intent: Intent) {
            MainService.readCookiesPath(context, intent)
            val notifier = FavoritesNotifier(context)
            notifier.readSettings(context, intent)
            notifier.restartTask(context)
        }

        @JvmStatic
        fun cancelAlarm(context: Context) {
            val notifier = FavoritesNotifier(context)
            notifier.cancel(context)
        }

        private const val MY_NOTIFICATION_ID = 2
    }

    init {
        readConfig()
    }
}