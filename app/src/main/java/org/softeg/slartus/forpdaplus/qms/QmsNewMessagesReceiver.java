package org.softeg.slartus.forpdaplus.qms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;
import org.softeg.slartus.forpdaplus.Client;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 29.05.13
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
public class QmsNewMessagesReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int newCount = intent.getIntExtra(QmsNotifier.UNREAD_MESSAGE_USERS_COUNT_KEY, 0);

        Client.getInstance().setQmsCount(newCount);
        Client.getInstance().doOnMailListener();

//        if(intent.getBooleanExtra("HasUnreadMessage",false))
//            playNotification(context);
    }

    private void playNotification(Context context) {
        try {

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
        }
    }
}
