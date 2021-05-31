package org.softeg.slartus.forpdaplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.softeg.slartus.forpdaapi.NewsApi;
import org.softeg.slartus.forpdacommon.AdBlocker;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.History;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer;
import org.softeg.slartus.forpdaplus.prefs.Preferences;


import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 17.10.15.
 */
public class NewsFragment extends WebViewFragment implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String URL_KEY = "Url";

    private final Handler mHandler = new Handler();
    private AdvWebView webView;

    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    private String m_NewsUrl;
    private final ArrayList<History> m_History = new ArrayList<>();
    private boolean loadImages;
    private String m_Title = App.getContext().getString(R.string.news);
    private FloatingActionButton fab;
    private FrameLayout buttonsPanel;

    public static NewsFragment newInstance(String url) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
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
    public WebViewClient getWebViewClient() {
        return new MyWebViewClient();
    }

    public void refresh() {
        showNews(m_NewsUrl);
    }


    public AdvWebView getWebView() {
        return webView;
    }

    @Override
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

    @SuppressLint("AddJavascriptInterface")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.news_fragment, container, false);
        webView = (AdvWebView) findViewById(R.id.wvBody);
        initSwipeRefreshLayout();
        registerForContextMenu(webView);
        setWebViewSettings();
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultFontSize(Preferences.News.getFontSize());
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        loadImages = webView.getSettings().getLoadsImagesAutomatically();

        webView.setWebViewClient(new MyWebViewClient());
        webView.addJavascriptInterface(this, "HTMLOUT");

        assert getArguments() != null;
        m_NewsUrl = getArguments().getString(URL_KEY);
        assert m_NewsUrl != null;
        showNews(m_NewsUrl);


        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (!App.getInstance().getPreferences().getBoolean("pancilInActionBar", false)) {
            fab.setOnClickListener(view -> {
                if (Client.getInstance().getLogined())
                    respond();
                else
                    Toast.makeText(getContext(), R.string.need_login, Toast.LENGTH_SHORT).show();
            });
            setHideFab(fab);
            setFabColors(fab);
        } else {
            fab.hide();
        }

        buttonsPanel = (FrameLayout) findViewById(R.id.buttonsPanel);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        //fab.setImageResource(R.drawable.pencil);
        //fab.setImageDrawable(ContextCompat.getDrawable(App.getContext(), R.drawable.pencil));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean pencil = App.getInstance().getPreferences().getBoolean("pancilInActionBar", false);
        menu.findItem(R.id.comment_item).setVisible(Client.getInstance().getLogined() & pencil);
        menu.findItem(R.id.hide_pencil_item).setChecked(Preferences.isHideFab());
        menu.findItem(R.id.LoadImages_item).setChecked(getWebView().getSettings().getLoadsImagesAutomatically());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comment_item:
                respond();
                return true;
            case R.id.refresh_item:
                refresh();
                return true;
            case R.id.thumb_up_item:
                like();
                return true;
            case R.id.hide_pencil_item:
                Preferences.setHideFab(!Preferences.isHideFab());
                setHideFab(fab);
                item.setChecked(Preferences.isHideFab());
                return true;
            case R.id.font_size_item:
                showFontSizeDialog();
                return true;
            case R.id.LoadImages_item:
                boolean loadImagesAutomatically1 = getWebView().getSettings().getLoadsImagesAutomatically();
                getWebView().getSettings().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                item.setChecked(!loadImagesAutomatically1);
                return true;
            case R.id.link_item:
                ExtUrl.showSelectActionDialog(getMainActivity(), getString(R.string.link), getUrl());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (inflater != null)
            inflater.inflate(R.menu.news, menu);
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
        getMainActivity();
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            Uri uri = data.getData();
            assert uri != null;
            String attachFilePath = FileUtils.getRealPathFromURI(getMainActivity(), uri);
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            webView.evalJs("window['HtmlInParseLessContent']('" + cssData + "');");
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void sendNewsAttaches(final String json) {
        getMainActivity().runOnUiThread(() -> {
            for (JsonElement s : new JsonParser().parse(json).getAsJsonArray()) {
                ArrayList<String> list1 = new ArrayList<>();
                for (JsonElement a : s.getAsJsonArray())
                    list1.add(a.getAsString());
                imageAttaches.add(list1);
            }
        });

    }

    public List<ArrayList<String>> imageAttaches = new ArrayList<>();

    private class MyWebViewClient extends WebViewClient {

        private final Map<String, Boolean> loadedUrls = new HashMap<>();

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            boolean ad;
            url = IntentActivity.getRedirectUrl(url);
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url);
                loadedUrls.put(url, ad);
            } else {
                ad = loadedUrls.get(url);
            }
            return ad ? AdBlocker.createEmptyResource() :
                    super.shouldInterceptRequest(view, url);
        }


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
            boolean ad;
            url = IntentActivity.getRedirectUrl(url);
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url);
                loadedUrls.put(url, ad);
            } else {
                ad = loadedUrls.get(url);
            }
            if (ad)
                return false;

            m_ScrollY = 0;
            m_ScrollX = 0;

            if (checkIsImage(url))
                return true;

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
                IntentActivity.showInDefaultBrowser(getActivity(), url);
                return true;
            }

            IntentActivity.tryShowUrl(getMainActivity(), mHandler, url, true, false);

            return true;
        }

        private boolean checkIsImage(final String url) {
            final Pattern imagePattern = PatternExtensions.compile("https?://.*?\\.(png|jpg|jpeg|gif)$");
            Uri uri = Uri.parse(url.toLowerCase());
            String host = uri.getHost();
            if (imagePattern.matcher(uri.toString()).find()
                    || (host != null && (host.toLowerCase().contains("ggpht.com")
                    || host.toLowerCase().contains("googleusercontent.com")
                    || host.toLowerCase().contains("windowsphone.com")))) {
                if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
                    Client.getInstance().showLoginForm(getContext());
                } else {
                    showImage(url);
                }
                return true;
            }
            return false;
        }

        private void showImage(String url) {
            for (ArrayList<String> list : imageAttaches) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).equals(url)) {
                        ImgViewer.startActivity(getContext(), list, i);
                        return;
                    }
                }
            }
            ImgViewer.startActivity(getContext(), url);
        }
    }


    private Boolean isReplyUrl(String url) {
        Matcher m = Pattern.compile("4pdaservice.org/(\\d+)/(\\d+)").matcher(url);
        if (m.find()) {
            respond(m.group(1), m.group(2), null);
            return true;
        }

        if (Pattern.compile("4pdaservice.org/#commentform").matcher(url).find()) {
            respond();
            return true;
        }

        return false;
    }

    private String getPostId() {
        final Pattern pattern = Pattern.compile(App.Host + "/\\d{4}/\\d{2}/\\d{2}/(\\d+)");
        Matcher m = pattern.matcher(m_NewsUrl);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static Boolean isAnchor(String url) {
        final Pattern pattern = Pattern.compile(App.Host + "/\\d{4}/\\d{2}/\\d{2}/\\d+/*#.*");
        return pattern.matcher(url).find();
    }

    private void showAnchor(String url) {
        final Pattern pattern = Pattern.compile(App.Host + "/\\d{4}/\\d{2}/\\d{2}/\\d+/*#(.*)");
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            webView.evalJs("scrollToElement('" + m.group(1) + "');");
        }
    }

    private static class NewsHtmlBuilder extends HtmlBuilder {
        @Override
        public HtmlBuilder endBody() {
            m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/newsAttaches.js\"></script>\n");
            return super.endBody();
        }

        @Override
        public void addScripts() {
            super.addScripts();
            m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/news.js\"></script>\n");
        }

        @Override
        public void addStyleSheetLink(StringBuilder sb) {
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://").append(getStyle()).append("\" />\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/roboto/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/flaticons/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/fonts/fontello/import.css\"/>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/forum/css/youtube_video.css\" type=\"text/css\" />\n");
        }
    }

    private GetNewsTask asyncTask = null;

    @Override
    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    private void showNews(String url) {
        m_NewsUrl = url.trim();
        webView.setWebViewClient(new MyWebViewClient());
        saveHistory(m_NewsUrl);

        asyncTask = new GetNewsTask();
        asyncTask.execute(m_NewsUrl.replace("|", ""));
    }

    public void showBody(String body) throws Exception {
        super.showBody();
        try {
            setTitle(m_Title);
            webView.loadDataWithBaseURL("https://" + App.Host + "/forum/", body, "text/html", "UTF-8", null);
            if (buttonsPanel.getTranslationY() != 0)
                ViewPropertyAnimator.animate(buttonsPanel)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(500)
                        .translationY(0);
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

        String Comment = null;
        String ReplyId;
        String Dp;

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.getInstance();

                if (TextUtils.isEmpty(Comment))
                    m_ThemeBody = transformBody(client.performGet(m_NewsUrl).getResponseBody());
                else {
                    Map<String, String> additionalHeaders = new HashMap<>();
                    additionalHeaders.put("comment", Comment);
                    additionalHeaders.put("comment_post_ID", getPostId());
                    additionalHeaders.put("submit", "Отправить комментарий");
                    additionalHeaders.put("comment_reply_ID", ReplyId);
                    additionalHeaders.put("comment_reply_dp", Dp);
                    m_ThemeBody = transformBody(client.performPost("https://" + App.Host + "/wp-comments-post.php", additionalHeaders, "UTF-8").getResponseBody());
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
            m_Title = App.getContext().getString(R.string.news);
            builder.beginHtml(m_Title);
            builder.beginBody("news", null, loadImages);
            builder.append("<div style=\"padding-top:").append(String.valueOf(HtmlBuilder.getMarginTop())).append("px\"/>\n");
            builder.append("<div id=\"main\">");
            body = body.replaceAll("\"//", "\"https://");

            Matcher matcher = PatternExtensions.compile("ModKarma\\((\\{?[\\s\\S]*?\\}?)(?:,\\s?\\d+)?\\)").matcher(body);
            builder.append(parseBody(body));

            if (matcher.find()) {
                builder.append("<script type=\"text/javascript\">function getCommentsData(){return '").append(matcher.group(1)).append("';}</script>");
                builder.append("<script type=\"text/javascript\">kek(").append(getPostId()).append(", ").append(String.valueOf(Client.getInstance().getLogined())).append(");</script>");
            }
            builder.append("</div>");
            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        private String parseBody(String body) {

            body = NewsApi.parseNewsBody(body);

            // удалим все стили
            body = body.replaceAll("<link rel=\"stylesheet\"[^>]*>", "");
            return normalizeCommentUrls(body);
        }


        private String normalizeCommentUrls(String body) {
            if (App.getInstance().getPreferences().getBoolean("loadNewsComment", false)) {
                body = body.replaceAll("(<div class=\"comment-box\" id=\"comments\">[\\s\\S]*?<ul class=\"page-nav box\">[\\s\\S]*?<\\/ul>)", "");
            }

            body = Pattern.compile("<iframe[^><]*?src=\"http://www.youtube.com/embed/([^\"/]*)\".*?(?:</iframe>|/>)", Pattern.CASE_INSENSITIVE)
                    .matcher(body)
                    .replaceAll("<a class=\"video-thumb-wrapper\" href=\"https?://www.youtube.com/watch?v=$1\"><img class=\"video-thumb\" width=\"480\" height=\"320\" src=\"https://img.youtube.com/vi/$1/0.jpg\"/></a>");
            return body
                    // удаляем форму ответа
                    .replaceAll("<form[^>]*action=\"/wp-comments-post.php\"[^>]*>[\\s\\S]*</form>",
                            "")
                    // заменяем обрезанные ссылки
                    .replace("href=\"/", "href=\"https://" + App.Host + "/")
                    .replace("href=\"#commentform\"", "href=\"https://4pdaservice.org/#commentform");
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
            if (isCancelled()) return;
            if (success) {
                try {
                    showBody(m_ThemeBody);
                } catch (Exception e) {
                    AppLog.e(e);
                }
                setLoading(false);
            } else {
                setTitle(ex.getMessage());
                webView.loadDataWithBaseURL("https://" + App.Host + "/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(getMainActivity(), ex);
            }
        }
    }

    public void respond() {
        respond("0", "0", null);
    }

    public void respond(final String replyId, final String dp, String user) {
        LayoutInflater inflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View layout = inflater.inflate(R.layout.news_comment_edit, null);

        assert layout != null;
        final EditText message_edit = layout.findViewById(R.id.comment);
        if (user != null)
            message_edit.setText(String.format("<b>%s,</b>", URLDecoder.decode(user)));
        new MaterialDialog.Builder(getMainActivity())
                .title(R.string.LeaveComment)
                .customView(layout, true)
                .positiveText(R.string.send)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    String message = message_edit.getText().toString();
                    if (TextUtils.isEmpty(message.trim())) {
                        Toast.makeText(getMainActivity(), R.string.empty_text, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    asyncTask = new GetNewsTask();
                    asyncTask.Comment = message;
                    asyncTask.ReplyId = replyId;
                    asyncTask.Dp = dp;
                    asyncTask.execute(m_NewsUrl);
                })
                .show();
    }

    private void like() {
        Toast.makeText(getMainActivity(), R.string.request_sent, Toast.LENGTH_SHORT).show();
        new Thread(() -> {

            Exception ex = null;

            try {
                Client.getInstance().likeNews(getPostId());
            } catch (Exception e) {
                ex = e;
            }

            final Exception finalEx = ex;
            mHandler.post(() -> {
                try {
                    if (finalEx != null) {
                        Toast.makeText(getMainActivity(), R.string.error_request, Toast.LENGTH_SHORT).show();
                        AppLog.e(getMainActivity(), finalEx);
                    } else {
                        Toast.makeText(getMainActivity(), R.string.request_performed, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex1) {
                    AppLog.e(getMainActivity(), ex1);
                }
            });
        }).start();
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void likeComment(final String id, final String comment) {
        getMainActivity().runOnUiThread(() -> sendLikeComment(id, comment));
    }

    private void sendLikeComment(final String id, final String comment) {
        Toast.makeText(getMainActivity(), R.string.request_sent, Toast.LENGTH_SHORT).show();
        new Thread(() -> {

            Exception ex = null;

            try {
                Client.getInstance().likeComment(id, comment);
            } catch (Exception e) {
                if (!(e instanceof ShowInBrowserException)) {
                    ex = e;
                }
            }

            final Exception finalEx = ex;
            mHandler.post(() -> {
                try {
                    if (finalEx != null) {
                        Toast.makeText(getMainActivity(), R.string.error_request, Toast.LENGTH_SHORT).show();
                        AppLog.e(getMainActivity(), finalEx);
                    } else {
                        Toast.makeText(getMainActivity(), R.string.request_performed, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex1) {
                    AppLog.e(getMainActivity(), ex1);
                }
            });
        }).start();
    }
}
