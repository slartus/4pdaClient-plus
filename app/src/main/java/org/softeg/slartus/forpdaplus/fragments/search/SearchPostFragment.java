package org.softeg.slartus.forpdaplus.fragments.search;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.hosthelper.HostHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.slartus.http.AppResponse;

/**
 * Created by radiationx on 15.11.15.
 */
public class SearchPostFragment extends WebViewFragment implements ISearchResultView {
    private final Handler mHandler = new Handler();
    private AdvWebView mWvBody;
    private static final String SEARCH_URL_KEY = "SEARCH_URL_KEY";
    private WebViewExternals m_WebViewExternals;
    private Bundle args;
    private FrameLayout buttonsPanel;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
    }

    public static Fragment newFragment(String searchUrl) {
        SearchPostFragment fragment = new SearchPostFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, searchUrl);
        fragment.setArguments(args);
        return fragment;
    }

    private final static int FILECHOOSER_RESULTCODE = 1;

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showChooseCssDialog() {
        getMainActivity().runOnUiThread(() -> {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");

                // intent.setDataAndType(Uri.parseCount("file://" + lastSelectDirPath), "file/*");
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);

            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getMainActivity(), R.string.no_app_for_get_file, Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                AppLog.e(getMainActivity(), ex);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getMainActivity(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            if (Build.VERSION.SDK_INT < 19)
                mWvBody.loadUrl("javascript:window['HtmlInParseLessContent']('" + cssData + "');");
            else
                mWvBody.evaluateJavascript("window['HtmlInParseLessContent']('" + cssData + "')",
                        s -> {

                        }
                );
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setHideActionBar();

        search(0);

    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_posts_result, container, false);
        initSwipeRefreshLayout();
        assert view != null;
        mWvBody = (AdvWebView) findViewById(R.id.body_webview);
        findViewById(R.id.btnUp).setOnClickListener(this::onBtnUpClick);
        findViewById(R.id.btnDown).setOnClickListener(this::onBtnDownClick);
        m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(App.getInstance().getPreferences());
        configWebView();
        m_WebViewExternals.setWebViewSettings();

        mWvBody.getSettings().setLoadWithOverviewMode(false);
        mWvBody.getSettings().setUseWideViewPort(true);
        mWvBody.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        mWvBody.addJavascriptInterface(this, "HTMLOUT");
        mWvBody.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">" +
                "</head><body bgcolor=" + AppTheme.getCurrentBackgroundColorHtml() + "></body></html>", "text/html", "UTF-8", null);
        registerForContextMenu(mWvBody);
        buttonsPanel = (FrameLayout) findViewById(R.id.buttonsPanel);
        return view;
    }

    @Override
    public String getResultView() {
        return SearchSettings.RESULT_VIEW_POSTS;
    }

    @Override
    public void search(String searchQuery) {
        if (args == null)
            args = new Bundle();
        args.putString(SEARCH_URL_KEY, searchQuery);
        search(0);
    }

    LoadResultTask mTask;

    public void search(final int startNum) {
        Runnable runnable = () -> {

            mTask = new LoadResultTask(startNum);
            mTask.execute();
        };
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED)
            mTask.cancel(false);
        else {
            runnable.run();
        }

    }

    public Context getContext() {
        return getMainActivity();
    }

    private void configWebView() {

        mWvBody.getSettings().setJavaScriptEnabled(true);
        mWvBody.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        mWvBody.getSettings().setDomStorageEnabled(true);
        mWvBody.getSettings().setAllowFileAccess(true);

        if (App.getInstance().getPreferences().getBoolean("system.WebViewScroll", true)) {
            mWvBody.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mWvBody.setScrollbarFadingEnabled(false);
        }

        m_WebViewExternals.setWebViewSettings();
        mWvBody.setWebViewClient(new MyWebViewClient());
        mWvBody.addJavascriptInterface(this, "HTMLOUT");
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler mHandler) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return m_WebViewExternals.dispatchKeyEvent(event);
    }

    private void showHtmlBody(String body) {
        try {
            MainActivity.searchSettings = SearchSettings.parse(getSearchQuery());
            mWvBody.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", body, "text/html", "UTF-8", null);
            if (buttonsPanel.getTranslationY() != 0)
                ViewPropertyAnimator.animate(buttonsPanel)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(500)
                        .translationY(0);
        } catch (Exception ex) {
            AppLog.e(getContext(), ex);
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showUserMenu(final String userId, final String userNick) {
        getMainActivity().runOnUiThread(() -> ForumUser.showUserQuickAction(getMainActivity(), userId, userNick));
    }

    @JavascriptInterface
    public void nextPage() {
        getMainActivity().runOnUiThread(() -> search(m_SearchResult.getCurrentPage() * m_SearchResult.getPostsPerPageCount(getSearchQuery())));
    }

    private String getSearchQuery() {
        return args.getString(SEARCH_URL_KEY);
    }

    @JavascriptInterface
    public void prevPage() {
        getMainActivity().runOnUiThread(() -> search((m_SearchResult.getCurrentPage() - 2) * m_SearchResult.getPostsPerPageCount(getSearchQuery())));
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void firstPage() {
        getMainActivity().runOnUiThread(() -> search(0));
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void lastPage() {
        getMainActivity().runOnUiThread(() -> search((m_SearchResult.getPagesCount() - 1) * m_SearchResult.getPostsPerPageCount(getSearchQuery())));
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void jumpToPage() {
        getMainActivity().runOnUiThread(() -> {
            CharSequence[] pages = new CharSequence[m_SearchResult.getPagesCount()];

            final int postsPerPage;

            postsPerPage = m_SearchResult.getPostsPerPageCount(getSearchQuery());

            final String page = getContext().getString(R.string.page_short);
            for (int p = 0; p < m_SearchResult.getPagesCount(); p++) {
                pages[p] = page + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
            }

            new MaterialDialog.Builder(getContext())
                    .title(R.string.jump_to_page)
                    .items(pages)
                    .itemsCallbackSingleChoice(m_SearchResult.getCurrentPage() - 1, (dialog, view1, i, pages1) -> {
                        search(i * postsPerPage);
                        return true; // allow selection
                    })
                    .show();
        });
    }

    public String Prefix() {
        return "theme";
    }

    @Override
    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(() -> new SaveHtml(getMainActivity(), html, "search"));
    }

    public AdvWebView getWebView() {
        return mWvBody;
    }

    @Override
    public WebViewClient getWebViewClient() {
        return new MyWebViewClient();
    }

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.search);
    }

    @Override
    public String getUrl() {
        return getSearchQuery();
    }

    @Override
    public void reload() {
        search(0);
    }

    @Override
    public AsyncTask getAsyncTask() {
        return null;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    public Window getWindow() {
        assert getContext() != null;
        return ((Activity) getContext()).getWindow();
    }

    public ActionBar getSupportActionBar() {
        return null;
    }

    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return false;
    }

    public void onBtnUpClick(View view) {
        mWvBody.pageUp(true);
    }

    public void onBtnDownClick(View view) {
        mWvBody.pageDown(true);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            if (url.contains("HTMLOUT.ru")) {
                Uri uri = Uri.parse(url);
                try {
                    String function = uri.getPathSegments().get(0);
                    String query = uri.getQuery();
                    Class[] parameterTypes = null;
                    String[] parameterValues = new String[0];
                    if (!TextUtils.isEmpty(query)) {
                        Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(query);
                        ArrayList<String> objs = new ArrayList<>();

                        while (m.find()) {
                            objs.add(m.group(2));
                        }
                        parameterValues = new String[objs.size()];
                        parameterTypes = new Class[objs.size()];
                        for (int i = 0; i < objs.size(); i++) {
                            parameterTypes[i] = String.class;
                            parameterValues[i] = objs.get(i);
                        }
                    }
                    Method method = this.getClass().getMethod(function, parameterTypes);

                    method.invoke(getMainActivity(), parameterValues);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            IntentActivity.tryShowUrl((Activity) getContext(), mHandler, url, true, false);

            return true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.link)
                .setOnMenuItemClickListener(menuItem -> {
                    ExtUrl.showSelectActionDialog(getMainActivity(), getString(R.string.link), getSearchQuery());
                    return true;
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.searchSettings = SearchSettings.parse(getSearchQuery());
    }

    private SearchResult m_SearchResult;

    private class LoadResultTask extends AsyncTask<String, String, Boolean> {
        private final int m_Page;

        LoadResultTask(int page) {
            m_Page = page;

        }

        private AppResponse response;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (this.isCancelled()) return false;

                response = Client.getInstance().preformGetWithProgress(getSearchQuery() + "&st=" + m_Page, null);
                SearchPostsParser searchPostsParser = new SearchPostsParser();
                response.setResponseBody(searchPostsParser.parse(response));
                m_SearchResult = searchPostsParser.searchResult;
                return true;

            } catch (Throwable e) {
                //Log.e(getContext(), e);
                ex = e;
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setLoading(true);
        }

        private Throwable ex;

        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onPostExecute(final Boolean success) {
            setLoading(false);
            if (response != null)
                showHtmlBody(response.getResponseBody());

            if (ex != null)
                AppLog.e(getContext(), ex);

            super.onPostExecute(success);
        }
    }
}
