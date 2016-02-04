package org.softeg.slartus.forpdaplus.fragments.topic;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.BoringLayout;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.softeg.slartus.forpdaapi.post.EditPost;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 04.02.16.
 */
public class CuratorFragment extends WebViewFragment {
    private View view;
    private AdvWebView webView;
    private Menu menu;
    private String topicId = "";
    private String url = "";
    private String[] idsArray;
    private String postarg = "";
    private String postact = "";
    private String postUrl = "";
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
    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean closeTab() {
        return false;
    }
    public static CuratorFragment newInstance(String url, String topicId){
        CuratorFragment fragment = new CuratorFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        args.putString("TOPIC_ID", topicId);
        fragment.setArguments(args);
        return fragment;
    }
    public static void showSpecial(String url, String topicId) {
        MainActivity.addTab("КУРАТОР 3000", url, newInstance(url, topicId));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.curator_fragment, container, false);
        webView = (AdvWebView) view.findViewById(R.id.wvBody);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setColorNormal(App.getInstance().getColorAccent("Accent"));
        fab.setColorPressed(App.getInstance().getColorAccent("Pressed"));
        fab.setColorRipple(App.getInstance().getColorAccent("Pressed"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.evalJs("getIds();");
            }
        });
        setHideFab(fab);
        initSwipeRefreshLayout();
        webView.addJavascriptInterface(this, "HTMLOUT");
        setHasOptionsMenu(true);
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
                m_ThemeBody = parse(Client.getInstance().performGet(url)).getHtml().toString();
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
    private class PostTask extends AsyncTask<Boolean, String, Boolean> {
        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(Boolean... get) {
            try {
                if (isCancelled()) return false;

                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("postact", postact));
                nvps.add(new BasicNameValuePair("postarg", postarg));
                for (String anIdsArray : idsArray)
                    nvps.add(new BasicNameValuePair("postsel[]", anIdsArray));
                m_ThemeBody = parse(Client.getInstance().performPost("http://4pda.ru/forum/index.php"+postUrl, nvps)).getHtml().toString();
                postarg = "";
                postact = "";
                idsArray = null;
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
    private HtmlBuilder parse(String m_ThemeBody){
        HtmlBuilder builder = new HtmlBuilder();
        builder.beginHtml("preview");
        builder.beginBody("preview");
        builder.append("<script type=\"text/javascript\">\n" +
                "        function getIds() {\n" +
                "            var p = document.documentElement ? document.documentElement : document.body;\n" +
                "            var c = p.getElementsByTagName('input');\n" +
                "            var result = [];\n" +
                "            for (i = 0; i < c.length; ++i){\n" +
                "                if ('checkbox' == c[i].type){\n" +
                "                    if(c[i].checked){\n" +
                "                        result.push(c[i].getAttribute(\"value\"));\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "            HTMLOUT.showCuratorDialog(result.join())\n" +
                "        }\n" +
                "    </script>");

        String pages = "";
        Matcher m = Pattern.compile("<form method=\"post\" action=\"([\\s\\S]*?)\">([\\s\\S]*?)<\\/form>").matcher(m_ThemeBody);
        Matcher postMatcher;
        Matcher pagesMatcher = Pattern.compile("<br>Страницы:[^<]*([\\s\\S]*?)<br>").matcher(m_ThemeBody);
        if(pagesMatcher.find()){
            pages = pagesMatcher.group(1);
        }
        if(m.find()){
            builder.append("<div class=\"panel top\"><div class=\"pages\">"+pages+"</div></div>");
            builder.append("<div class=\"posts_list\">");
            postUrl = m.group(1);
            m = Pattern.compile("(<input [\\s\\S]*?)<hr>").matcher(m.group(2));
            while (m.find()){

                postMatcher = Pattern.compile("<input type=\"checkbox\" name=\"postsel\\[\\]\" value=\"(\\d*)\"[\\s\\S]*showuser=(\\d*)\"[^>]*>([^<]*)<\\/a>([^$]*)<br>[^<]*<br>([^$]*)").matcher(m.group(1));
                if(postMatcher.find()){
                    builder.append("<div class=\"post_container\"");
                    builder.append("<div class=\"post_header\">");
                    builder.append("<a class=\"inf nick\" href=\"http://4pda.ru/forum/index.php?showuser="+postMatcher.group(2)+"\"><span><b>"+postMatcher.group(3)+"</b></span></a>");
                    builder.append("<a class=\"inf link\" href=\"http://4pda.ru/forum/index.php?act=findpost&amp;pid="+postMatcher.group(1)+"\"><span><span class=\"sharp\">#</span>"+postMatcher.group(1)+"</span></a>");
                    builder.append("<div class=\"date-link\"><span class=\"inf date\"><span>"+postMatcher.group(4).replaceAll("\n","").replace("@", "")+"</span></span>");
                    builder.append("<div class=\"checkbox\"><label><input type=\"checkbox\" name=\"postsel[]\" value=\""+postMatcher.group(1)+"\"></label></div>");
                    builder.append("</div>");
                    builder.append("<div class=\"post_body \">");
                    builder.append(postMatcher.group(5));
                    builder.append("</div>");
                    builder.append("</div>");
                }


            }
            builder.append("</div>");
            builder.append("<div class=\"panel bottom\"><div class=\"pages\">"+pages+"</div>");
        }
        builder.endBody();
        builder.endHtml();
        return builder;
    }
    private void showThemeBody(String body) {
        try {
            webView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);
            webView.setWebViewClient(new MyWebViewClient());
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void load(String url, String topicId){
        this.topicId = topicId;
        load(url);
    }
    public void load(String url){
        this.url = url;
        setLoading(true);
        new LoadPostsTask().execute(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Настройка")
                .setIcon(R.drawable.ic_settings_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ThemeCurator.showMmodDialog(getActivity(), CuratorFragment.this, getArguments().getString("TOPIC_ID"));
                        return true;
                    }
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add("Ссылка")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ExtUrl.showSelectActionDialog(getMainActivity(), "Ссылка", url);
                        return true;
                    }
                });
        this.menu = menu;
    }
    @JavascriptInterface
    public void showCuratorDialog(final String ids) {
        run(new Runnable() {
            @Override
            public void run() {
                idsArray = ids.split(",");
                Log.e("kek", idsArray.length+" length");
                Log.e("kek", "'"+idsArray[0]+"'");
                if (TextUtils.isEmpty(idsArray[0])) {
                    Toast.makeText(getContext(), "Не выбрано ни одного сообщения", Toast.LENGTH_SHORT).show();
                    return;
                }

                LayoutInflater inflater = getMainActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.curator_actions, null);
                final RadioGroup group = (RadioGroup) view.findViewById(R.id.radioGroup);
                final EditText editText = (EditText) view.findViewById(R.id.editText2);
                editText.setVisibility(View.GONE);
                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.radioButton:
                                editText.setVisibility(View.GONE);
                                break;
                            case R.id.radioButton2:
                                editText.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });

                new MaterialDialog.Builder(getContext())
                        .customView(view, true)
                        .positiveText("Выполнить")
                        .negativeText("Отмена")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
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

                            }
                        })
                        .show();
            }
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

        if("mmod".equals(Uri.parse(url).getQueryParameter("autocom"))){
            load(url);
            return true;
        }

        return false;
    }
}
