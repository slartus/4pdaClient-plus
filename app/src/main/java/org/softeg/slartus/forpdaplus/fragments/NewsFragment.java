package org.softeg.slartus.forpdaplus.fragments;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.FragmentActivity;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.History;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.video.PlayerActivity;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 17.10.15.
 */
public class NewsFragment extends WebViewFragment implements IBrickFragment,MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static Context mContext;
    private static final String URL_KEY = "Url";
    private static final String TAG = "NewsActivity";
    private Handler mHandler = new Handler();
    private AdvWebView webView;
    private RelativeLayout pnlSearch;
    private FloatingActionButton fabComment;

    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    private EditText txtSearch;
    private String m_NewsUrl;
    public static String s_NewsUrl = null;
    private Uri m_Data = null;
    private ArrayList<History> m_History = new ArrayList<>();
    private boolean pencil;
    private boolean loadImages;

    View view;

    public static NewsFragment newInstance(Context context, String url){
        mContext = context;
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }
    public android.support.v4.app.FragmentActivity getContext(){
        return getActivity();
    }
    private View findViewById(int id){
        return view.findViewById(id);
    }
    public View getView(){
        return view;
    }
    public ActionBar getSupportActionBar(){
        return ((AppCompatActivity)getContext()).getSupportActionBar();
    }

    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }

    @Override
    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.news_activity, container, false);

        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getContext(), "Режим разработчика", Toast.LENGTH_SHORT).show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fabComment = (FloatingActionButton) findViewById(R.id.fab);
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
        loadImages = webView.getSettings().getLoadsImagesAutomatically();
        webView.setActionBarheight(getSupportActionBar().getHeight());
        setHideFab(fabComment);

        //webView.setWebChromeClient(new MyWebChromeClient());
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
        /*Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        assert intent != null;

        Bundle extras = intent.getExtras();

        assert extras != null;
*/
        m_NewsUrl = getArguments().getString(URL_KEY);
        s_NewsUrl = m_NewsUrl;



        fabComment.setColorNormal(App.getInstance().getColorAccent("Accent"));
        fabComment.setColorPressed(App.getInstance().getColorAccent("Pressed"));
        fabComment.setColorRipple(App.getInstance().getColorAccent("Pressed"));
        if(Client.getInstance().getLogined()&!PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("pancilInActionBar", false)){
            fabComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    respond();
                }
            });
        }else{
            fabComment.setVisibility(View.GONE);
        }
        //getContext().setWebView(webView);
        return view;
    }


    //@Override
    protected boolean isTransluent() {
        return true;
    }

    protected void afterCreate() {
        //getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }


    private final static int FILECHOOSER_RESULTCODE = 1;

    @JavascriptInterface
    public void showChooseCssDialog() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getContext(), ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getContext(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            webView.evalJs("window['HtmlInParseLessContent']('" + cssData + "');");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //finish();
            //onBackPressed();
            return true;
        }

        return true;
    }
