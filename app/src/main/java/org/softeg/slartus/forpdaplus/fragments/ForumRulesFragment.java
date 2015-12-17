package org.softeg.slartus.forpdaplus.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.util.regex.Matcher;

/**
 * Created by radiationx on 06.12.15.
 */
public class ForumRulesFragment extends WebViewFragment{
    AdvWebView m_WebView;
    AsyncTask asyncTask;
    public final static String m_Title = "Правила форума";
    @Override
    public Menu getMenu() {
        return null;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    @Override
    public AdvWebView getWebView() {
        return m_WebView;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public WebViewClient MyWebViewClient() {
        return new MyWebViewClient();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String Prefix() {
        return "forum_rules";
    }

    @Override
    public void reload() {
        LoadRulesTask task = new LoadRulesTask();
        task.execute("".replace("|", ""));
        asyncTask = task;
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
        removeArrow();
    }

    public static void showRules() {
        MainActivity.addTab(m_Title, "RULES", new ForumRulesFragment());
    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.webview_fragment, container, false);
        m_WebView = (AdvWebView) view.findViewById(R.id.wvBody);
        setHasOptionsMenu(true);


        initSwipeRefreshLayout();
        setHasOptionsMenu(true);
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

        LoadRulesTask task = new LoadRulesTask();
        task.execute("".replace("|", ""));
        asyncTask = task;

        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getMainActivity(), "Режим разработчика", Toast.LENGTH_SHORT).show();

        return view;
    }

    private class LoadRulesTask extends AsyncTask<String, String, Boolean> {

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.getInstance();
                m_ThemeBody = transformBody(client.performGet("http://4pda.ru/forum/index.php?act=boardrules"));

                return true;
            } catch (Throwable e) {
                return false;
            }
        }

        private String transformBody(String body) {
            HtmlBuilder builder = new HtmlBuilder();
            builder.beginHtml(m_Title);
            builder.beginBody("rules");

            builder.append("<div class=\"posts_list\"><div class=\"post_container\"><div class=\"post_body \">");
            builder.append(Jsoup.parse(body).select(".tablepad").first().html());
            builder.append("</div></div></div>");

            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        protected void onPreExecute() {
            setLoading(true);
        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            setLoading(false);

            if (isCancelled()) return;

            if (success) {
                showThemeBody(m_ThemeBody);
            } else {
                getSupportActionBar().setTitle(ex.getMessage());
                m_WebView.loadDataWithBaseURL("\"file:///android_asset/\"", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(getMainActivity(), ex);
            }
        }
    }
    private void showThemeBody(String body) {
        try {
            getMainActivity().setTitle(m_Title);
            m_WebView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);
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

































