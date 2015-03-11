package org.softeg.browser.pageviewcontrol.htmloutinterfaces;

import android.graphics.Picture;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.WebView;

import org.softeg.browser.pageviewcontrol.AppWebView;


/*
 * Created by slinkin on 06.10.2014.
 */
public class AppPictureListener implements WebView.PictureListener {
    Thread m_ScrollThread;
    private int mScrollY;
    private String mScrollElement;
    private Handler mHandler;
    private AppWebView mWebView;
    public static final int SCROLL_TO_BOTTOM=-999;
    private AppPictureListener(Handler handler,
                               AppWebView webView){
        mHandler = handler;
        mWebView = webView;
    }

    public AppPictureListener(Handler handler,AppWebView webView,String scrollElement,
                              int scrollY){
        this(handler,webView);
        mScrollElement = scrollElement;
        mScrollY = scrollY;
    }

    public void onNewPicture(WebView view, Picture arg1) {
        if (TextUtils.isEmpty(mScrollElement) && mScrollY == 0) {
            //webView.setPictureListener(null);
            return;
        }

        if (m_ScrollThread != null) return;

        m_ScrollThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    public void run() {
                        tryScrollToElement();
                    }
                });
            }
        });


        m_ScrollThread.start();
    }

    private void tryScrollToElement() {
        if (mScrollY == SCROLL_TO_BOTTOM) {
            mWebView.pageDown(true);
            mScrollY = 0;
        }
        else if (mScrollY != 0) {
            mWebView.scrollTo(0, mScrollY);
            mScrollY = 0;
        } else if (!TextUtils.isEmpty(mScrollElement)) {
            mWebView.offActionBarOnScrollEvents();
            mWebView.scrollTo(0, 100);
            mWebView.scrollTo(0, 0);
            mWebView.onActionBarOnScrollEvents();
            mWebView.evalJs("scrollToElement('" + mScrollElement + "');");
        }


        mScrollElement = null;
        mWebView.setPictureListener(null);
    }
}
