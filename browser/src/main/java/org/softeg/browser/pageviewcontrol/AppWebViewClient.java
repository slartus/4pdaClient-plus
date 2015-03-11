package org.softeg.browser.pageviewcontrol;

import android.content.Intent;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dmitriy.tarasov.android.intents.IntentUtils;

import org.softeg.slartus.yarportal.AppLog;
import org.softeg.slartus.yarportal.UrlManager;

import java.lang.ref.WeakReference;


/*
 * Created by slinkin on 02.10.2014.
 */
public class AppWebViewClient extends WebViewClient {
    private WeakReference<IWebViewClientListener> listener;

    public AppWebViewClient(IWebViewClientListener listener) {
        super();
        this.listener = new WeakReference<IWebViewClientListener>(listener);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        listener.get().setProgressBarIndeterminateVisibility(true);
        //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        view.clearHistory();
        listener.get().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final String url) {
        try {
            if (url != null && url.startsWith("file:///android_asset/webkit/"))
                return true;
            if (UrlManager.tryOpenUrl(listener.get().getContext(), url)) {
                return true;
            }

            Intent intent = IntentUtils.openLink(url);
            listener.get().getContext().startActivity(intent);
        } catch (Throwable ex) {
            AppLog.e(ex);
        }
        return true;
    }

}
