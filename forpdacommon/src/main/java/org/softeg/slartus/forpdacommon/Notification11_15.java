package org.softeg.slartus.forpdacommon;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;


/*
 * Created by slinkin on 13.06.13.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Notification11_15 extends NotificationBridge {
    protected Notification.Builder mBuilder;

    public Notification11_15(Context context, int icon, CharSequence tickerText, long when) {
        super(context);
        mBuilder = new Notification.Builder(context);

        setSmallIcon(icon);
        setTicker(tickerText);
        setWhen(when);
    }

    @Override
    public NotificationBridge setSmallIcon(int smallIcon) {
        mBuilder.setSmallIcon(smallIcon);
        return this;
    }

    @Override
    public NotificationBridge setTicker(CharSequence tickerText) {
        mBuilder.setTicker(tickerText);
        return this;
    }

    @Override
    public NotificationBridge setWhen(long when) {
        mBuilder.setWhen(when);
        return this;
    }

    @Override
    public NotificationBridge setContentTitle(CharSequence contentTitle) {
        mBuilder.setContentTitle(contentTitle);
        return this;
    }

    @Override
    public NotificationBridge setContentText(CharSequence contentText) {
        mBuilder.setContentText(contentText);
        return this;
    }

    @Override
    public NotificationBridge setContentIntent(PendingIntent pendingIntent) {
        mBuilder.setContentIntent(pendingIntent);
        return this;
    }

    @Override
    public NotificationBridge setDefaults(int defaults) {
        mBuilder.setDefaults(defaults);
        return this;
    }

    @Override
    public NotificationBridge setAutoCancel(boolean autoCancel) {
        mBuilder.setAutoCancel(autoCancel);
        return this;
    }


    @Override
    public NotificationBridge setProgress(int max, int progress, boolean indeterminate) {
        mBuilder.setProgress(max, progress, indeterminate);
        return this;
    }

    @Override
    public Notification createNotification() {
        return mBuilder.getNotification();
    }

    @Override
    public NotificationBridge setSound(Uri sound) {
        mBuilder.setSound(sound);
        return this;
    }

    @Override
    public NotificationBridge setSound(Uri sound, int streamType) {
        mBuilder.setSound(sound, streamType);
        return this;
    }


}
