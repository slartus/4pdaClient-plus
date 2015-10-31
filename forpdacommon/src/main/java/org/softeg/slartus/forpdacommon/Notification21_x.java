package org.softeg.slartus.forpdacommon;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

/**
 * Created by radiationx on 18.06.15.
 */
public class Notification21_x extends Notification11_15{

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Notification21_x(Context context, int icon, CharSequence tickerText, long when) {
        super(context, icon, tickerText, when);
        mBuilder.setStyle(new Notification.InboxStyle());
        mBuilder.setColor(Color.argb(255,2,119,189));
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Notification createNotification() {
        return mBuilder.build();
    }
}