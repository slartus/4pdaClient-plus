package org.softeg.slartus.forpdaplus.fragments.topic;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 04.02.16.
 */
public class CuratorFragment extends WebViewFragment {
    private AdvWebView webView;
    private String topicId = "";
    private String url = "";
    private String ids = "";
    private String postarg = "";
    private String postact = "";
    private String postUrl = "";

    @Override
    public AdvWebView getWebView() {
        return webView;
    }

    @Override
    public WebViewClient getWebViewClient() {
        return new MyWebViewClient();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public void reload() {
        load(url, topicId);
    }

    @Override
    public AsyncTask getAsyncTask() {
        return null;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    public static CuratorFragment newInstance(String url, String topicId) {
        CuratorFragment fragment = new CuratorFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putString("TOPIC_ID", topicId);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showSpecial(String url, String topicId) {
        MainActivity.addTab(App.getContext().getString(R.string.multimoderation), url, newInstance(url, topicId));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.curator_fragment, container, false);
        webView = (AdvWebView) findViewById(R.id.wvBody);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (App.getInstance().getPreferences().getBoolean("pancilInActionBar", false)) {
            fab.hide();
        } else {
            setHideFab(fab);
            setFabColors(fab);
            fab.setOnClickListener(view -> webView.evalJs("getIds();"));
        }
        ImageButton up = (ImageButton) findViewById(R.id.btnUp);
        ImageButton down = (ImageButton) findViewById(R.id.btnDown);
        up.setOnClickListener(v -> webView.pageUp(true));
        down.setOnClickListener(v -> webView.pageDown(true));
        initSwipeRefreshLayout();
        webView.addJavascriptInterface(this, "HTMLOUT");
        topicId = getArguments().getString("TOPIC_ID");
        url = getArguments().getString("URL");
        load(url, topicId);
        return view;
    }

    private class LoadPostsTask extends AsyncTask<Boolean, String, Boolean> {
        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(Boolean... get) {
            try {
                if (isCancelled()) return false;
                m_ThemeBody = parse(Client.getInstance().performGet(url, true, false).getResponseBody()).getHtml().toString();
                return true;
            } catch (Throwable e) {
                return false;
            }
        }


        protected void onPreExecute() {
            setLoading(true);
        }


        protected void onPostExecute(final Boolean success) {
            setLoading(false);
            if (isCancelled()) return;
            if (success) {
                showThemeBody(m_ThemeBody);
                getArguments().putString("TOPIC_ID", topicId);
                System.gc();
            }
        }
    }

    private class PostTask extends AsyncTask<Boolean, String, Boolean> {
        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(Boolean... get) {
            try {
                if (isCancelled()) return false;

                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("postact", postact));
                nvps.add(new BasicNameValuePair("postarg", postarg));
                nvps.add(new BasicNameValuePair("postsel2", ids));
                m_ThemeBody = parse(Client.getInstance().performPost(postUrl, nvps).getResponseBody()).getHtml().toString();
                postarg = "";
                postact = "";
                ids = "";
                return true;
            } catch (Throwable e) {
                return false;
            }
        }


        protected void onPreExecute() {
            setLoading(true);
        }

        protected void onPostExecute(final Boolean success) {
            setLoading(false);
            if (isCancelled()) return;
            if (success) {
                showThemeBody(m_ThemeBody);
                getArguments().putString("TOPIC_ID", topicId);
            }
        }
    }

    private HtmlBuilder parse(String m_ThemeBody) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.beginHtml("curator");
        builder.beginBody("curator");

        String pages = "";
        Pattern mainPattern = PatternExtensions.compile("<form method=\"post\"[\\s\\S]*?action=\"([^\"]*?)\"[^>]*>([\\s\\S]*?)</form>");
        Pattern postsPattern = PatternExtensions.compile("<div id=\"anchor-p\\d+\">([\\s\\S]+?)<\\/div>(?=(<div id=\"anchor-p\\d+\">|<hr>))");
        Pattern postPattern = PatternExtensions.compile("<input type=\"checkbox\" name=\"postsel\\[\\]\" value=\"(\\d*?)\"[\\s\\S]*?showuser=(\\d*)\"[^>]*>(.*?)<\\/a>([^$]*?)(?:<br[^>]*>)+([^$]*)");

        Matcher postMatcher;
        Matcher m = Pattern.compile("<br>Страницы:[^<]*([\\s\\S]*?)<br>").matcher(m_ThemeBody);
        if (m.find())
            pages = m.group(1);

        m = mainPattern.matcher(m_ThemeBody);
        if (m.find()) {
            builder.append("<div class=\"panel top\"><div class=\"pages\">").append(pages).append("</div>");
            builder.append("<div class=\"controls\"><button onclick=\"invertCheckboxes();\">Инвертировать</button><button onclick=\"setCheckedAll();\">Выделить всё</button></div></div>");
            builder.append("<div class=\"posts_list\">");
            postUrl = m.group(1);

            m = postsPattern.matcher(m.group(2));
            while (m.find()) {

                postMatcher = postPattern.matcher(m.group(1));
                if (postMatcher.find()) {
                    String postId = postMatcher.group(1);
                    String userId = postMatcher.group(2);
                    String userNick = postMatcher.group(3);
                    String groupAndDate = postMatcher.group(4);
                    String postBody = postMatcher.group(5);

                    addPost(builder, postId, userId, userNick, groupAndDate, postBody);
                }
            }
            builder.append("</div>");
            builder.append("<div class=\"panel bottom\"><div class=\"controls\"><button onclick=\"invertCheckboxes();\">Инвертировать</button><button onclick=\"setCheckedAll();\">Выделить всё</button></div>");
            builder.append("<div class=\"pages\">").append(pages).append("</div></div>");
        }
        builder.endBody();
        builder.endHtml();
        return builder;
    }

