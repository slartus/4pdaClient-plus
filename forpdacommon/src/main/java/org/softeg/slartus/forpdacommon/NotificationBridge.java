package org.softeg.slartus.forpdacommon;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 31.05.13
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class NotificationBridge {

    protected Context mContext;


    protected NotificationBridge(Context context){
        mContext = context;
    }

    public static NotificationBridge createBridge(Context context,int icon, CharSequence tickerText,
                                     long when){
        int sdk = Build.VERSION.SDK_INT;

        if(sdk<16)
            return new Notification11_15(context,icon,tickerText,when);
        if(sdk<21)
            return new Notification16_20(context,icon,tickerText,when);

        return new Notification21_x(context,icon,tickerText,when);
    }

    public  NotificationBridge setSmallIcon(int smallIcon){
        return this;
    }

    public  NotificationBridge setTicker(CharSequence tickerText){
        return this;
    }

    public  NotificationBridge setWhen(long when){
        return this;
    }

    public  NotificationBridge setContentTitle(CharSequence contentTitle){
        return this;
    }

    public  NotificationBridge setContentText(CharSequence contentText){
        return this;
    }

    public  NotificationBridge setContentIntent(PendingIntent pendingIntent){
        return this;
    }

    public  NotificationBridge setDefaults(int defaults){
        return this;
    }

    public  NotificationBridge setAutoCancel(boolean autoCancel){
        return this;
    }

    public NotificationBridge setProgress(int max, int progress, boolean indeterminate){
        return this;
    }

    public NotificationBridge setSound(Uri sound){
        return this;
    }

    public NotificationBridge setSound(Uri sound, int streamType){
        return this;
    }

    public abstract Notification createNotification();


//    public static Notification createNotification(Context context, int icon, CharSequence tickerText,
//                                                  long when,CharSequence contentTitle,
//                                                  CharSequence contentText, Intent contentIntent) {
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, 0);
//        Notification notification = null;
//        int sdk = Build.VERSION.SDK_INT;
//        if (sdk < 11) {
//            notification = new Notification(icon, tickerText, when);
//            notification.defaults = Notification.DEFAULT_ALL;
//            notification.flags |= Notification.FLAG_AUTO_CANCEL;
//            notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);
//
//        } else {
//            Notification.Builder builder = new Notification.Builder(context)
//                    .setSmallIcon(icon)
//                    .setTicker(tickerText)
//                    .setWhen(when)
//                    .setContentTitle(contentTitle)
//                    .setContentText(contentText)
//                    .setContentIntent(pendingIntent)
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setAutoCancel(true);
//            if (sdk < 16) {
//                notification = builder.getNotification();
//            } else {
//                notification = builder.build();
//            }
//
//        }
//
//
//        return notification;
//
//
//    }
}
