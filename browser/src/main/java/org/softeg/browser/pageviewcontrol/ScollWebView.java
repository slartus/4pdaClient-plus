package org.softeg.browser.pageviewcontrol;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import org.softeg.slartus.yarportal.AppLog;

import java.util.Calendar;


/*
 * Created by slartus on 10.10.2014.
 */
public class ScollWebView extends WebView {
    private int actionBarHeight = 72;
    public ScollWebView(Context context) {
        super(context);
    }

    public ScollWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

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
        try {
            if (!m_ActionBarOnScrollEventsState) return;
            if (mOnScrollChangedCallback == null) return;
            int k = t - oldt;

            mRawY = Math.min(actionBarHeight, k + mRawY);
            mRawY = Math.max(-actionBarHeight, mRawY);
            mRawY = Math.min(getScrollY(), mRawY);
            if (mRawY == actionBarHeight)
                mOnScrollChangedCallback.onScrollDown(m_InTouch);
            else if (mRawY == -actionBarHeight || t <= actionBarHeight)
                mOnScrollChangedCallback.onScrollUp(m_InTouch);
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    private static final int MAX_CLICK_DURATION = 200;
    private static final int MAX_TOUCH__Y_DISTANCE = 30;
    private long startClickTime;
    private Boolean m_InTouch = false;
    private Point m_MotionDown = null;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Boolean b = super.onTouchEvent(event);
        try {

            if (mOnScrollChangedCallback == null) return b;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    m_MotionDown = new Point((int) event.getX(), (int) event.getY());
                    m_InTouch = true;
                    final HitTestResult hitTestResult = getHitTestResult();
                    if (hitTestResult == null) {
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                    } else
                        switch (hitTestResult.getType()) {
                            case HitTestResult.UNKNOWN_TYPE:
                            case HitTestResult.EDIT_TEXT_TYPE:
                                startClickTime = Calendar.getInstance().getTimeInMillis();
                        }

                    break;
                }
                case MotionEvent.ACTION_UP: {
                    m_InTouch = false;
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    if (clickDuration < MAX_CLICK_DURATION &&m_MotionDown!=null&& Math.abs(m_MotionDown.y - event.getY()) < MAX_TOUCH__Y_DISTANCE) {
                        mOnScrollChangedCallback.onTouch();
                    }
                }
            }
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }finally {
            m_LastMotionEvent = new Point((int) event.getX(), (int) event.getY());
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
        public void onScrollDown(Boolean inTouch);

        public void onScrollUp(Boolean inTouch);

        public void onTouch();
    }
}
