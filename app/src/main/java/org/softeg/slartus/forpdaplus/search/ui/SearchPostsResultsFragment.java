package org.softeg.slartus.forpdaplus.search.ui;/*
 * Created by slinkin on 29.04.2014.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import org.softeg.slartus.forpdaplus.BaseFragment;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.IWebViewContainer;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.search.ISearchResultView;
import org.softeg.slartus.forpdaplus.search.SearchPostsParser;
import org.softeg.slartus.forpdaplus.search.SearchResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchPostsResultsFragment extends BaseFragment implements IWebViewContainer, ISearchResultView {
    private Handler mHandler = new Handler();
    private AdvWebView mWvBody;
    private static final String SEARCH_URL_KEY = "SEARCH_URL_KEY";
    private WebViewExternals m_WebViewExternals;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private MaterialDialog progressDialog;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        progressDialog = new MaterialDialog.Builder(getContext()).progress(true,0).content("Загрузка...").build();
    }

    public static Fragment newFragment(String searchUrl) {
        SearchPostsResultsFragment fragment = new SearchPostsResultsFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_URL_KEY, searchUrl);
        fragment.setArguments(args);
        return fragment;
    }

    private final static int FILECHOOSER_RESULTCODE = 1;

    @JavascriptInterface
    public void showChooseCssDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getActivity(), ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getActivity(), data.getData());
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
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        FloatingActionButton fab = (FloatingActionButton) ((ActionBarActivity) getActivity()).findViewById(R.id.fab);
        if (fab == null) return;
        if (actionBar == null) return;
        BrowserViewsFragmentActivity.setHideActionBar(mWvBody, actionBar, fab);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHideActionBar();

        search(0);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = mWvBody.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    public android.view.View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.search_posts_result, container, false);
        assert v != null;
        mWvBody = (AdvWebView) v.findViewById(R.id.body_webview);
        v.findViewById(R.id.btnUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBtnUpClick(view);
            }
        });
        v.findViewById(R.id.btnDown).setOnClickListener(new View.OnClickListener() {
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
        return v;
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
            if (getActivity() == null) return;
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

    private Context getContext() {
        return getActivity();
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        return m_WebViewExternals.dispatchKeyEvent(event);
    }

    private void showHtmlBody(String body) {
        try {

            mWvBody.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);


        } catch (Exception ex) {
            AppLog.e(getContext(), ex);
        }
    }

    @JavascriptInterface
    public void showUserMenu(final String userId, final String userNick) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ForumUser.showUserQuickAction(getActivity(), getWebView(), userId, userNick);
            }
        });
    }

    @JavascriptInterface
    public void nextPage() {
        getActivity().runOnUiThread(new Runnable() {
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search((m_SearchResult.getCurrentPage() - 2) * m_SearchResult.getPostsPerPageCount(getSearchQuery()));
            }
        });
    }

    @JavascriptInterface
    public void firstPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search(0);
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search((m_SearchResult.getPagesCount() - 1) * m_SearchResult.getPostsPerPageCount(getSearchQuery()));
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        getActivity().runOnUiThread(new Runnable() {
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

    public WebView getWebView() {
        return mWvBody;
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

    public void showLinkMenu(String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, getContext(), link);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler mHandler) {
        final WebView.HitTestResult hitTestResult = getWebView().getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
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

                    method.invoke(getActivity(), parameterValues);
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
        if (Preferences.System.isDeveloper()) {
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
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                FileOutputStream outputStream;

                try {
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        Toast.makeText(getContext(), "Внешнее хранилище недоступно!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    File file = new File(App.getInstance().getExternalFilesDir(null), "search.txt");
                    FileWriter out = new FileWriter(file);
                    out.write(html);
                    out.close();
                    Uri uri = Uri.fromFile(file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "text/plain");
                    startActivity(intent);
                } catch (Exception e) {
                    AppLog.e(getActivity(), e);
                }
            }
        });
    }

    private void saveHtml() {
        try {
            mWvBody.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
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
