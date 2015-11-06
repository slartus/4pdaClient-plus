package org.softeg.slartus.forpdaplus.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotificationBridge;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.QuickStartActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.DownloadTask;
import org.softeg.slartus.forpdaplus.tabs.DownloadsTab;

import java.io.UnsupportedEncodingException;


/**
 * User: slinkin
 * Date: 30.07.12
 * Time: 10:31
 */
public class DownloadReceiver extends ResultReceiver {
    private Handler m_Handler;
    private Context m_Context;

    public DownloadReceiver(Handler handler, Context context) {
        super(handler);
        m_Handler = handler;
        m_Context = context;
    }
    private static int getNotificationIcon() {
        boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        return whiteIcon ? R.drawable.notify_icon : R.drawable.icon_mat;
    }
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode != DownloadsService.UPDATE_PROGRESS) return;
        int notificationId = resultData.getInt("downloadTaskId");

        final DownloadTask downloadTask = Client.getInstance().getDownloadTasks().getById(notificationId);

        final Context context = m_Context;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        switch (downloadTask.getState()) {
            case DownloadTask.STATE_ERROR:
                Intent i = new Intent(context, QuickStartActivity.class);

                Notification notif = null;
                notif = NotificationBridge.createBridge(
                        context,
                        getNotificationIcon(),
                        context.getString(R.string.DownloadAborted),
                        System.currentTimeMillis())
                        .setContentTitle("Не удалось загрузить")
                        .setContentText("Нажмите для дальнейших действий")
                        .setContentIntent(PendingIntent.getActivity(context, 0, i, 0))
                        .setAutoCancel(true)
                        .createNotification();

                mNotificationManager.notify(downloadTask.getUrl(), notificationId, notif);
                break;
            case DownloadTask.STATE_CANCELED: {
                Intent intent = new Intent(context, QuickStartActivity.class);
                intent.putExtra("template", DownloadsTab.TEMPLATE);

                Notification notification = null;
                try {
                    notification = NotificationBridge.createBridge(
                            context,
                            getNotificationIcon(),
                            context.getString(R.string.DownloadAborted),
                            System.currentTimeMillis())
                            .setContentTitle(downloadTask.getFileName())
                            .setContentText(DownloadTask.getStateMessage(downloadTask.getState(), downloadTask.getEx()))
                            .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                            .setAutoCancel(true)
                            .createNotification();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                mNotificationManager.notify(downloadTask.getUrl(), notificationId, notification);
                return;
            }
            case DownloadTask.STATE_SUCCESSFULL: {
                Intent intent = getRunFileIntent(downloadTask.getOutputFile());

                Notification notification = null;
                try {
                    notification = NotificationBridge.createBridge(
                            context,
                            getNotificationIcon(),
                            context.getString(R.string.DownloadComplete),
                            System.currentTimeMillis())
                            .setContentTitle(downloadTask.getFileName())
                            .setContentText(DownloadTask.getStateMessage(downloadTask.getState(), downloadTask.getEx()))
                            .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                            .setAutoCancel(true)
                            .createNotification();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                mNotificationManager.notify(downloadTask.getUrl(), notificationId, notification);

                m_Handler.post(new Runnable() {
                    public void run() {
                        try {
                            Toast.makeText(context, downloadTask.getFileName() + "\n" + context.getString(R.string.DownloadComplete), Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return;
            }
            default: {

                try {
                    showProgressNotification(context, notificationId, downloadTask.getFileName(), downloadTask.getPercents(), downloadTask.getUrl());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showProgressNotification(Context context, int notificationId, String title, int percents, String tag) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, QuickStartActivity.class);
        intent.putExtra("template", DownloadsTab.TEMPLATE);
        String contentText = percents + "%";
        NotificationBridge notificationBridge = NotificationBridge.createBridge(
                context,
                getNotificationIcon(),
                context.getString(R.string.DownloadFile),
                System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(contentText)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .setAutoCancel(true);


        Notification notification = null;
        
            notificationBridge.setProgress(100, percents, false);
            notification = notificationBridge.createNotification();
        

        mNotificationManager.notify(tag, notificationId, notification);
    }

    private Intent getRunFileIntent(String filePath) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(FileUtils.fileExt(filePath).substring(1));
        newIntent.setDataAndType(Uri.parse("file://" + filePath), mimeType);
        newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }
}
