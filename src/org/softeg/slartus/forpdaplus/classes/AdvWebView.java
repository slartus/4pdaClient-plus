package org.softeg.slartus.forpdaplus.classes;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.Calendar;

/**
 * User: slinkin
 * Date: 25.01.12
 * Time: 10:00
 */
public class AdvWebView extends WebView {
    private int actionBarHeight=72;

    public interface OnScrollChangedListener {
        void onScrollChanged(int x, int y, int oldx, int oldy);
    }

    private OnScrollChangedListener m_OnScrollListener;

    public void setOnScrollListener(OnScrollChangedListener scrollListener) {
        m_OnScrollListener = scrollListener;
    }

    public AdvWebView(Context context) {
        super(context);
        init(context);
    }

    public AdvWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }


    private void init(Context context) {
        // gd = new GestureDetector(context, sogl);
        getSettings().setJavaScriptEnabled(true);

        getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        getSettings().setDomStorageEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT > 15) {
            getSettings().setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
            getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT < 18)
            getSettings().setPluginState(WebSettings.PluginState.ON);// для воспроизведения видео


        if (Preferences.System.isShowWebViewScroll()) {
            setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            setScrollbarFadingEnabled(false);
        }

        setBackgroundColor(MyApp.getInstance().getThemeStyleWebViewBackground());
        loadData("<html><head></head><body bgcolor=" + MyApp.getInstance().getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");
    }

    private Point m_LastMotionEvent = null;

    public Point getLastMotionEvent() {
        return m_LastMotionEvent;
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
        if(mOnScrollChangedCallback==null)return;
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
        m_LastMotionEvent = new Point((int) event.getX(), (int) event.getY());
        if(mOnScrollChangedCallback==null)return b;
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

    public void evalJs(String js) {
        if (Build.VERSION.SDK_INT < 19)
            loadUrl("javascript:" + js);
        else
            evaluateJavascript(js, null);
    }
}
