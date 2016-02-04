package org.softeg.slartus.forpdaplus.fragments.topic;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by radiationx on 01.02.16.
 */
public class PostPreviewFragment extends WebViewFragment {
    private AdvWebView webView;
    private View view;
    private String url;
    private String title;
    private WebViewClient webViewClient;
    private HtmlBuilder builder;
    private Map<String,String> bbMap = new HashMap<>();

    @Override
    public AdvWebView getWebView() {
        return webView;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public WebViewClient MyWebViewClient() {
        return webViewClient;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void reload() {

    }

    @Override
    public AsyncTask getAsyncTask() {
        return null;
    }

    @Override
    public Menu getMenu() {
        return null;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    public static PostPreviewFragment newInstance(String body, String tag){
        PostPreviewFragment fragment = new PostPreviewFragment();
        Bundle args = new Bundle();
        args.putString("BB_CODES_BODY", body);
        args.putString("parentTag", tag);
        fragment.setArguments(args);
        return fragment;
    }
    public static void showSpecial(String body, String tag) {
        MainActivity.addTab("Предпросмотр " + App.getInstance().getTabByTag(tag).getTitle(), "preview_" + tag, newInstance(body, tag));
    }
    public void load(String body){
        builder = new HtmlBuilder();
        builder.beginHtml("preview");
        builder.beginBody("preview");

        builder.append("<div class=\"panel top\" style=\"text-align:center;\"><div class=\"topic_title_post\"><a>Данная функция является экспериментальной, поэтому реальное отображение сообщения может отличаться</a></div></div><div class=\"posts_list\"><div class=\"post_container\"><div class=\"post_body \">");
        builder.append(parse(body));
        builder.append("</div></div></div>");

        builder.endBody();
        builder.endHtml();
        webView.loadDataWithBaseURL("http://4pda.ru/", builder.getHtml().toString(), "text/html", "UTF-8", null);
        webViewClient = new MyWebViewClient();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.post_preview_layout, container, false);
        title = App.getInstance().getTabByTag(getTag()).getTitle();
        url = App.getInstance().getTabByTag(getTag()).getUrl();
        initBBCodes();
        webView = (AdvWebView) view.findViewById(R.id.webView);
        registerForContextMenu(webView);
        setWebViewSettings();


        webView.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT < 18)
            //noinspection deprecation
            webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = App.getInstance().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAppCacheEnabled(true);


        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);


        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable e) {
                android.util.Log.e("kuk", e.getMessage());
            }
        }
        load(getArguments().getString("BB_CODES_BODY"));
        return view;
    }
    public class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            IntentActivity.tryShowUrl(getMainActivity(), new Handler(), url, true, false);
            return true;
        }
    }
    public String parse(String text) {
        String html = text;

        for (Map.Entry entry: bbMap.entrySet())
            html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());

        for (Map.Entry entry: bbMap.entrySet())
            html = html.replaceAll(entry.getKey().toString().toLowerCase(), entry.getValue().toString());

        html = html.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>");

        return html;
    }
    private void initBBCodes(){
        bbMap.put("\\[B\\]([^$]*?)\\[/B\\]", "<b>$1</b>");
        bbMap.put("\\[I\\]([^$]*?)\\[/I\\]", "<i>$1</i>");
        bbMap.put("\\[U\\]([^$]*?)\\[/U\\]", "<u>$1</u>");
        bbMap.put("\\[S\\]([^$]*?)\\[/S\\]", "<del>$1</del>");
        bbMap.put("\\[SUB\\]([^$]*?)\\[/SUB\\]", "<sub>$1</sub>");
        bbMap.put("\\[SUP\\]([^$]*?)\\[/SUP\\]", "<sub>$1</sub>");
        bbMap.put("\\[LEFT\\]([^$]*?)\\[/LEFT\\]", "<div align=\"left\">$1</div>");
        bbMap.put("\\[CENTER\\]([^$]*?)\\[/CENTER\\]", "<div align=\"center\">$1</div>");
        bbMap.put("\\[RIGHT\\]([^$]*?)\\[/RIGHT\\]", "<div align=\"right\">$1</div>");
        bbMap.put("\\[URL=\"([^$]*?)\"\\]([^$]*?)\\[/URL\\]", "<a href='$1'>$2</a>");
        bbMap.put("\\[SNAPBACK\\]([^$]*?)\\[/SNAPBACK\\]", "<a href=\"/forum/index.php?act=findpost&amp;pid=$1\" target=\"_blank\" title=\"Перейти к сообщению\"><img src=\"http://s.4pda.ru/forum/style_images/1/post_snapback.gif\" alt=\"*\" border=\"0\"></a>");
        bbMap.put("\\[OFFTOP\\]([^$]*?)\\[/OFFTOP\\]", "<font style=\"font-size:9px;color:gray;\">$1</font>");
        bbMap.put("\\[COLOR=\"([^$]*?)\"\\]([^$]*?)\\[/COLOR\\]", "<span style=\"color:$1;\">$2</span>");
        bbMap.put("\\[COLOR=([^$]*?)\\]([^$]*?)\\[/COLOR\\]", "<span style=\"color:$1;\">$2</span>");
        bbMap.put("\\[SIZE=1\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:8pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[SIZE=2\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:10pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[SIZE=3\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:12pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[SIZE=4\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:14pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[SIZE=5\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:18pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[SIZE=6\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:24pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[SIZE=7\\]([^$]*?)\\[/SIZE\\]", "<span style='font-size:36pt;line-height:100%;'>$1</span>");
        bbMap.put("\\[CODE\\]([^$]*?)\\[/CODE\\]", "<div class=\"post-block code box\"><div class=\"block-title\"></div><div class=\"block-body \">$1</div></div>");
        bbMap.put("\\[HIDE\\]([^$]*?)\\[/HIDE\\]", "<div class=\"post-block hidden\"><div class=\"block-title\"></div><div class=\"block-body\">$1</div></div>");
        bbMap.put("\\[SPOILER\\]", "<div class=\"post-block spoil close\"><div class=\"block-title\"></div><div class=\"block-body\">");
        bbMap.put("\\[SPOILER=([^$]*?)\\]", "<div class=\"post-block spoil close\"><div class=\"block-title\">$1</div><div class=\"block-body\">");
        bbMap.put("\\[/SPOILER\\]", "</div></div>");
        bbMap.put("\\[LIST\\]([^$]*?)\\[/LIST\\]", "<ul>$1</ul>");
        bbMap.put("\\[LIST=(\\d*)\\]([^$]*?)\\[/LIST\\]", "<ol type=\"$1\">$2</ol>");
        bbMap.put("\\[\\*\\]", "<li>");
        bbMap.put("name=\"([^$]*?)\"", "$1");
        bbMap.put("date=\"([^$]*?)\"", " @ $1");
        bbMap.put("post=([^]]*)", "<a href=\"/forum/index.php?act=findpost&amp;pid=$1\" target=\"_blank\" title=\"Перейти к сообщению\"><img src=\"http://s.4pda.ru/forum/style_images/1/post_snapback.gif\" alt=\"*\" border=\"0\"></a>");
        bbMap.put("\\[QUOTE([^$]*?)\\]([^$]*?)\\[/QUOTE\\]", "<div class=\"post-block quote\"><div class=\"block-title\">$1</div><div class=\"block-body\">$2</div></div>");
        bbMap.put("\\[ATTACHMENT=\"[\\d]*:([^$]*?)\"\\]", "<a href=\"#\">$1</a> ");
    }
}
