package org.softeg.slartus.forpdanotifyservice.qms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.softeg.slartus.forpdanotifyservice.favorites.FavoritesNotifier;


/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 31.05.13
 * Time: 8:53
 * To change this template use File | Settings | File Templates.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if("QMS_ALARM".equals(intent.getAction())) {
            Log.i(DEBUG_TAG, DEBUG_TAG + ".onReceive");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    QmsNotifier checker = new QmsNotifier(context);
                    checker.checkUpdates();
                }
            });
            thread.start();
        }
    }
}
