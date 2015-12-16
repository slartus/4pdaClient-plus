package org.softeg.slartus.forpdaplus.fragments;

import android.app.Activity;
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
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.softeg.slartus.forpdaapi.NewsApi;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.History;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.video.PlayerActivity;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 17.10.15.
 */
public class NewsFragment extends WebViewFragment implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String URL_KEY = "Url";
    private static final String TAG = "NewsActivity";
    private Handler mHandler = new Handler();
    private AdvWebView webView;

    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    private String m_NewsUrl;
    public static String s_NewsUrl = null;
    private Uri m_Data = null;
    private ArrayList<History> m_History = new ArrayList<>();
    private boolean loadImages;
    private String m_Title = "Новости";
    private View view;
    private Menu menu;

    public static NewsFragment newInstance(Context context, String url){
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }
    public View getView(){
        return view;
    }

    private View findViewById(int id){
        return getView().findViewById(id);
    }

    @Override
    public String getTitle() {
        return m_Title;
    }

    public String getUrl() {
        return m_NewsUrl;
    }

    @Override
    public void reload() {
        refresh();
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    @Override
    public WebViewClient MyWebViewClient() {
        return new MyWebViewClient();
    }

    public void refresh() {
        showNews(m_NewsUrl);
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    public AdvWebView getWebView() {
        return webView;
    }

    public String Prefix() {
        return "news";
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        getMainActivity().setContentView(R.layout.main);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.news_fragment, container, false);
        initSwipeRefreshLayout();
        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getMainActivity(), "Режим разработчика", Toast.LENGTH_SHORT).show();

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
        //m_WebView.setActionBarheight(getSupportActionBar().getHeight());


        webView.setWebViewClient(new MyWebViewClient());
        webView.addJavascriptInterface(this, "HTMLOUT");

        m_NewsUrl = getArguments().getString(URL_KEY);
        s_NewsUrl = m_NewsUrl;


        FloatingActionButton fabComment = (FloatingActionButton) findViewById(R.id.fab);
        setHideFab(fabComment);
        fabComment.setColorNormal(App.getInstance().getColorAccent("Accent"));
        fabComment.setColorPressed(App.getInstance().getColorAccent("Pressed"));
        fabComment.setColorRipple(App.getInstance().getColorAccent("Pressed"));
        if(Client.getInstance().getLogined()&!PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("pancilInActionBar", false))
            fabComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    respond();
                }
            });
        else
            fabComment.setVisibility(View.GONE);

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
        return view;
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
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

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
        /*optionsMenu.add("Скрывать верхнюю панель")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Preferences.setHideActionBar(!Preferences.isHideActionBar());
                        setHideActionBar();
                        menuItem.setChecked(Preferences.isHideActionBar());
                        return true;
                    }
                }).setCheckable(true).setChecked(Preferences.isHideActionBar());*/
        if(!pencil) {
            optionsMenu.add("Скрывать карандаш")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setHideFab(!Preferences.isHideFab());
                            setHideFab();
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

        ExtUrl.addUrlSubMenu(new Handler(), getMainActivity(), menu, getUrl(), null, null);

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
        this.menu = menu;
    }

    public void saveHtml() {
        try {
            webView.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SaveHtml(getMainActivity(), html, "News");
            }
        });
    }


    @Override
    public boolean onBackPressed() {
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
        getMainActivity();
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getMainActivity(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            webView.evalJs("window['HtmlInParseLessContent']('" + cssData + "');");
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
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
                PlayerActivity.showYoutubeChoiceDialog(getMainActivity(), url);
                return true;
            }

            IntentActivity.tryShowUrl(getMainActivity(), mHandler, url, true, false);

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

    private class NewsHtmlBuilder extends HtmlBuilder {
        @Override
        public void addStyleSheetLink(StringBuilder sb) {
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://").append(getStyle()).append("\" />\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/forum/css/youtube_video.css\" type=\"text/css\" />\n");
        }
    }

    AsyncTask asyncTask = null;

    @Override
    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    private void showNews(String url) {
        webView.setWebViewClient(new MyWebViewClient());
        saveHistory(url);
        m_NewsUrl = url;
        GetNewsTask getThemeTask = new GetNewsTask(getMainActivity());
        getThemeTask.execute(url.replace("|", ""));
        asyncTask = getThemeTask;
    }

    public void showBody(String body) {
        super.showBody();
        try {
            getMainActivity().setTitle(m_Title);
            webView.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private void saveHistory(String nextUrl) {
        if (m_FromHistory) {
            m_FromHistory = false;
            return;
        }
        if (m_NewsUrl != null && !TextUtils.isEmpty(m_NewsUrl) && !m_NewsUrl.equals(nextUrl)) {
            History history = new History();
            history.url = m_NewsUrl;
            history.scrollX = m_ScrollX;
            history.scrollY = m_ScrollY;
            m_History.add(history);
        }
    }

    private class GetNewsTask extends AsyncTask<String, String, Boolean> {

        public String Comment = null;
        public String ReplyId;
        public String Dp;

        public GetNewsTask(Context context) {}

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
            Matcher matchert = PatternExtensions.compile("<script[\\s\\S]*\\(([\\s\\S]*),[\\s\\S]*<.script>").matcher(body);
            m_Title = "Новости";
            if (matcher.find()) {
                m_Title = Html.fromHtml(matcher.group(1)).toString();
            }
            builder.beginHtml(m_Title);
            builder.beginBody("news", null, loadImages);
            builder.append("<div style=\"padding-top:" + HtmlBuilder.getMarginTop() + "px\"/>\n");
            builder.append("<div id=\"main\">");
            /*builder.append("<script type=\"text/javascript\" async=\"async\" src=\"file:///android_asset/forum/js/jqp.min.js\"></script>\n");
            builder.append("<script type=\"text/javascript\" async=\"async\" src=\"file:///android_asset/forum/js/site.min.js\"></script>\n");
            builder.append("<script type=\"text/javascript\">(function(f,h){var c=\"$4\";if(\"function\"!=typeof f[c]||.3>f[c].lib4PDA){var g={},b=function(){return f[c]||this},k=function(a,d){return function(){\"function\"==typeof d&&(!a||a in b?d(b[a],a):!g[a]&&(g[a]=[d])||g[a].push(d))}};b.fn=b.prototype={lib4PDA:.3,constructor:b,addModule:function(a,d){if(!(a in b)){b[a]=b.fn[a]=d?\"function\"==typeof d?d(b):d:h;for(var c=0,e=g[a];e&&c<e.length;)e[c++](b[a],a);delete g[a]}return b},onInit:function(a,d){for(var c=(a=(a+\"\").split(\" \")).length,e=d;c;)e=new k(a[--c],\n" +
                    "e);e();return b}};f[c]=f.lib4PDA=b;for(c in b.fn)b[c]=b.fn[c]}})(window);(function(a){var wrsI=0;\n" +
                    "window.wrs=function(c,f){a.write('<div id=\"wrs-div'+wrsI+'\"></div>');var d=a.getElementById('wrs-div'+wrsI),i=setInterval(function(w){if(!c()){return;}clearInterval(i);w=a.write;a.write=function(t){d.innerHTML+=t};f();a.write=w},500);wrsI++}})(document);</script>");
*/
            builder.append(parseBody(body));

            if (matchert.find()) {
                builder.append("<script type=\"text/javascript\">function getCommentsData(){return '"+matchert.group(1)+"';}</script>");
  ;
                builder.append("<script>\n" +
                        "    window.onload = function() {\n" +
                        "        var anchors = document.querySelectorAll('.karma');\n" +
                        "        var data = JSON.parse(getCommentsData())["+getPostId()+"];\n" +
                        "        for(var i = 0; i < anchors.length; i++) {\n" +
                        "            var found = anchors[i].getAttribute(\"data-karma\").match(/([\\d]*)-([\\d]*)/);\n" +
                        "            anchors[i].innerHTML= '<b class=\"icon-karma-up\" title=\"Мне нравится\" data-karma-act=\"1-264127-2745153\"></b><span class=\"num-wrap\"><span class=\"num\" title=\"Понравилось\"></span></span>';\n" +
                        "            anchors[i].querySelector(\".num-wrap .num\").innerHTML = data[found[2]][3];\n" +
                        "            anchors[i].onclick = function () {\n" +
                        "                found = this.getAttribute(\"data-karma\").match(/([\\d]*)-([\\d]*)/);\n" +
                        "                this.querySelector(\".num-wrap .num\").innerHTML = data[found[2]][3]+1;\n" +
                        "                HTMLOUT.likeComment(found[1],found[2]);\n" +
                        "            };\n" +
                        "        }\n" +
                        "    }\n" +
                        "</script>");
                if(Client.getInstance().getLogined()){
                    builder.append("<script>\n" +
                            "    window.onload = function() {\n" +
                            "        var anchors = document.querySelectorAll('.karma');\n" +
                            "        var data = JSON.parse(getCommentsData())["+getPostId()+"];\n" +
                            "        for(var i = 0; i < anchors.length; i++) {\n" +
                            "            var found = anchors[i].getAttribute(\"data-karma\").match(/([\\d]*)-([\\d]*)/);\n" +
                            "            anchors[i].innerHTML= '<b class=\"icon-karma-up\" title=\"Мне нравится\" data-karma-act=\"1-264127-2745153\"></b><span class=\"num-wrap\"><span class=\"num\" title=\"Понравилось\"></span></span>';\n" +
                            "            anchors[i].querySelector(\".num-wrap .num\").innerHTML = data[found[2]][3];\n" +
                            "            anchors[i].onclick = function () {\n" +
                            "                found = this.getAttribute(\"data-karma\").match(/([\\d]*)-([\\d]*)/);\n" +
                            "                this.querySelector(\".num-wrap .num\").innerHTML = data[found[2]][3]+1;\n" +
                            "                HTMLOUT.likeComment(found[1],found[2]);\n" +
                            "            };\n" +
                            "        }\n" +
                            "    }\n" +
                            "</script>");
                }else {
                    builder.append("<script>\n" +
                            "    window.onload = function() {\n" +
                            "        var anchors = document.querySelectorAll('.karma');\n" +
                            "        var data = JSON.parse(getCommentsData())["+getPostId()+"];\n" +
                            "        for(var i = 0; i < anchors.length; i++) {\n" +
                            "            var found = anchors[i].getAttribute(\"data-karma\").match(/([\\d]*)-([\\d]*)/);\n" +
                            "            anchors[i].innerHTML= '<b class=\"icon-karma-up\" title=\"Мне нравится\" data-karma-act=\"1-264127-2745153\"></b><span class=\"num-wrap\"><span class=\"num\" title=\"Понравилось\"></span></span>';\n" +
                            "            anchors[i].querySelector(\".num-wrap .num\").innerHTML = data[found[2]][3];\n" +
                            "        }\n" +
                            "    }\n" +
                            "</script>");
                }
            }

            builder.append("</div>");
            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        private String parseBody(String body) {
            Matcher m = PatternExtensions.compile("<article [\\s\\S]*?>([\\s\\S]*?)<aside [\\s\\S]*?>").matcher(body);

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
        protected void onPreExecute() {
            try {
                setLoading(true);
            } catch (Exception ex) {
                this.cancel(true);
            }
        }

        private Throwable ex;

        @Override
        protected void onPostExecute(final Boolean success) {
            Comment = null;
            setLoading(false);
            if (isCancelled()) return;
            if (success) {
                showBody(m_ThemeBody);

            } else {
                getMainActivity().setTitle(ex.getMessage());
                webView.loadDataWithBaseURL("\"file:///android_asset/\"", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(getMainActivity(), ex);
            }
        }
    }

    public void respond() {
        respond("0", "0", null);
    }

    public void respond(final String replyId, final String dp, String user) {
        LayoutInflater inflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.news_comment_edit, null);

        assert layout != null;
        final EditText message_edit = (EditText) layout.findViewById(R.id.comment);
        if (user != null)
            message_edit.setText("<b>" + URLDecoder.decode(user) + ",</b>");
        new MaterialDialog.Builder(getMainActivity())
                .title(R.string.LeaveComment)
                .customView(layout,true)
                .positiveText(R.string.Send)
                .negativeText("Отмена")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String message = message_edit.getText().toString();
                        if (TextUtils.isEmpty(message.trim())) {
                            Toast.makeText(getMainActivity(), "Текст не можут быть пустым!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        GetNewsTask getThemeTask = new GetNewsTask(getMainActivity());
                        getThemeTask.Comment = message;
                        getThemeTask.ReplyId = replyId;
                        getThemeTask.Dp = dp;
                        getThemeTask.execute(m_NewsUrl);
                    }
                })
                .show();
    }

    private void like() {
        Toast.makeText(getMainActivity(), "Запрос отправлен", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getMainActivity(), "Ошибка запроса", Toast.LENGTH_SHORT).show();
                                AppLog.e(getMainActivity(), finalEx);
                            } else {
                                Toast.makeText(getMainActivity(), "Запрос выполнен", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            AppLog.e(getMainActivity(), ex);
                        }
                    }
                });
            }
        }).start();
    }
    @JavascriptInterface
    public void likeComment(final String id, final String comment) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendLikeComment(id, comment);
            }
        });
    }
    private void sendLikeComment(final String id, final String comment) {
        Toast.makeText(getMainActivity(), "Запрос отправлен", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {

                Exception ex = null;

                try {
                    Client.getInstance().likeComment(id, comment);
                } catch (Exception e) {
                    ex = e;
                }

                final Exception finalEx = ex;
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if (finalEx != null) {
                                Toast.makeText(getMainActivity(), "Ошибка запроса", Toast.LENGTH_SHORT).show();
                                AppLog.e(getMainActivity(), finalEx);
                            } else {
                                Toast.makeText(getMainActivity(), "Запрос выполнен", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            AppLog.e(getMainActivity(), ex);
                        }
                    }
                });
            }
        }).start();
    }
}
