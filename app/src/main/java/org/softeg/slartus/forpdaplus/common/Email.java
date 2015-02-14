package org.softeg.slartus.forpdaplus.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.R;


/**
 * Created by slinkin on 26.06.13.
 */
public class Email {
    public static final String EMAIL = "slartus+4pda@gmail.com";

    public static void send(final Context context, final CharSequence subject,
                            final CharSequence body) {
        send(context, subject, body, null);
    }

    public static void send(final Context context, final CharSequence subject,
                            final CharSequence body, final String attachFilePath) {
        final Handler transThreadHandler = new Handler();
        Thread th = new Thread(new Runnable() {
            public void run() {
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                if (!TextUtils.isEmpty(attachFilePath))
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + attachFilePath));
                transThreadHandler.post(new Runnable() {
                    public void run() {
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.SendBy_)));
                    }
                });
            }
        });
        th.start();
    }
}
