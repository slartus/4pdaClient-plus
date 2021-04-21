package org.softeg.slartus.forpdaplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 31.05.13
 * Time: 17:53
 * To change this template use File | Settings | File Templates.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            App.resStartNotifierServices();
        }
    }
}
