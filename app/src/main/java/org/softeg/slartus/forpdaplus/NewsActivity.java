package org.softeg.slartus.forpdaplus;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpdaplus.classes.History;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.video.PlayerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 07.12.11
 * Time: 8:07
 */
public class NewsActivity extends BrowserViewsFragmentActivity
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String URL_KEY = "Url";
    private Handler mHandler = new Handler();
    private AdvWebView webView;
    private RelativeLayout pnlSearch;

    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    private EditText txtSearch;
    private String m_NewsUrl;
    public static String s_NewsUrl = null;
    private Uri m_Data = null;
    private ArrayList<History> m_History = new ArrayList<>();

    public static void shownews(Context context, String url) {
        Intent intent = new Intent(context, NewsActivity.class);
        intent.putExtra(NewsActivity.URL_KEY, url);

        context.startActivity(intent);
    }

    @Override
    protected boolean isTransluent() {
        return true;
    }

    protected void afterCreate() {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.news_activity);

        if (Preferences.System.isDeveloper())
            Toast.makeText(this, "Режим разработчика", Toast.LENGTH_SHORT).show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        webView = (AdvWebView) findViewById(R.id.wvBody);
        registerForContextMenu(webView);
        setWebViewSettings();
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultFontSize(Preferences.News.getFontSize());
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable ignore) {

            }
        }
        webView.setActionBarheight(getActionBar().getHeight());
        setHideActionBar();

        webView.setWebChromeClient(new MyWebChromeClient());
        pnlSearch = (RelativeLayout) findViewById(R.id.pnlSearch);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        ImageButton btnPrevSearch = (ImageButton) findViewById(R.id.btnPrevSearch);
        btnPrevSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(false);
            }
        });
        ImageButton btnNextSearch = (ImageButton) findViewById(R.id.btnNextSearch);
        btnNextSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(true);
            }
        });
        ImageButton btnCloseSearch = (ImageButton) findViewById(R.id.btnCloseSearch);
        btnCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSearch();
            }
        });

        webView.setWebViewClient(new MyWebViewClient());
        webView.addJavascriptInterface(this, "HTMLOUT");
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        assert intent != null;
        Bundle extras = intent.getExtras();

        assert extras != null;
        m_NewsUrl = extras.getString(URL_KEY);
        s_NewsUrl = m_NewsUrl;

    }

    private final static int FILECHOOSER_RESULTCODE = 1;

    @JavascriptInterface
    public void showChooseCssDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(NewsActivity.this, "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(NewsActivity.this, ex);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(this, data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            webView.evalJs("window['HtmlInParseLessContent']('" + cssData + "');");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        }

        return true;
    }

    public ImageButton getFullScreenButton() {
        return (ImageButton) findViewById(R.id.btnFullScreen);
    }

    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }

    protected void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MenuFragment mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();

    }

    public void refresh() {
        showNews(m_NewsUrl);
    }

    @Override
    public void onResume() {
        super.onResume();

        webView.setWebViewClient(new MyWebViewClient());
        if (s_NewsUrl != null) {
            s_NewsUrl = null;
            showNews(m_NewsUrl);
        }

        if (m_Data != null) {
            String url = m_Data.toString();
            m_Data = null;
            if (IntentActivity.isNews(url)) {
                showNews(url);
            }
        }
    }

    public WebView getWebView() {
        return webView;
    }

    @Override
    public String Prefix() {
        return "news";
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        setContentView(R.layout.main);
//        WebView wb = (WebView) findViewById(R.id.webview);
//        initWebView();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            super.onShowCustomView(view, callback);
            if (view instanceof FrameLayout) {
                FrameLayout frame = (FrameLayout) view;
                if (frame.getFocusedChild() instanceof VideoView) {
                    VideoView video = (VideoView) frame.getFocusedChild();
                    frame.removeView(video);
                    NewsActivity.this.setContentView(video);
                    video.setOnCompletionListener(NewsActivity.this);
                    video.setOnErrorListener(NewsActivity.this);
                    video.start();
                }
            }
        }
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            NewsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


            NewsActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            m_ScrollY = 0;
            m_ScrollX = 0;

            if (isReplyUrl(url))
                return true;

            if (isAnchor(url)) {
                showAnchor(url);
                return true;
            }

            if (IntentActivity.isNews(url)) {
                showNews(url);
                return true;
            }

            if (IntentActivity.isYoutube(url)) {
                PlayerActivity.showYoutubeChoiceDialog(NewsActivity.this, url);
                return true;
            }

            IntentActivity.tryShowUrl(NewsActivity.this, mHandler, url, true, false);

            return true;
        }
    }


    private Boolean isReplyUrl(String url) {
        Matcher m = Pattern.compile("http://4pdaservice.org/(\\d+)/(\\d+)").matcher(url);
        if (m.find()) {
            respond(m.group(1), m.group(2), null);
            return true;
        }

        if (Pattern.compile("http://4pdaservice.org/#commentform").matcher(url).find()) {
            respond();
            return true;
        }

        return false;
    }

    private String getPostId() {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/(\\d+)");
        Matcher m = pattern.matcher(m_NewsUrl);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static Boolean isAnchor(String url) {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+/*#.*");
        return pattern.matcher(url).find();
    }

    private void showAnchor(String url) {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+/*#(.*)");
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            webView.loadUrl("javascript:scrollToElement('" + m.group(1) + "')");
        }
    }

    public boolean onSearchRequested() {
        pnlSearch.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default:
                ExtUrl.showSelectActionDialog(mHandler, NewsActivity.this,
                        m_Title, "", hitTestResult.getExtra(), "", "", "", "", "");
        }
    }


    @Override
    public void onBackPressed() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return;
        }

        if (!m_History.isEmpty()) {
            m_FromHistory = true;
            History history = m_History.get(m_History.size() - 1);
            m_History.remove(m_History.size() - 1);
            m_ScrollX = history.scrollX;
            m_ScrollY = history.scrollY;
            showNews(history.url);
        } else {

            super.onBackPressed();
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);

        }
    }

    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        webView.findAll(query);
        try {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(webView, true);
        } catch (Throwable ignored) {
        }
        onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(new Runnable() {
            public void run() {
                webView.findAll("");
                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, false);
                } catch (Throwable ignored) {
                }

                pnlSearch.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
        });

    }

    private void showNews(String url) {
        webView.setWebViewClient(new MyWebViewClient());
        saveHistory(url);
        m_NewsUrl = url;
        closeSearch();
        GetNewsTask getThemeTask = new GetNewsTask(this);
        getThemeTask.execute(url.replace("|", ""));
    }

    private void showThemeBody(String body) {
        try {

            setTitle(m_Title);
            webView.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);


        } catch (Exception ex) {
            AppLog.e(this, ex);
        }
    }

    private void saveHistory(String nextUrl) {
        if (m_FromHistory) {
            m_FromHistory = false;
            return;
        }
//        URI redirectUrl = Client.getInstance().getRedirectUri();
//        if (redirectUrl != null)
//            m_History.add(redirectUrl.toString());
//        else
        if (m_NewsUrl != null && !TextUtils.isEmpty(m_NewsUrl) && !m_NewsUrl.equals(nextUrl)) {
            History history = new History();
            history.url = m_NewsUrl;
            history.scrollX = m_ScrollX;
            history.scrollY = m_ScrollY;
            m_History.add(history);
        }
    }



    private String m_Title = "Новости";

    public static final class MenuFragment extends Fragment {


        public MenuFragment() {
            super();
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        private Boolean m_FirstTime = true;

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            if (!m_FirstTime)
                getInterface().onPrepareOptionsMenu();
            m_FirstTime = false;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem item;

            item = menu.add(R.string.DoComment).setIcon(R.drawable.ic_menu_comment);
            item.setVisible(Client.getInstance().getLogined());
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ((NewsActivity) getActivity()).respond();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add(R.string.Refresh).setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ((NewsActivity) getActivity()).refresh();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add(R.string.Like).setIcon(R.drawable.ic_menu_thumb_up
                    //        MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark
            );
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ((NewsActivity) getActivity()).like();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


            SubMenu optionsMenu = menu.addSubMenu("Настройки");
            optionsMenu.getItem().setIcon(R.drawable.ic_menu_preferences);
            optionsMenu.getItem().setTitle(R.string.Settings);
            optionsMenu.add("Скрывать верхнюю панель")
                    .setIcon(R.drawable.ic_menu_images).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Preferences.setHideActionBar(!Preferences.isHideActionBar());
                    getInterface().setHideActionBar();
                    menuItem.setChecked(Preferences.isHideActionBar());
                    return true;
                }
            }).setCheckable(true).setChecked(Preferences.isHideActionBar());
            optionsMenu.add("Размер шрифта")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            getInterface().showFontSizeDialog();
                            return true;
                        }
                    });

            optionsMenu.add(R.string.LoadImages).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Boolean loadImagesAutomatically1 = getInterface().getWebView().getSettings().getLoadsImagesAutomatically();
                    getInterface().getWebView().getSettings().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                    menuItem.setChecked(!loadImagesAutomatically1);
                    return true;
                }
            }).setCheckable(true).setChecked(getInterface().getWebView().getSettings().getLoadsImagesAutomatically());

            ExtUrl.addUrlSubMenu(new Handler(), getActivity(), menu,
                    ((NewsActivity) getActivity()).getUrl(), null, null);

            if (Preferences.System.isDeveloper()) {
                menu.add("Сохранить страницу").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            getInterface().saveHtml();
                        } catch (Exception ex) {
                            return false;
                        }
                        return true;
                    }
                });
            }

            item = menu.add(R.string.Close).setIcon(R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    getActivity().finish();
                    return true;
                }
            });


        }

        public NewsActivity getInterface() {
            return (NewsActivity) getActivity();
        }
    }

    public void saveHtml() {
        try {
            webView.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(this, ex);
        }
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                FileOutputStream outputStream;

                try {
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        Toast.makeText(getContext(), "Внешнее хранилище недоступно!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    File file = new File(App.getInstance().getExternalFilesDir(null), "news.txt");
                    FileWriter out = new FileWriter(file);
                    out.write(html);
                    out.close();
                    Uri uri = Uri.fromFile(file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "text/plain");
                    startActivity(intent);
                } catch (Exception e) {
                    AppLog.e(NewsActivity.this, e);
                }
            }
        });
    }

    private void like() {
        Toast.makeText(this, "Запрос отправлен", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {

                Exception ex = null;


                try {
                    Client.getInstance().likeNews(getPostId());
                } catch (Exception e) {
                    ex = e;
                }

                final Exception finalEx = ex;
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if (finalEx != null) {
                                Toast.makeText(NewsActivity.this, "Ошибка запроса", Toast.LENGTH_SHORT).show();
                                AppLog.e(NewsActivity.this, finalEx);
                            } else {
                                Toast.makeText(NewsActivity.this, "Запрос выполнен", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            AppLog.e(NewsActivity.this, ex);
                        }
                    }
                });
            }
        }).start();
    }

    private String getUrl() {
        return m_NewsUrl;
    }

    private class NewsHtmlBuilder extends HtmlBuilder {
        @Override
        public void addStyleSheetLink(StringBuilder sb) {
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://" + getStyle() + "\" />\n");
            sb.append("<link rel=\"stylesheet\" href=\"file:///android_asset/forum/css/youtube_video.css\" type=\"text/css\" />\n");
        }
    }

    private class GetNewsTask extends AsyncTask<String, String, Boolean> {
        private final ProgressDialog dialog;
        public String Comment = null;
        public String ReplyId;
        public String Dp;

        public GetNewsTask(Context context) {
            dialog = new AppProgressDialog(context);
        }

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.getInstance();
                if (TextUtils.isEmpty(Comment))
                    m_ThemeBody = transformBody(client.performGet(m_NewsUrl));
                else {
                    Map<String, String> additionalHeaders = new HashMap<>();
                    additionalHeaders.put("comment", Comment);
                    additionalHeaders.put("comment_post_ID", getPostId());
                    additionalHeaders.put("submit", "Отправить комментарий");
                    additionalHeaders.put("comment_reply_ID", ReplyId);
                    additionalHeaders.put("comment_reply_dp", Dp);
                    m_ThemeBody = transformBody(client.performPost("http://4pda.ru/wp-comments-post.php", additionalHeaders, "UTF-8"));


                }
                return true;
            } catch (Throwable e) {
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }
        }

        private String transformBody(String body) {
            NewsHtmlBuilder builder = new NewsHtmlBuilder();
            Matcher matcher = PatternExtensions.compile("<title>([^<>]*)</title>").matcher(body);
            m_Title = "Новости";
            if (matcher.find()) {
                m_Title = Html.fromHtml(matcher.group(1)).toString();
            }
            builder.beginHtml(m_Title);
            builder.beginBody();
            builder.append("<div style=\"margin-top:72pt\"/>\n");
            builder.append("<div id=\"main\">");

            builder.append(parseBody(body));

            builder.append("</div><br/><br/><br/><br/>");
            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        private String parseBody(String body) {
            Matcher m = PatternExtensions.compile("<article id=\"content\" class=\"\">([\\s\\S]*?)<aside id=\"sidebar\">").matcher(body);
            if (m.find()) {
                return normalizeCommentUrls(m.group(1)).replaceAll("<form[\\s\\S]*?/form>", "");
            }
            m = PatternExtensions.compile("<div id=\"main\">([\\s\\S]*?)<form action=\"(http://4pda.ru)?/wp-comments-post.php\" method=\"post\" id=\"commentform\">").matcher(body);
            if (m.find()) {
                return normalizeCommentUrls(m.group(1)) + getNavi(body);
            }
            m = PatternExtensions.compile("<div id=\"main\">([\\s\\S]*?)<div id=\"categories\">").matcher(body);
            if (m.find()) {
                return normalizeCommentUrls(m.group(1)) + getNavi(body);
            }

            return normalizeCommentUrls(body);
        }


        private String getNavi(String body) {
            String navi = "<P></P><div class=\"navigation\"><div>";

            Matcher matcher = Pattern.compile("<a href=\"/(\\w+)/newer/(\\d+)/\" rel=\"next\">").matcher(body);
            if (matcher.find()) {
                navi += "<a href=\"http://4pda.ru/" + matcher.group(1) + "/newer/" + matcher.group(2) + "/\" rel=\"next\">&#8592;&nbsp;Назад</a> ";
            }

            matcher = Pattern.compile("&nbsp;<a href=\"/(\\w+)/older/(\\d+)/\" rel=\"prev\">").matcher(body);
            if (matcher.find()) {
                navi += "&nbsp;<a href=\"http://4pda.ru/" + matcher.group(1) + "/older/" + matcher.group(2) + "/\" rel=\"prev\">Вперед&nbsp;&#8594;</a> ";
            }
            return navi + "</div/div>";
        }


        private String normalizeCommentUrls(String body) {
            body = Pattern.compile("<iframe[^><]*?src=\"http://www.youtube.com/embed/([^\"/]*)\".*?(?:</iframe>|/>)", Pattern.CASE_INSENSITIVE)
                    .matcher(body)
                    .replaceAll("<a class=\"video-thumb-wrapper\" href=\"http://www.youtube.com/watch?v=$1\"><img class=\"video-thumb\" width=\"480\" height=\"320\" src=\"http://img.youtube.com/vi/$1/0.jpg\"/></a>");
            return body
                    .replaceAll("<div id=\"comment-form-reply-\\d+\"><a href=\"#\" data-callfn=\"commentform_move\" data-comment=\"(\\d+)\">ответить</a></div></div><ul class=\"comment-list level-(\\d+)\">"
                            , "<div id=\"comment-form-reply-$1\"><a href=\"http://4pdaservice.org/$1/$2\">ответить</a></div></div><ul class=\"comment-list level-$2\">")
                    .replace("href=\"/", "href=\"http://4pda.ru/")
                    .replace("href=\"#commentform\"", "href=\"http://4pdaservice.org/#commentform")
                    ;
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    dialog.setMessage(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка новости...");
                this.dialog.show();
            } catch (Exception ex) {
                AppLog.e(null, ex);
                this.cancel(true);
            }
        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            Comment = null;
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                AppLog.e(NewsActivity.this, ex);
            }

            if (isCancelled()) return;
            if (success) {
                showThemeBody(m_ThemeBody);

            } else {
                NewsActivity.this.setTitle(ex.getMessage());
                webView.loadDataWithBaseURL("\"file:///android_asset/\"", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(NewsActivity.this, ex);
            }
        }


    }

    public void respond() {
        respond("0", "0", null);
    }

    public void respond(final String replyId, final String dp, String user) {


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.news_comment_edit, null);

        assert layout != null;
        final EditText message_edit = (EditText) layout.findViewById(R.id.comment);
        if (user != null)
            message_edit.setText("<b>" + URLDecoder.decode(user) + ",</b>");
        new AlertDialogBuilder(this)
                .setTitle(R.string.LeaveComment)
                .setView(layout)
                .setPositiveButton(R.string.Send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        String message = message_edit.getText().toString();
                        if (TextUtils.isEmpty(message.trim())) {
                            Toast.makeText(NewsActivity.this, "Текст не можут быть пустым!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        GetNewsTask getThemeTask = new GetNewsTask(NewsActivity.this);
                        getThemeTask.Comment = message;
                        getThemeTask.ReplyId = replyId;
                        getThemeTask.Dp = dp;
                        getThemeTask.execute(m_NewsUrl);

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            // останавливаем всопроизведение видео
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(getWebView(), (Object[]) null);

        } catch (Throwable ignored) {

        }
        getWebView().setWebViewClient(null);

    }


    @Override
    public void onStop() {
        super.onStop();

        webView.setWebViewClient(null);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        webView.setWebViewClient(null);

    }
}
