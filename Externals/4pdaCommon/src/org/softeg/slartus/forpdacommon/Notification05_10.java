package org.softeg.slartus.forpdacommon;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;

/**
 * Created by slinkin on 13.06.13.
 */
public class Notification05_10 extends NotificationBridge {
    Notification mNotification;

    public Notification05_10(Context context, int icon, CharSequence tickerText, long when) {
        super(context);
        mNotification = new Notification(icon, tickerText, when);
    }

    private CharSequence mContentTitle = null;
    private CharSequence mContentText = null;
    private PendingIntent mPendingIntent = null;

    @Override
    public NotificationBridge setContentTitle(CharSequence contentTitle) {
        mContentTitle = contentTitle;
        mNotification.setLatestEventInfo(mContext, mContentTitle, mContentText, mPendingIntent);
        return this;
    }

    @Override
    public NotificationBridge setContentText(CharSequence contentText) {
        mContentText = contentText;
        mNotification.setLatestEventInfo(mContext, mContentTitle, mContentText, mPendingIntent);
        return this;
    }

    @Override
    public NotificationBridge setContentIntent(PendingIntent pendingIntent) {
        mPendingIntent = pendingIntent;
        mNotification.setLatestEventInfo(mContext, mContentTitle, mContentText, mPendingIntent);
        return this;
    }

    @Override
    public NotificationBridge setDefaults(int defaults) {
        mNotification.defaults = defaults;
        return this;
    }

    @Override
    public NotificationBridge setAutoCancel(boolean autoCancel) {
        if (autoCancel)
            mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        else
            mNotification.flags &= Notification.FLAG_AUTO_CANCEL;
        return this;
    }

    @Override
    public NotificationBridge setProgress(int max, int progress, boolean indeterminate){
        return this;
    }

    @Override
    public Notification createNotification() {
        return mNotification;
    }
}
