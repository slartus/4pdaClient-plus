package org.softeg.browser.pageviewcontrol;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;

import org.softeg.slartus.yarportal.preferences.AppPreferences;

/*
 * Created by slinkin on 02.10.2014.
 */
public class AppWebView extends ScollWebView {
    private static final String TAG = "AppWebView";

    public AppWebView(Context context) {
        super(context);

    }

    public AppWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

    }



    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String js) {
        try {
            if (Build.VERSION.SDK_INT >= 19) {
                evaluateJavascript(js,null);
            } else {
                loadUrl("javascript:" + js);
            }
        } catch (IllegalStateException ex) {
            android.util.Log.e("AdvWebView", ex.toString());
        } catch (Throwable ex) {
            android.util.Log.e("AdvWebView", ex.toString());
        }
    }

    public void initTopicPageWebView() {
        // gd = new GestureDetector(context, sogl);
        getSettings().setJavaScriptEnabled(true);

        getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        getSettings().setDomStorageEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        if (Build.VERSION.SDK_INT > 15) {
            getSettings().setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
            getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT < 18)
            getSettings().setPluginState(WebSettings.PluginState.ON);// для воспроизведения видео


        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setScrollbarFadingEnabled(false);
        getSettings().setLoadWithOverviewMode(false);
        getSettings().setUseWideViewPort(true);
        getSettings().setDefaultFontSize(AppPreferences.getWebViewFontSize());
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable e) {
                android.util.Log.e(TAG, e.getMessage());
            }
        }
    }

    public void scrollTo(String fragment) {
        evalJs("scrollToElement('" + fragment + "');");
    }


}
