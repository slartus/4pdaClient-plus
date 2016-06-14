package org.softeg.slartus.forpdacommon;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

/*
 * Created by radiationx on 18.06.15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Notification21_x extends Notification16_20 {
    public Notification21_x(Context context, int icon, CharSequence tickerText, long when) {
        super(context, icon, tickerText, when);

        mBuilder.setColor(Color.argb(255, 2, 119, 189));
    }

    @Override
    public Notification createNotification() {
        return mBuilder.build();
    }
}