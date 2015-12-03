package org.softeg.slartus.forpdaplus.fragments.search;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 15.11.15.
 */
public class SearchPostFragment extends WebViewFragment implements ISearchResultView {
    private Handler mHandler = new Handler();
    private AdvWebView mWvBody;
    private static final String SEARCH_URL_KEY = "SEARCH_URL_KEY";
    private WebViewExternals m_WebViewExternals;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private MaterialDialog progressDialog;
    private Bundle args;
    private View view;
    private Menu menu;

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getContext(), "Режим разработчика", Toast.LENGTH_SHORT).show();
        progressDialog = new MaterialDialog.Builder(getContext()).progress(true,0).content("Загрузка...").build();
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

    @JavascriptInterface
    public void showChooseCssDialog() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getMainActivity(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getMainActivity(), ex);
                }
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
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        }
                );
        }
    }

    public void setHideActionBar() {
        if (getWebView() == null || !(getWebView() instanceof AdvWebView))
            return;
        ActionBar actionBar = ((AppCompatActivity) getMainActivity()).getSupportActionBar();
        FloatingActionButton fab = (FloatingActionButton) ((AppCompatActivity) getMainActivity()).findViewById(R.id.fab);
        if (fab == null) return;
        if (actionBar == null) return;
        BrowserViewsFragmentActivity.setHideActionBar(mWvBody, actionBar, fab);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setHideActionBar();

        search(0);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_posts_result, container, false);
        assert view != null;
        mWvBody = (AdvWebView) view.findViewById(R.id.body_webview);
        view.findViewById(R.id.btnUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBtnUpClick(view);
            }
        });
        view.findViewById(R.id.btnDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBtnDownClick(view);
            }
        });
        m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(PreferenceManager.getDefaultSharedPreferences(App.getContext()));
        configWebView();
        m_WebViewExternals.setWebViewSettings();

        mWvBody.getSettings().setLoadWithOverviewMode(false);
        mWvBody.getSettings().setUseWideViewPort(true);
        mWvBody.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                mWvBody.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable ignore) {

            }
        }
        mWvBody.addJavascriptInterface(this, "HTMLOUT");
        mWvBody.loadDataWithBaseURL("http://4pda.ru/forum/", "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">" +
                "</head><body bgcolor=" + App.getInstance().getCurrentBackgroundColorHtml() + "></body></html>", "text/html", "UTF-8", null);
        registerForContextMenu(mWvBody);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = createSwipeRefreshLayout(getView());

    }

    protected SwipeRefreshLayout createSwipeRefreshLayout(View view) {
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.ptr_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                search(0);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(App.getInstance().getMainAccentColor());
        return swipeRefreshLayout;
    }

    protected void setLoading(final Boolean loading) {
        try {
            if (getMainActivity() == null) return;
            mSwipeRefreshLayout.setRefreshing(loading);
        } catch (Throwable ignore) {
            android.util.Log.e("TAG", ignore.toString());
        }
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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                mTask = new LoadResultTask(startNum);
                mTask.execute();
            }
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

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        if (prefs.getBoolean("system.WebViewScroll", true)) {
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
            mWvBody.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);
        } catch (Exception ex) {
            AppLog.e(getContext(), ex);
        }
    }

    @JavascriptInterface
    public void showUserMenu(final String userId, final String userNick) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ForumUser.showUserQuickAction(getMainActivity(), getWebView(), userId, userNick);
            }
        });
    }

    @JavascriptInterface
    public void nextPage() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search(m_SearchResult.getCurrentPage() * m_SearchResult.getPostsPerPageCount(getSearchQuery()));
            }
        });
    }

    private String getSearchQuery() {
        return args.getString(SEARCH_URL_KEY);
    }

    @JavascriptInterface
    public void prevPage() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search((m_SearchResult.getCurrentPage() - 2) * m_SearchResult.getPostsPerPageCount(getSearchQuery()));
            }
        });
    }

    @JavascriptInterface
    public void firstPage() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search(0);
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search((m_SearchResult.getPagesCount() - 1) * m_SearchResult.getPostsPerPageCount(getSearchQuery()));
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence[] pages = new CharSequence[m_SearchResult.getPagesCount()];

                final int postsPerPage;

                postsPerPage = m_SearchResult.getPostsPerPageCount(getSearchQuery());


                for (int p = 0; p < m_SearchResult.getPagesCount(); p++) {
                    pages[p] = "Стр. " + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
                }

                new MaterialDialog.Builder(getContext())
                        .title("Перейти к странице")
                        .items(pages)
                        .itemsCallbackSingleChoice(m_SearchResult.getCurrentPage() - 1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int i, CharSequence pages) {
                                search(i * postsPerPage);
                                return true; // allow selection
                            }
                        })
                        .show();
            }
        });
    }

    public String Prefix() {
        return "theme";
    }

    public AdvWebView getWebView() {
        return mWvBody;
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
        return "Поиск";
    }

    @Override
    public String getUrl() {
        return getSearchQuery();
    }

    @Override
    public void reload() {}

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
        if (Preferences.System.isDevSavePage()) {
            menu.add("Сохранить страницу").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        saveHtml();
                    } catch (Exception ex) {
                        return false;
                    }
                    return true;
                }


            });
        }
        ExtUrl.addUrlSubMenu(new Handler(), getMainActivity(), menu, getSearchQuery(), null, null);
        this.menu = menu;
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SaveHtml(getMainActivity(),html,"Search");
            }
        });
    }

    private void saveHtml() {
        try {
            mWvBody.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.searchSettings = SearchSettingsDialogFragment.createForumSearchSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.searchSettings = SearchSettings.parse(getSearchQuery());
    }

    private SearchResult m_SearchResult;

    private class LoadResultTask extends AsyncTask<String, String, Boolean> {
        private int m_Page;

        public LoadResultTask(int page) {
            m_Page = page;

        }

        private String pageBody;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (this.isCancelled()) return false;

                pageBody = Client.getInstance().loadPageAndCheckLogin(getSearchQuery() + "&st=" + m_Page, null);
                SearchPostsParser searchPostsParser = new SearchPostsParser();
                pageBody = searchPostsParser.parse(pageBody);
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
            progressDialog.show();
            setLoading(true);
        }

        private Throwable ex;

        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onPostExecute(final Boolean success) {
            setLoading(false);
            progressDialog.dismiss();
            showHtmlBody(pageBody);


            if (ex != null)
                AppLog.e(getContext(), ex);

            super.onPostExecute(success);
        }
    }
}
