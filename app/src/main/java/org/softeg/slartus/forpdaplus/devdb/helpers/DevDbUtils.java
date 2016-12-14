package org.softeg.slartus.forpdaplus.devdb.helpers;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;

/**
 * Created by isanechek on 19.12.15.
 */
public class DevDbUtils {

    public static void saveTitle(Context context, String title){
        App.getInstance().getPreferences().edit().putString("devdbDeviceTitle", title).apply();
    }
    public static String getTitle(Context context){
        return App.getInstance().getPreferences().getString("devdbDeviceTitle", "ForPDA");
    }
    // OTHER
    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
    }

    public static void showUrl(Context context, String url) {
        Handler mHandler = new Handler();
        IntentActivity.tryShowUrl(((MainActivity) context), mHandler, url, false, true);
    }
}
