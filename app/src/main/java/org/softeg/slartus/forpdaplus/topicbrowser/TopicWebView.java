package org.softeg.slartus.forpdaplus.topicbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import org.softeg.slartus.forpdaplus.App;

import java.util.Calendar;

/**
 * User: slinkin
 * Date: 20.09.12
 * Time: 8:48
 */
public class TopicWebView extends WebView {
    public static final String SCROLL_Y_KEY = "SCROLL_Y_KEY";
    private int actionBarHeight;

    public TopicWebView(Context context) {
        super(context);
        init(context);
    }

    public TopicWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public TopicWebView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }


    public void evalJs(String js) {
        if (Build.VERSION.SDK_INT < 19)
            loadUrl("javascript:" + js);
        else
            evaluateJavascript(js, null);
    }

    private void init(Context context) {
        LinearLayout v = new LinearLayout(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
        this.addView(v);
        // gd = new GestureDetector(context, sogl);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);


        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        if (prefs.getBoolean("system.WebViewScroll", true)) {
            setScrollbarFadingEnabled(false);
            setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        }

        setBackgroundColor(App.getInstance().getThemeStyleWebViewBackground());
        loadData("<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\"></head><body bgcolor="
                + App.getInstance().getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");
    }


    public void scrollTo(String fragment) {
        evalJs("goToAnchor('" + fragment + "');");
    }


    private OnScrollChangedCallback mOnScrollChangedCallback;


    public void setActionBarheight(int actionBarHeight) {
        this.actionBarHeight = Math.max(actionBarHeight, 72);
    }

    private int mRawY = 0;

    /**
     * @param l    Current horizontal scroll origin
     * @param t    Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (!m_ActionBarOnScrollEventsState) return;
        int k = t - oldt;

        mRawY = Math.min(actionBarHeight, k + mRawY);
        mRawY = Math.max(-actionBarHeight, mRawY);
        mRawY = Math.min(getScrollY(), mRawY);
        if (mRawY == actionBarHeight)
            mOnScrollChangedCallback.onScrollDown();
        else if (mRawY == -actionBarHeight || t <= actionBarHeight)
            mOnScrollChangedCallback.onScrollUp();
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        Boolean b = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final WebView.HitTestResult hitTestResult = getHitTestResult();
                if (hitTestResult == null) {
                    startClickTime = Calendar.getInstance().getTimeInMillis();
                } else
                    switch (hitTestResult.getType()) {
                        case WebView.HitTestResult.UNKNOWN_TYPE:
                        case WebView.HitTestResult.EDIT_TEXT_TYPE:
                            startClickTime = Calendar.getInstance().getTimeInMillis();
                    }

                break;
            }
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {

                    mOnScrollChangedCallback.onTouch();


                }
            }
        }
        return b;

    }

    private Boolean m_ActionBarOnScrollEventsState = true;

    public void offActionBarOnScrollEvents() {
        m_ActionBarOnScrollEventsState = false;
    }

    public void onActionBarOnScrollEvents() {
        m_ActionBarOnScrollEventsState = true;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public static interface OnScrollChangedCallback {
        public void onScrollDown();

        public void onScrollUp();

        public void onTouch();
    }
}
