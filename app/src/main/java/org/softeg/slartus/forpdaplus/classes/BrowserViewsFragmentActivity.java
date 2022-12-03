package org.softeg.slartus.forpdaplus.classes;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BrowserViewsFragmentActivity extends AppCompatActivity implements IWebViewContainer {
    public abstract String Prefix();

    public abstract WebView getWebView();


    WebViewExternals m_WebViewExternals;

    public WebViewExternals getWebViewExternals() {
        if (m_WebViewExternals == null)
            m_WebViewExternals = new WebViewExternals(this);
        return m_WebViewExternals;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy()
    {
        WebView mWebView= getWebView();
        // null out before the super call
        if (mWebView != null)
        {
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.removeAllViews();
            mWebView.loadUrl("about:blank");
            mWebView = null;
        }
        super.onDestroy();
    }


    protected void setWebViewSettings(Boolean loadImagesAutomaticallyAlways) {
        getWebViewExternals().setWebViewSettings(loadImagesAutomaticallyAlways);

    }

    public void setWebViewSettings() {
        setWebViewSettings(false);
    }

    public void onPrepareOptionsMenu() {
        getWebViewExternals().onPrepareOptionsMenu();
    }

    /*@Override
    protected void loadPreferences(SharedPreferences prefs) {
        super.loadPreferences(prefs);
        getWebViewExternals().loadPreferences(prefs);

    }*/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return getWebViewExternals().dispatchKeyEvent(event);
    }

    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

}
