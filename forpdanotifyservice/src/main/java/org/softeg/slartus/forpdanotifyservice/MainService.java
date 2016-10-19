package org.softeg.slartus.forpdanotifyservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.softeg.slartus.forpdanotifyservice.favorites.FavoritesNotifier;
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;

import java.util.ArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 24.05.13
 * Time: 9:57
 * To change this template use File | Settings | File Templates.
 */
public class MainService extends Service {

    private static ArrayList<NotifierBase> getNotifiers(Context context) {
        ArrayList<NotifierBase> res = new ArrayList<>();
        res.add(new QmsNotifier(context));
        res.add(new FavoritesNotifier(context));
        return res;
    }


    private static final String LOG_TAG = "Notifier.MainService";

    public static void readCookiesPath(Context context, Intent intent) {
        try {
            // Log.d(LOG_TAG, "intent" + intent);
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("CookiesPath")) {
                saveCookiesPath(context, intent.getExtras().getString("CookiesPath"));
                // Log.d(LOG_TAG, "CookiesPath" + (intent.getExtras().getString("CookiesPath")));
            }

        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.toString());
            ex.printStackTrace();
        }
    }

    private static void readSettings(Context context, Intent intent) {
        // Log.d(LOG_TAG, "readSettings");

        try {
            // Log.d(LOG_TAG, "intent" + intent);
            readCookiesPath(context, intent);

            for (NotifierBase notifier : getNotifiers(context)) {
                notifier.readSettings(context, intent);
            }
        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.toString());
            ex.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cancelAlarm(this);
        // Log.d(LOG_TAG, "MyService onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void cancelAlarm(Context context) {
        try {
            for (NotifierBase notifier : getNotifiers(context)) {
                notifier.cancel(context);
            }
        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }


    public static void restartTask(final Context context, Intent intent) {
        readSettings(context, intent);
        restartTask(context);
    }

    public static void restartTask(final Context context) {
        try {
            for (NotifierBase notifier : getNotifiers(context)) {
                notifier.restartTask(context);
            }
        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }


    private static void saveCookiesPath(Context context, String cookiesPath) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CookiesPath", cookiesPath);
        editor.apply();
    }

}
