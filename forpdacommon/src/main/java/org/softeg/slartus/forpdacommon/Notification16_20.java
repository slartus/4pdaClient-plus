package org.softeg.slartus.forpdacommon;

import android.annotation.TargetApi;

import android.app.Notification;
import android.content.Context;
import android.os.Build;


/*
 * Created by slinkin on 13.06.13.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Notification16_20 extends Notification11_15 {

    public Notification16_20(Context context, int icon, CharSequence tickerText, long when) {
        super(context, icon, tickerText, when);
        mBuilder.setStyle(new Notification.InboxStyle());
    }


    @Override
    public Notification createNotification() {
        return mBuilder.build();
    }
}
