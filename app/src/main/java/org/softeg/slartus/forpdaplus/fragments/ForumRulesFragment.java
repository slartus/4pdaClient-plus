package org.softeg.slartus.forpdaplus.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.hosthelper.HostHelper;

/**
 * Created by radiationx on 06.12.15.
 */
public class ForumRulesFragment extends WebViewFragment {
    private AdvWebView m_WebView;
    private AsyncTask asyncTask;
    public String m_Title = App.getContext().getString(R.string.forum_rules);
    private String url = "https://" + App.Host + "/forum/index.php?act=boardrules";

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
        return url;
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
    }

    public static ForumRulesFragment newInstance(String url) {
        ForumRulesFragment fragment = new ForumRulesFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showRules() {
        MainActivity.addTab(App.getContext().getString(R.string.forum_rules), "RULES", new ForumRulesFragment());
    }

    public static void showRules(String url) {
        MainActivity.addTab(App.getContext().getString(R.string.forum_rules), url, newInstance(url));
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
        if (getArguments() != null)
            url = getArguments().getString("URL");

        asyncTask = new LoadRulesTask().execute();

        return view;
    }

    private class LoadRulesTask extends AsyncTask<String, String, Boolean> {

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.getInstance();
                m_ThemeBody = transformBody(client.performGet(url).getResponseBody());

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
            Document doc = Jsoup.parse(body);
            Element element = doc.select(".tablepad").first();
            if (element == null)
                element = doc.select(".postcolor").first();
            builder.append(element.html());
            m_Title = doc.select(".maintitle").first().text().trim();
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
                setTitle(m_Title);
                try {
                    showBody();
                } catch (Exception e) {
                    AppLog.e(e);
                }
            } else {
                getSupportActionBar().setTitle(ex.getMessage());
                m_WebView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(getMainActivity(), ex);
            }
        }
    }

    private void showThemeBody(String body) {
        try {
            setTitle(m_Title);
            m_WebView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", body, "text/html", "UTF-8", null);
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

    Menu menu;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.copy_link)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        StringUtils.copyToClipboard(getContext(), url);
                        Toast.makeText(getActivity(), R.string.link_copied_to_buffer, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
    }
}

































