package org.softeg.slartus.forpdaplus.fragments.topic;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
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
import org.softeg.slartus.forpdaplus.tabs.TabsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radiationx on 01.02.16.
 */
public class PostPreviewFragment extends WebViewFragment {
    private AdvWebView webView;
    private String url;
    private String title;
    private WebViewClient webViewClient;
    private HtmlBuilder builder;
    private final List<BBCode> bbCodes = new ArrayList<>();


    @Override
    public AdvWebView getWebView() {
        return webView;
    }

    @Override
    public WebViewClient getWebViewClient() {
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
    public boolean closeTab() {
        return false;
    }

    public static PostPreviewFragment newInstance(String body, String tag) {
        PostPreviewFragment fragment = new PostPreviewFragment();
        Bundle args = new Bundle();
        args.putString("BB_CODES_BODY", body);
        args.putString("parentTag", tag);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showSpecial(String body, String tag) {
        MainActivity.addTab(App.getContext().getString(R.string.preview) + " " + TabsManager.getInstance().getTabByTag(tag).getTitle(), "preview_" + tag, newInstance(body, tag));
    }

    public void load(String body) {
        builder = new HtmlBuilder();
        builder.beginHtml("preview");
        builder.beginBody("preview");

        builder.append("<div class=\"panel top\" style=\"text-align:center;\"><div class=\"topic_title_post\"><a>Данная функция является экспериментальной, поэтому реальное отображение сообщения может отличаться</a></div></div><div class=\"posts_list\"><div class=\"post_container\"><div class=\"post_body \">");
        builder.append(parse(body));
        builder.append("</div></div></div>");

        builder.endBody();
        builder.endHtml();
        webView.loadDataWithBaseURL("https://" + App.Host + "/forum/", builder.getHtml().toString(), "text/html", "UTF-8", null);
        webViewClient = new MyWebViewClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.post_preview_layout, container, false);
        title = TabsManager.getInstance().getTabByTag(getTag()).getTitle();
        url = TabsManager.getInstance().getTabByTag(getTag()).getUrl();
        initBBCodes();
        webView = (AdvWebView) findViewById(R.id.webView);
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
        webView.addJavascriptInterface(this, "HTMLOUT");
        webView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        load(getArguments().getString("BB_CODES_BODY"));
        return view;
    }

    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            IntentActivity.tryShowUrl(getMainActivity(), new Handler(), url, true, false);
            return true;
        }
    }

    public String parse(String text) {
        String html = text;
        for (BBCode bbcode : bbCodes)
            html = html.replaceAll(bbcode.bbcode, bbcode.htmlcode);

        html = html.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>");

        return html;
    }

    private void initBBCodes() {
        bbCodes.add(new BBCode("(?i)\\[B\\]", "<b>"));
        bbCodes.add(new BBCode("(?i)\\[/B\\]", "</b>"));

        bbCodes.add(new BBCode("(?i)\\[I\\]", "<i>"));
        bbCodes.add(new BBCode("(?i)\\[/I\\]", "</i>"));

        bbCodes.add(new BBCode("(?i)\\[U\\]", "<u>"));
        bbCodes.add(new BBCode("(?i)\\[/U\\]", "</u>"));

        bbCodes.add(new BBCode("(?i)\\[S\\]", "<del>"));
        bbCodes.add(new BBCode("(?i)\\[/S\\]", "</del>"));

        bbCodes.add(new BBCode("(?i)\\[SUB\\]", "<sub>"));
        bbCodes.add(new BBCode("(?i)\\[/SUB\\]", "</sub>"));

        bbCodes.add(new BBCode("(?i)\\[SUP\\]", "<sub>"));
        bbCodes.add(new BBCode("(?i)\\[/SUP\\]", "</sub>"));

        bbCodes.add(new BBCode("(?i)\\[LEFT\\]", "<div align=\"left\">"));
        bbCodes.add(new BBCode("(?i)\\[/LEFT\\]", "</div>"));

        bbCodes.add(new BBCode("(?i)\\[CENTER\\]", "<div align=\"center\">"));
        bbCodes.add(new BBCode("(?i)\\[/CENTER\\]", "</div>"));

        bbCodes.add(new BBCode("(?i)\\[RIGHT\\]", "<div align=\"right\">"));
        bbCodes.add(new BBCode("(?i)\\[/RIGHT\\]", "</div>"));

        bbCodes.add(new BBCode("(?i)\\[ANCHOR\\]([^$]*?)\\[/ANCHOR\\]", "<a name=\"$1\" title=\"$1\">ˇ</a>"));

        bbCodes.add(new BBCode("(?i)\\[URL\\]([^\\[]*)\\[\\/URL\\]", "<a href='$1'>$1</a>"));
        bbCodes.add(new BBCode("(?i)\\[URL=\"([^$]*?)\"\\]", "<a href='$1'>"));
        bbCodes.add(new BBCode("(?i)\\[/URL\\]", "</a>"));

        bbCodes.add(new BBCode("(?i)\\[EMAIL\\]([^\\[]*)\\[\\/EMAIL\\]", "<a href='mailto:$1'>$1</a>"));
        bbCodes.add(new BBCode("(?i)\\[EMAIL=\"([^$]*?)\"\\]", "<a href='mailto:$1'>"));
        bbCodes.add(new BBCode("(?i)\\[/EMAIL\\]", "</a>"));

        bbCodes.add(new BBCode("(?i)\\[FONT=[\"|^$]*([^$]*?)[\"^$]*\\]", "<span style=\"font-family:$1\">"));
        bbCodes.add(new BBCode("(?i)\\[/FONT\\]", "</span>"));

        bbCodes.add(new BBCode("(?i)\\[SNAPBACK\\]([^$]*?)\\[/SNAPBACK\\]", "<a href=\"/forum/index.php?act=findpost&amp;pid=$1\" target=\"_blank\" title=\"Перейти к сообщению\"><img src=\"https://s." + App.Host + "/forum/style_images/1/post_snapback.gif\" alt=\"*\" border=\"0\"></a>"));

        bbCodes.add(new BBCode("(?i)\\[OFFTOP\\]", "<font style=\"font-size:9px;color:gray;\">"));
        bbCodes.add(new BBCode("(?i)\\[/OFFTOP\\]", "</font>"));

        bbCodes.add(new BBCode("(?i)\\[SIZE=1\\]", "<span style='font-size:8pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[SIZE=2\\]", "<span style='font-size:10pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[SIZE=3\\]", "<span style='font-size:12pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[SIZE=4\\]", "<span style='font-size:14pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[SIZE=5\\]", "<span style='font-size:18pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[SIZE=6\\]", "<span style='font-size:24pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[SIZE=7\\]", "<span style='font-size:36pt;line-height:100%;'>"));
        bbCodes.add(new BBCode("(?i)\\[/SIZE\\]", "</span>"));

        bbCodes.add(new BBCode("(?i)\\[LIST\\]([^$]*?)\\[/LIST\\]", "<ul>$1</ul>"));
        bbCodes.add(new BBCode("(?i)\\[LIST=([^\\]]*)\\]([^$]*?)\\[\\/LIST\\]", "<ol type=\"$1\">$2</ol>"));
        bbCodes.add(new BBCode("(?i)\\[\\*\\]", "<li>"));

        bbCodes.add(new BBCode("(?i)\\[ATTACHMENT=\"[\\d]*:([^$]*?)\"\\]", "<a href=\"#\">$1</a> "));

        bbCodes.add(new BBCode("(?i)\\[COLOR=[\"|^$]*([^$]*?)[\"^$]*\\]", "<span style=\"color:$1;\">"));
        bbCodes.add(new BBCode("(?i)\\[/COLOR\\]", "</span>"));

        bbCodes.add(new BBCode("(?i)\\[BACKGROUND=[\"|^$]*([^$]*?)[\"^$]*\\]", "<span style=\"background-color:$1;\">"));
        bbCodes.add(new BBCode("(?i)\\[/BACKGROUND\\]", "</span>"));

        bbCodes.add(new BBCode("(?i)\\[CODE\\]", "<div class=\"post-block code box\"><div class=\"block-title\"></div><div class=\"block-body \">"));
        bbCodes.add(new BBCode("(?i)\\[CODE=[\"|^$]*([^$]*?)[\"^$]*\\]", "<div class=\"post-block code box\"><div class=\"block-title\">$1</div><div class=\"block-body \">"));
        bbCodes.add(new BBCode("(?i)\\[/CODE\\]", "</div></div>"));

        bbCodes.add(new BBCode("(?i)\\[HIDE[^\\]]*\\]", "<div class=\"post-block hidden\"><div class=\"block-title\"></div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[/HIDE\\]", "</div></div>"));

        bbCodes.add(new BBCode("(?i)\\[SPOILER\\]", "<div class=\"post-block spoil close\"><div class=\"block-title\"></div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[SPOILER=[\"|^$]*([^$]*?)[\"^$]*\\]", "<div class=\"post-block spoil close\"><div class=\"block-title\">$1</div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[/SPOILER\\]", "</div></div>"));

        bbCodes.add(new BBCode("(?i)\\[QUOTE=[\"|^$]*([^$]*?)[\"^$]*\\]", "<div class=\"post-block quote\"><div class=\"block-title\">$1</div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[QUOTE([^$]*?)\\]", "<div class=\"post-block quote\"><div class=\"block-title\">$1</div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[/QUOTE\\]", "</div></div>"));

        bbCodes.add(new BBCode("(?i)\\[MOD\\]", "<div class=\"post-block tbl mod\"><div class=\"block-title\">M</div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[/MOD\\]", "</div></div>"));

        bbCodes.add(new BBCode("(?i)\\[CUR\\]", "<div class=\"post-block tbl cur\"><div class=\"block-title\">K</div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[/CUR\\]", "</div></div>"));

        bbCodes.add(new BBCode("(?i)\\[EX\\]", "<div class=\"post-block tbl ex\"><div class=\"block-title\">!</div><div class=\"block-body\">"));
        bbCodes.add(new BBCode("(?i)\\[/EX\\]", "</div></div>"));

        bbCodes.add(new BBCode("name=\"([^$]*?)\"", "$1"));
        bbCodes.add(new BBCode("date=\"([^$]*?)\"", " @ $1"));
        bbCodes.add(new BBCode("post=([^]^<]*)", "<a href=\"/forum/index.php?act=findpost&amp;pid=$1\" target=\"_blank\" title=\"Перейти к сообщению\"><img src=\"https://s." + App.Host + "/forum/style_images/1/post_snapback.gif\" alt=\"*\" border=\"0\"></a>"));

    }

    private class BBCode {
        public String bbcode;
        public String htmlcode;

        public BBCode(final String bbcode, final String htmlcode) {
            this.bbcode = bbcode;
            this.htmlcode = htmlcode;
        }
    }
}