    private void addPost(HtmlBuilder builder, String postId, String userId, String userNick,
                         String groupAndDate, String postBody) {
        builder.append("<div class=\"post_container\">");
        builder.append("<div class=\"post_header\">");
        builder.append("<a class=\"inf nick\" href=\"https://" + App.Host + "/forum/index.php?showuser=")
                .append(userId).append("\"><span><b>").append(userNick)
                .append("</b></span></a>");
        builder.append("<a class=\"inf link\" href=\"https://" + App.Host + "/forum/index.php?act=findpost&amp;pid=")
                .append(postId).append("\"><span><span class=\"sharp\">#</span>").append(postId)
                .append("</span></a>");
        builder.append("<div class=\"date-link\"><span class=\"inf date\"><span>")
                .append(groupAndDate.replaceAll("\n", "").replace("@", "")).append("</span></span></div>");
        builder.append("<label class=\"checkbox\"><input type=\"checkbox\" name=\"postsel[]\" value=\"").append(postId).append("\"><span class=\"icon\"></span></label>");
        builder.append("</div>");
        builder.append("<div class=\"post_body \">");
        builder.append(postBody);
        builder.append("</div>");
        builder.append("</div>");
    }

    private void showThemeBody(String body) {
        try {
            webView.loadDataWithBaseURL("https://" + HostHelper.getHost() + "/forum/", body, "text/html", "UTF-8", null);
            webView.setWebViewClient(new MyWebViewClient());
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void load(String url, String topicId) {
        this.topicId = topicId;
        load(url);
    }

    public void load(String url) {
        this.url = url;
        setLoading(true);
        new LoadPostsTask().execute(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (App.getInstance().getPreferences().getBoolean("pancilInActionBar", false)) {
            menu.add(R.string.write)
                    .setIcon(R.drawable.auto_fix)
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("getIds();");
                        return true;
                    }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menu.add(R.string.setting)
                .setIcon(R.drawable.settings_white)
                .setOnMenuItemClickListener(item -> {
                    ThemeCurator.showMmodDialog(getActivity(), CuratorFragment.this, getArguments().getString("TOPIC_ID"));
                    return true;
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(R.string.link)
                .setOnMenuItemClickListener(item -> {
                    ExtUrl.showSelectActionDialog(getMainActivity(), getString(R.string.link), url);
                    return true;
                });
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showCuratorDialog(final String ids) {
        run(() -> {
            CuratorFragment.this.ids = ids;
            String[] idsArray = ids.split(",");
            if (TextUtils.isEmpty(idsArray[0])) {
                Toast.makeText(getContext(), R.string.no_selected_posts, Toast.LENGTH_SHORT).show();
                return;
            }

            LayoutInflater inflater = getMainActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.curator_actions, null);
            final RadioGroup group = view.findViewById(R.id.radioGroup);
            final EditText editText = view.findViewById(R.id.editText2);
            editText.setVisibility(View.GONE);
            group.setOnCheckedChangeListener((group1, checkedId) -> {
                switch (checkedId) {
                    case R.id.radioButton:
                        editText.setVisibility(View.GONE);
                        break;
                    case R.id.radioButton2:
                        editText.setVisibility(View.VISIBLE);
                        break;
                }
            });

            new MaterialDialog.Builder(getContext())
                    .customView(view, true)
                    .positiveText(R.string.perform)
                    .negativeText(R.string.cancel)
                    .onPositive((dialog, which) -> {
                        switch (group.getCheckedRadioButtonId()) {
                            case R.id.radioButton:
                                postact = "del";
                                break;
                            case R.id.radioButton2:
                                postact = "mov";
                                postarg = editText.getText().toString();
                                break;
                        }
                        new PostTask().execute();

                    })
                    .show();
        });

    }


    public void run(final Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if (checkIsTheme(url))
                return true;
            IntentActivity.tryShowUrl(getMainActivity(), new Handler(), url, true, false);
            return true;
        }

    }

    private boolean checkIsTheme(String url) {

        if ("mmod".equals(Uri.parse(url).getQueryParameter("act"))) {
            load(url);
            return true;
        }

        return false;
    }
}
