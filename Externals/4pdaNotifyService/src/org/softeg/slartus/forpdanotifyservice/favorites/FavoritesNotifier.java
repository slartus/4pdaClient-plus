package org.softeg.slartus.forpdanotifyservice.favorites;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicsApi;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.NotificationBridge;
import org.softeg.slartus.forpdanotifyservice.Client;
import org.softeg.slartus.forpdanotifyservice.MainService;
import org.softeg.slartus.forpdanotifyservice.NotifierBase;
import org.softeg.slartus.forpdanotifyservice.R;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by slinkin on 26.08.13.
 */
public class FavoritesNotifier extends NotifierBase {

    private static final String LOG_TAG = "QmsMainService.FavoritesNotifier";
    public static final String NEW_ACTION = "org.softeg.slartus.forpdanotifyservice.newtopicpost";
    public static final String TIME_OUT_KEY = "FavoritesNotifier.service.timeout";
    public static final String LAST_DATETIME_KEY = "FavoritesNotifier.service.lastdatetime";
    public static final String NEW_TOPICS_COUNT_KEY = "NewTopicsCount";
    public static final String HAS_UNREAD_NOTIFY_KEY = "HasUnreadNotify";
    private Boolean m_PinnedOnly = false;

    public FavoritesNotifier(Context context) {
        super(context);
        readConfig();
    }

    private void readConfig() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_PinnedOnly = preferences.getBoolean("FavoritesNotifier.service.pinned_only", false);
    }

    public static Boolean isUse(Context context) {
        return ExtPreferences.getBoolean(context, "FavoritesNotifier.service.use", false);
    }

    private int getNewTopicsCount(ArrayList<FavTopic> topics) {
        int res = 0;
        for (FavTopic topic : topics) {
            if (topic.getIsNew()&&(!m_PinnedOnly||topic.isPinned()))
                res++;
        }
        return res;
    }

    public void checkUpdates() {
        try {
            Log.i(LOG_TAG, "checkFavorites.start");
            if (!isUse(getContext()))
                return;
            String cookiesPath = loadCookiesPath();

            // Log.d(LOG_TAG, "CookiesPath " + cookiesPath);

            if (cookiesPath == null)
                return;


            Client client = new Client(cookiesPath);
            ArrayList<FavTopic> topics = TopicsApi.getFavTopics(client, new ListInfo());
            Log.d(LOG_TAG, "favorites.size=" + topics.size());

            Intent intent = new Intent(NEW_ACTION);
            intent.putExtra(NEW_TOPICS_COUNT_KEY, getNewTopicsCount(topics));
            Boolean hasUnread = false;
            if (hasUnreadNotify(client, topics)) {
                hasUnread = true;
                intent.putExtra(HAS_UNREAD_NOTIFY_KEY, true);
            } else {
                intent.putExtra(HAS_UNREAD_NOTIFY_KEY, false);
            }
            sendNotify(getContext(), topics, hasUnread);
            getContext().sendBroadcast(intent);

            Log.i(LOG_TAG, "checkFavorites.end");

        } catch (Throwable throwable) {
            Log.e(LOG_TAG, throwable.toString());
        }
    }

    private boolean hasUnreadNotify(Client client, ArrayList<FavTopic> topics) throws Throwable {
        Log.d(LOG_TAG, "favorites.hasUnreadNotify=");

        if (topics.size() == 0 || getNewTopicsCount(topics) == 0)
            return false;

        Topic lastPostedTopic = null;
        for (FavTopic topic : topics) {
            if (topic.getIsNew()&&(!m_PinnedOnly||topic.isPinned())) {
                lastPostedTopic = topic;
                break;
            }
        }

        if (lastPostedTopic==null||!lastPostedTopic.getIsNew())
            return false;

        GregorianCalendar lastPostedTopicCalendar = new GregorianCalendar();
        lastPostedTopicCalendar.setTime(lastPostedTopic.getLastMessageDate());


        GregorianCalendar lastDateTime = loadLastDate(LAST_DATETIME_KEY);

        if (lastDateTime == null || lastDateTime.before(lastPostedTopicCalendar)) {
            saveLastDate(lastPostedTopicCalendar, LAST_DATETIME_KEY);
            return true;
        }
        return false;
    }

    @Override
    public void readSettings(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(TIME_OUT_KEY)) {
            float timeOut = intent.getExtras().getFloat(TIME_OUT_KEY, 5);
            // Log.d(LOG_TAG, "timeOutExtras " + timeOut);
            saveTimeOut(context, timeOut, TIME_OUT_KEY);
        }
    }

    public static int REQUEST_CODE_START = 839264722;

    private static PendingIntent getAlarmPendingIntent(Context context, int flag) {
        Intent downloader = new Intent(context, FavoritesAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE_START, downloader, flag);
    }

    private static void restartTaskStatic(Context context) {
        float timeOut = loadTimeOut(context, TIME_OUT_KEY);
        Log.i(LOG_TAG, "checkFavorites.TimeOut: " + timeOut);

        PendingIntent pendingIntent = getAlarmPendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (long) (timeOut * 60000), pendingIntent);
    }

    public static void restartTask(final Context context, Intent intent) {
        MainService.readCookiesPath(context, intent);
        FavoritesNotifier notifier = new FavoritesNotifier(context);
        notifier.readSettings(context, intent);
        notifier.restartTask(context);
    }

    @Override
    public void restartTask(Context context) {
        restartTaskStatic(context);
    }

    public static void cancelAlarm(Context context) {
        FavoritesNotifier notifier = new FavoritesNotifier(context);
        notifier.cancel(context);
    }

    @Override
    public void cancel(Context context) {
        try {
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(getAlarmPendingIntent(context, 0));
        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }

    private static final int MY_NOTIFICATION_ID = 2;

    private void sendNotify(Context context, ArrayList<FavTopic> topics, Boolean hasUnread) {
        Log.i(LOG_TAG, "favotires sendNotify");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String url = "http://4pda.ru/forum/index.php?autocom=favtopics";
        if (getNewTopicsCount(topics) == 1)
            url = "http://4pda.ru/forum/index.php?showtopic=" + topics.get(0).getId() + "&view=getnewpost";

        Intent marketIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url));
        if (hasUnread) {
            Log.i(LOG_TAG, "favotires notify!");
            String message = "Избранное (" + getNewTopicsCount(topics) + ")";
            if (getNewTopicsCount(topics) == 1) {
                message = String.format("%s ответил в тему \"%s\", на которую вы подписаны",
                        topics.get(0).getLastMessageAuthor(), topics.get(0).getTitle());
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, marketIntent, 0);
            Notification noti = NotificationBridge.createBridge(
                    context,
                    R.drawable.icon,
                    "Непрочитанные сообщения в темах",
                    System.currentTimeMillis())
                    .setContentTitle(message)
                    .setContentText("Непрочитанные сообщения в темах")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .createNotification();

            notificationManager.notify(MY_NOTIFICATION_ID, noti);
        } else if (getNewTopicsCount(topics) == 0) {
            notificationManager.cancel(MY_NOTIFICATION_ID);
        }
    }
}
