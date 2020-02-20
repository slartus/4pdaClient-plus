package org.softeg.slartus.forpdaapi;/*
 * Created by slinkin on 18.07.2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

public class ClientPreferences {
    public static class Notifications {
        public static Boolean useSound(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getBoolean("notifiers.service.use_sound", true);
        }

        public static void setSound(Context context, Uri soundUri) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString("notifiers.service.sound", soundUri != null ? soundUri.toString() : null).commit();
        }

        public static Uri getSound(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String soundString = prefs.getString("notifiers.service.sound", defaultUri == null ? null : defaultUri.toString());
            if (soundString == null)
                return null;
            try {
                return Uri.parse(soundString);
            } catch (Throwable ex) {
                return null;
            }
        }

        public static boolean isDefaultSound(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getBoolean("notifiers.service.is_default_sound", true);
        }

        public static class SilentMode {
            public static Calendar getStartTime(Context context) {
                return getTime(context, "notifiers.silent_mode.start_time");
            }

            public static void setStartTime(Context context, int hourOfDay, int minute) {
                setTime(context, "notifiers.silent_mode.start_time", hourOfDay, minute);
            }

            public static Calendar getEndTime(Context context) {
                return getTime(context, "notifiers.silent_mode.end_time");
            }

            public static void setEndTime(Context context, int hourOfDay, int minute) {
                setTime(context, "notifiers.silent_mode.end_time", hourOfDay, minute);
            }


            public static Calendar getTime(Context context, String key) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                Date date = new Date(prefs.getLong(key, 0));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.SECOND, 0);
                return cal;
            }

            public static void setTime(Context context, String key, int hourOfDay, int minute) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putLong(key, cal.getTimeInMillis()).apply();
            }

            public static Boolean isEnabled(Context context) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                return prefs.getBoolean("notifiers.silent_mode.enabled", false);
            }
        }

        public static class Qms {

        }

        public static class Favorites {

        }
    }
}
