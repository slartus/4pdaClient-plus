package org.softeg.slartus.forpdaplus.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.regex.Matcher;

/**
 * Created by radiationx on 22.12.15.
 */
public class SpecialView extends WebViewFragment {
    private AdvWebView m_WebView;
    private AsyncTask asyncTask;
    public static String m_Title = "ForPDA";
    public static String m_Url = "";

    @Override
    public boolean closeTab() {
        return false;
    }

    @Override
    public AdvWebView getWebView() {
        return m_WebView;
    }

    @Override
    public WebViewClient getWebViewClient() {
        return new MyWebViewClient();
    }

    @Override
    public String getTitle() {
        return m_Title;
    }

    @Override
    public String getUrl() {
        return m_Url;
    }

    @Override
    public String Prefix() {
        return "special";
    }

    @Override
    public void reload() {
        asyncTask = new LoadRulesTask().execute();
    }

    @Override
    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static void showSpecial(String url) {
        m_Url = url.trim();
        MainActivity.addTab(m_Title, m_Url, new SpecialView());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.webview_fragment, container, false);
        m_WebView = (AdvWebView) findViewById(R.id.wvBody);


        initSwipeRefreshLayout();
        assert view != null;
        registerForContextMenu(m_WebView);

        m_WebView.getSettings();
        m_WebView.getSettings().setDomStorageEnabled(true);
        m_WebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        m_WebView.getSettings().setAppCachePath(getMainActivity().getApplicationContext().getCacheDir().getAbsolutePath());
        m_WebView.getSettings().setAppCacheEnabled(true);

        m_WebView.getSettings().setAllowFileAccess(true);

        m_WebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        m_WebView.addJavascriptInterface(this, "HTMLOUT");
        m_WebView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        m_WebView.setWebViewClient(new MyWebViewClient());

        asyncTask = new LoadRulesTask().execute();

        return view;
    }

    private class LoadRulesTask extends AsyncTask<String, String, Boolean> {

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;

                m_ThemeBody = Client.getInstance().performGet(m_Url).getResponseBody();

                Matcher matcher = PatternExtensions.compile("<title>([\\S\\s]*?)</title>").matcher(m_ThemeBody);
                if (matcher.find())
                    m_Title = Html.fromHtml(matcher.group(1)).toString();
                return true;
            } catch (Throwable e) {
                return false;
            }
        }


        protected void onPreExecute() {
            setLoading(true);
        }


        protected void onPostExecute(final Boolean success) {
            setLoading(false);

            if (isCancelled()) return;

            if (success) {
                showThemeBody(m_ThemeBody);
            } else {
                m_WebView.loadDataWithBaseURL("https://" + App.Host + "/forum/", m_ThemeBody, "text/html", "UTF-8", null);
            }
        }
    }

    private void showThemeBody(String body) {
        try {
            setTitle(m_Title);
            m_WebView.loadDataWithBaseURL("https://" + App.Host + ".ru/forum/", body, "text/html", "UTF-8", null);
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            IntentActivity.tryShowUrl(getMainActivity(), new Handler(), url, true, false);
            return true;
        }

    }
}
