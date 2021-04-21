package org.softeg.slartus.forpdanotifyservice.favorites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.softeg.slartus.forpdanotifyservice.BuildConfig;

/**
 * Created by slinkin on 26.08.13.
 */
public class FavoritesAlarmReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = "FavoritesAlarmReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if("FAVORITES_ALARM".equals(intent.getAction())) {
            Log.i(DEBUG_TAG, DEBUG_TAG + ".onReceive");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    FavoritesNotifier checker = new FavoritesNotifier(context);
                    checker.checkUpdates();
                }
            });
            thread.start();
        }
    }
}