/*
    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }
*/

    public void refresh() {
        showNews(m_NewsUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
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
    public Window getWindow() {
        return null;
    }

    //@Override
    public String Prefix() {
        return "news";
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        getActivity().setContentView(R.layout.main);
//        WebView wb = (WebView) findViewById(R.id.webview);
//        initWebView();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    @Override
    public String getListName() {
        return null;
    }

    @Override
    public String getListTitle() {
        return null;
    }

    @Override
    public void loadData(boolean isRefresh) {

    }

    @Override
    public void startLoad() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            //getContext().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


            //getContext().setProgressBarIndeterminateVisibility(false);
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
                PlayerActivity.showYoutubeChoiceDialog(getContext(), url);
                return true;
            }

            IntentActivity.tryShowUrl(getContext(), mHandler, url, true, false);

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
/*
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }
    */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default:
                ExtUrl.showSelectActionDialog(mHandler, getContext(),
                        m_Title, "", hitTestResult.getExtra(), "", "", "", "", "");
        }
    }

    @Override
    public boolean onBackPressed() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return true;
        }
        if (!m_History.isEmpty()) {
            m_FromHistory = true;
            History history = m_History.get(m_History.size() - 1);
            m_History.remove(m_History.size() - 1);
            m_ScrollX = history.scrollX;
            m_ScrollY = history.scrollY;
            showNews(history.url);
            return true;
        } else {
            return false;
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
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
        });

    }

    private void showNews(String url) {
        webView.setWebViewClient(new MyWebViewClient());
        saveHistory(url);
        m_NewsUrl = url;
        closeSearch();
        GetNewsTask getThemeTask = new GetNewsTask(getContext());
        getThemeTask.execute(url.replace("|", ""));

    }

    private void showThemeBody(String body) {
        try {

            getActivity().setTitle(m_Title);
            webView.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);
            for(int i = 0; i <= App.getInstance().getTabItems().size()-1; i++){
                if(App.getInstance().getTabItems().get(i).getTag().equals(getTag())) {
                    App.getInstance().getTabItems().get(i).setTitle(m_Title);
                    App.getInstance().getTabItems().get(i).setUrl(getUrl());
                    TabDrawerMenu.notifyDataSetChanged();
                    return;
                }
            }

        } catch (Exception ex) {
            AppLog.e(getContext(), ex);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item;
        boolean pencil = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("pancilInActionBar", false);
        if(Client.getInstance().getLogined()&pencil){
            item = menu.add("Комментировать").setIcon(R.drawable.ic_pencil_white_24dp);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    respond();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        item = menu.add(R.string.Refresh).setIcon(R.drawable.ic_refresh_white_24dp);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                refresh();
                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        item = menu.add(R.string.Like).setIcon(R.drawable.ic_thumb_up_white_24dp
                //        MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark
        );
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                like();
                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        SubMenu optionsMenu = menu.addSubMenu("Настройки");
        optionsMenu.getItem().setIcon(R.drawable.ic_settings_white_24dp);
        optionsMenu.getItem().setTitle(R.string.Settings);
        optionsMenu.add("Скрывать верхнюю панель")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Preferences.setHideActionBar(!Preferences.isHideActionBar());
                        setHideActionBar();
                        menuItem.setChecked(Preferences.isHideActionBar());
                        return true;
                    }
                }).setCheckable(true).setChecked(Preferences.isHideActionBar());
        if(!pencil) {
            optionsMenu.add("Скрывать карандаш")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setHideFab(!Preferences.isHideFab());
                            setHideActionBar();
                            menuItem.setChecked(Preferences.isHideFab());
                            return true;
                        }
                    }).setCheckable(true).setChecked(Preferences.isHideFab());
        }
        optionsMenu.add("Размер шрифта")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showFontSizeDialog();
                        return true;
                    }
                });

        optionsMenu.add(R.string.LoadImages)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Boolean loadImagesAutomatically1 = getWebView().getSettings().getLoadsImagesAutomatically();
                        getWebView().getSettings().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                        menuItem.setChecked(!loadImagesAutomatically1);
                        return true;
                    }
                }).setCheckable(true).setChecked(getWebView().getSettings().getLoadsImagesAutomatically());

        ExtUrl.addUrlSubMenu(new Handler(), getActivity(), menu, getUrl(), null, null);

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
/*
        item = menu.add(R.string.Close).setIcon(R.drawable.ic_close_white_24dp);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                getActivity().finish();
                return true;
            }
        });
        */


    }

    private String m_Title = "Новости";

    public void saveHtml() {
        try {
            webView.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SaveHtml(getContext(), html, "News");
            }
        });
    }

    private void like() {
        Toast.makeText(getContext(), "Запрос отправлен", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "Ошибка запроса", Toast.LENGTH_SHORT).show();
                                AppLog.e(getContext(), finalEx);
                            } else {
                                Toast.makeText(getContext(), "Запрос выполнен", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            AppLog.e(getContext(), ex);
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
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/forum/css/youtube_video.css\" type=\"text/css\" />\n");
        }
    }

    private class GetNewsTask extends AsyncTask<String, String, Boolean> {
        private final MaterialDialog dialog;
        public String Comment = null;
        public String ReplyId;
        public String Dp;

        public GetNewsTask(Context context) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .content("Загрузка новости")
                    .build();
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
            Matcher matcher = PatternExtensions.compile("<h1 itemprop=\"name\">([\\s\\S]*?)<\\/h1>").matcher(body);
            Matcher matchert = PatternExtensions.compile("<script type=\".*\">wrs([\\s\\S]*?)<.script>").matcher(body);
            m_Title = "Новости";
            if (matcher.find()) {
                m_Title = Html.fromHtml(matcher.group(1)).toString();
            }
            builder.beginHtml(m_Title);
            builder.beginBody("news",null,loadImages);
            builder.append("<div style=\"padding-top:" + builder.getMarginTop() + "px\"/>\n");
            builder.append("<div id=\"main\">");
            builder.append("<script type=\"text/javascript\" async=\"async\" src=\"file:///android_asset/forum/js/jqp.min.js\"></script>\n");
            builder.append("<script type=\"text/javascript\" async=\"async\" src=\"file:///android_asset/forum/js/site.min.js\"></script>\n");
            builder.append("<script type=\"text/javascript\">(function(f,h){var c=\"$4\";if(\"function\"!=typeof f[c]||.3>f[c].lib4PDA){var g={},b=function(){return f[c]||this},k=function(a,d){return function(){\"function\"==typeof d&&(!a||a in b?d(b[a],a):!g[a]&&(g[a]=[d])||g[a].push(d))}};b.fn=b.prototype={lib4PDA:.3,constructor:b,addModule:function(a,d){if(!(a in b)){b[a]=b.fn[a]=d?\"function\"==typeof d?d(b):d:h;for(var c=0,e=g[a];e&&c<e.length;)e[c++](b[a],a);delete g[a]}return b},onInit:function(a,d){for(var c=(a=(a+\"\").split(\" \")).length,e=d;c;)e=new k(a[--c],\n" +
                    "e);e();return b}};f[c]=f.lib4PDA=b;for(c in b.fn)b[c]=b.fn[c]}})(window);(function(a){var wrsI=0;\n" +
                    "window.wrs=function(c,f){a.write('<div id=\"wrs-div'+wrsI+'\"></div>');var d=a.getElementById('wrs-div'+wrsI),i=setInterval(function(w){if(!c()){return;}clearInterval(i);w=a.write;a.write=function(t){d.innerHTML+=t};f();a.write=w},500);wrsI++}})(document);</script>");

            builder.append(parseBody(body));

            if (matchert.find()) {
                builder.append("<script type=\"text/javascript\">wrs"+matchert.group(1)+"</script>");
            }

            builder.append("</div>");
            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        private String parseBody(String body) {
            Matcher m = PatternExtensions.compile("<article id=\"content\"[\\s\\S]*?>([\\s\\S]*?)<aside id=\"sidebar\">").matcher(body);

            if (m.find()) {
                return normalizeCommentUrls(m.group(1)).replaceAll("<form[\\s\\S]*?/form>", "");
            }
            m = PatternExtensions
                    .compile("<div id=\"main\">([\\s\\S]*?)<form action=\"(http://4pda.ru)?/wp-comments-post.php\" method=\"post\" id=\"commentform\">")
                    .matcher(body);
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
            if(PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("loadNewsComment", false)){
                body = body.replaceAll("(<div class=\"comment-box\" id=\"comments\">[\\s\\S]*?<ul class=\"page-nav box\">[\\s\\S]*?<\\/ul>)", "");
            }

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
                    dialog.setContent(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.show();
            } catch (Exception ex) {
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
                Log.e(TAG, ex.toString());
            }

            if (isCancelled()) return;
            if (success) {
                showThemeBody(m_ThemeBody);

            } else {
                getContext().setTitle(ex.getMessage());
                webView.loadDataWithBaseURL("\"file:///android_asset/\"", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(getContext(), ex);
            }
        }


    }

    public void respond() {
        respond("0", "0", null);
    }

    public void respond(final String replyId, final String dp, String user) {


        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.news_comment_edit, null);

        assert layout != null;
        final EditText message_edit = (EditText) layout.findViewById(R.id.comment);
        if (user != null)
            message_edit.setText("<b>" + URLDecoder.decode(user) + ",</b>");
        new MaterialDialog.Builder(getContext())
                .title(R.string.LeaveComment)
                .customView(layout,true)
                .positiveText(R.string.Send)
                .negativeText("Отмена")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String message = message_edit.getText().toString();
                        if (TextUtils.isEmpty(message.trim())) {
                            Toast.makeText(getContext(), "Текст не можут быть пустым!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        GetNewsTask getThemeTask = new GetNewsTask(getContext());
                        getThemeTask.Comment = message;
                        getThemeTask.ReplyId = replyId;
                        getThemeTask.Dp = dp;
                        getThemeTask.execute(m_NewsUrl);
                    }
                })
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView!=null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    webView.onPause();
                }
            }, 1500);
            webView.setWebViewClient(null);
        }
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
