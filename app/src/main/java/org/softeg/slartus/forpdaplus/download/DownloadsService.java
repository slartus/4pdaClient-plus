package org.softeg.slartus.forpdaplus.download;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ActionSelectDialogFragment;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.lang.ref.WeakReference;
import java.net.HttpCookie;

import ru.slartus.http.Http;

public class DownloadsService {

    public static void download(final Activity context1, final String url, final Boolean finish) {
        ActionSelectDialogFragment.INSTANCE.execute(context1,
                context1.getString(R.string.download_method),
                "file.downloaderManagers",
                context1.getResources().getTextArray(R.array.downloaderManagersArray),
                context1.getResources().getTextArray(R.array.downloaderManagersValues),
                value -> {
                    try {
                        // внешний
                        if (value != null && "2".equals(value.toString())) {
                            new GetTempUrlTask(context1, uri -> {
                                try {
                                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    context1.startActivity(marketIntent);
                                    if (finish)
                                        context1.finish();
                                } catch (Throwable e) {
                                    AppLog.e(context1, e);
                                }
                            })
                                    .execute(url);
                        } else {
                            if (ContextCompat.checkSelfPermission(context1, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                Toast.makeText(context1, R.string.no_permission, Toast.LENGTH_SHORT).show();
                            else
                                new GetTempUrlTask(context1, uri -> {
                                    try {
                                        systemDownload(context1, FileUtils.getFileNameFromUrl(url), uri.toString());
                                        if (finish)
                                            context1.finish();
                                    } catch (Throwable e) {
                                        AppLog.e(context1, e);
                                    }
                                })
                                        .execute(url);
                        }
                    } catch (Throwable ex) {
                        AppLog.e(context1, ex);
                    }
                }, context1.getString(R.string.download_method_notify)
        );
    }


    private static void systemDownload(Context context, String fileName, String url) {
        StringBuilder cookiesString = new StringBuilder();
        for (HttpCookie cookie : Client.getInstance().getCookies()) {
            cookiesString.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(IntentService.DOWNLOAD_SERVICE);
        assert dm != null;

        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(url))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .addRequestHeader("Cookie", cookiesString.toString())
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.allowScanningByMediaScanner();

        dm.enqueue(request);
    }

    private static class GetTempUrlTask extends AsyncTask<String, Void, Uri> {


        private final MaterialDialog dialog;

        private final WeakReference<Context> m_Context;
        private final onOpenUrlInterface openUrlAction;

        public interface onOpenUrlInterface {
            void open(Uri uri);
        }

        GetTempUrlTask(Context context, onOpenUrlInterface openUrlAction) {
            m_Context = new WeakReference<>(context);
            this.openUrlAction = openUrlAction;

            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.request_link)
                    .build();
        }

        @Override
        protected Uri doInBackground(String... params) {
            try {
                String url = params[0];
                String fileUrl = Http.Companion.getInstance().performGetRedirectUrlElseRequestUrl(url);
                return Uri.parse(fileUrl);
            } catch (Throwable e) {
                ex = e;
                return null;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            try {
                this.dialog.show();
            } catch (Throwable ignored) {

            }
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Uri uri) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Throwable ignored) {

            }

            if (uri != null) {
                openUrlAction.open(uri);

            } else {
                if (ex != null)
                    AppLog.e(m_Context.get(), ex);
                else
                    Toast.makeText(m_Context.get(), R.string.unknown_error,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

}
