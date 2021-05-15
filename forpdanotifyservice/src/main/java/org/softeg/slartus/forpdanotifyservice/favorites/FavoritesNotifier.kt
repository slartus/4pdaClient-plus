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
import org.softeg.slartus.forpdanotifyservice.BuildConfig
import org.softeg.slartus.forpdanotifyservice.NotifierBase
import org.softeg.slartus.hosthelper.HostHelper
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
            Log.i(TAG, "checkFavorites.start")
            if (!isUse(context)) return
            loadCookiesPath() ?: return

            // Log.d(LOG_TAG, "CookiesPath " + cookiesPath);
            val unreadTopics = getUnreadTopics(TopicsApi.getFavTopics(ListInfo()))

            val hasNewUnread = hasNewUnreadTopic(unreadTopics)
            updateNotification(context, unreadTopics, hasNewUnread)
            Log.i(TAG, "checkFavorites.end")
        } catch (throwable: Throwable) {
            Log.e(TAG, throwable.toString())
        } finally {
            // restartTaskStatic(context, true)
        }
    }

    private fun getUnreadTopics(topics: ArrayList<FavTopic>) = topics
            .filter { it.isNew && (!m_PinnedOnly || it.isPinned) }
            .sortedByDescending { it.lastMessageDate }


    private fun hasNewUnreadTopic(unreadTopics: List<FavTopic>): Boolean {
        if (unreadTopics.isEmpty()) return false
        val lastPostedTopic = unreadTopics.first()
        val lastPostedTopicCalendar = GregorianCalendar()
        lastPostedTopicCalendar.time = lastPostedTopic.lastMessageDate
        val lastDateTime = loadLastDate(LAST_DATETIME_KEY)
        if (lastDateTime == null || lastDateTime.before(lastPostedTopicCalendar)) {
            saveLastDate(lastPostedTopicCalendar, LAST_DATETIME_KEY)
            return true
        }
//        if (BuildConfig.DEBUG) {
//            return true
//        }
        return false
    }

    fun saveTimeout(context: Context, timeOut: Float) {
        saveTimeOut(context, timeOut, TIME_OUT_KEY)
    }

    override fun restartTask(context: Context) {
        restartTaskStatic(context, false)
    }

    override fun cancel(context: Context) {
        try {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            alarm?.cancel(getAlarmPendingIntent(context, 0))
        } catch (ex: Throwable) {
            Log.e(TAG, ex.toString())
        }
    }

    private fun updateNotification(context: Context, unreadTopics: List<FavTopic>, hasNewUnread: Boolean) {
        Log.i(TAG, "favotires sendNotify")
        if (hasNewUnread) {
            Log.i(TAG, "favotires notify!")
            var message = "Избранное (" + unreadTopics.size + ")"
            if (unreadTopics.size == 1) {
                message = String.format("%s ответил в тему \"%s\", на которую вы подписаны",
                        unreadTopics.first().lastMessageAuthor, unreadTopics.first().title)
            }
            var url = "https://${HostHelper.host}/forum/index.php?autocom=favtopics"
            if (unreadTopics.size == 1) url = "https://${HostHelper.host}/forum/index.php?showtopic=" + unreadTopics.first().id + "&view=getnewpost"
            sendNotify(context, message, "Непрочитанные сообщения в темах", url, MY_NOTIFICATION_ID)
        } else if (unreadTopics.isEmpty()) {
            cancelNotification(context, MY_NOTIFICATION_ID)
        }
    }

    companion object {
        private val TAG = FavoritesNotifier::class.simpleName
        const val TIME_OUT_KEY = "FavoritesNotifier.service.timeout"
        private const val LAST_DATETIME_KEY = "FavoritesNotifier.service.lastdatetime"


        @JvmStatic
        fun isUse(context: Context?): Boolean {
            return ExtPreferences.getBoolean(context, "FavoritesNotifier.service.use", false)
        }

        private const val REQUEST_CODE_START = 839264722
        private fun getAlarmPendingIntent(context: Context, flag: Int): PendingIntent {
            val receiverIntent = Intent(context, FavoritesAlarmReceiver::class.java)
                    .apply {
                        action = "FAVORITES_ALARM"
                    }
            return PendingIntent.getBroadcast(context, REQUEST_CODE_START, receiverIntent, flag)
        }

        private fun restartTaskStatic(context: Context, useTimeOut: Boolean) {
            val timeOut = loadTimeOut(context, TIME_OUT_KEY)
            Log.i(TAG, "checkFavorites.TimeOut: $timeOut")
            val pendingIntent = getAlarmPendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

            alarm?.cancel(pendingIntent)
            val wakeUpTime = SystemClock.elapsedRealtime() + if (useTimeOut) (timeOut * 60000).toLong() else 0L

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                alarm?.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeUpTime, pendingIntent)
            } else {
                alarm?.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeUpTime, pendingIntent)
            }
        }

        @JvmStatic
        fun restartTask(context: Context, cookesPath: String, timeOut: Float) {
            saveCookiesPath(context, cookesPath)
            val notifier = FavoritesNotifier(context)
            notifier.saveTimeout(context, timeOut)
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