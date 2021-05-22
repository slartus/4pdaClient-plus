package org.softeg.slartus.forpdaplus.fragments.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by radiationx on 07.11.15.
 */
public class ProfileEditFragment extends WebViewFragment {
    private AdvWebView m_WebView;

    public final static String m_Title = "Изменить личные данные";
    private final String parentTag = TabsManager.getInstance().getCurrentFragmentTag();
    private final static String url = "https://"+ HostHelper.getHost() +"/forum/index.php?act=UserCP&CODE=01";

    public static void editProfile() {
        MainActivity.addTab(m_Title, url, new ProfileEditFragment());
    }


    @Override
    public AdvWebView getWebView() {
        return m_WebView;
    }


    @Override
    public WebViewClient getWebViewClient() {
        return new WebViewClient();
    }

    @Override
    public String getTitle() {
        return m_Title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void reload() {
        asyncTask = new getEditProfileTask().execute("".replace("|", ""));
    }

    @Override
    public String Prefix() {
        return "edit_profile";
    }

    AsyncTask asyncTask = null;

    @Override
    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_edit_activity, container, false);
        initSwipeRefreshLayout();
        assert view != null;
        m_WebView = (AdvWebView) findViewById(R.id.wvBody);
        registerForContextMenu(m_WebView);

        m_WebView.getSettings();
        m_WebView.getSettings().setDomStorageEnabled(true);
        m_WebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        m_WebView.getSettings().setAppCachePath(getMainActivity().getApplicationContext().getCacheDir().getAbsolutePath());
        m_WebView.getSettings().setAppCacheEnabled(true);

        m_WebView.getSettings().setAllowFileAccess(true);

        m_WebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        m_WebView.addJavascriptInterface(this, "HTMLOUT");
        m_WebView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());

        asyncTask = new getEditProfileTask().execute();
        return view;
    }

    @JavascriptInterface
    public void sendProfile(final String json) {
        getMainActivity().runOnUiThread(() -> asyncTask = new editProfileTask(getMainActivity(), json).execute());
    }

    private class editProfileTask extends AsyncTask<String, Void, Void> {
        Map<String, String> additionalHeaders = new HashMap<>();
        MaterialDialog dialog;

        editProfileTask(Context context, String json) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Отправка данных")
                    .build();

            try {
                JSONArray jr = new JSONArray(json);
                JSONObject jb;
                for (int i = 0; i < jr.length(); i++) {
                    jb = jr.getJSONObject(i);
                    if (!jb.getString("name").equals("null")) {
                        additionalHeaders.put(jb.getString("name"), jb.getString("value"));
                    }
                }
                additionalHeaders.put("auth_key", Client.getInstance().getAuthKey());
                additionalHeaders.put("act", "UserCP");
                additionalHeaders.put("CODE", "21");
                additionalHeaders.put("ed-0_wysiwyg_used", "0");
                additionalHeaders.put("editor_ids[]", "ed-0");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                this.dialog.show();
            } catch (Exception ex) {
                this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                Client.getInstance().performPost("https://"+ HostHelper.getHost() +"/forum/index.php", additionalHeaders);
            } catch (IOException e) {
                Log.d("asdasd", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            Toast.makeText(getContext(), "Данные отправлены", Toast.LENGTH_SHORT).show();
            if (TabsManager.getInstance().isContainsByTag(parentTag)) {
                ((ProfileFragment) TabsManager.getInstance().getTabByTag(parentTag).getFragment()).startLoadData();
            }
            getMainActivity().tryRemoveTab(getTag());
        }
    }

    private void showThemeBody(String body) {
        try {
            setTitle(m_Title);
            m_WebView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", body, "text/html", "UTF-8", null);
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private class getEditProfileTask extends AsyncTask<String, String, Boolean> {

        getEditProfileTask() {
        }

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.getInstance();
                m_ThemeBody = transformBody(client.performGet(url).getResponseBody());

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        private String transformBody(String body) {
            HtmlBuilder builder = new HtmlBuilder();
            builder.beginHtml(m_Title);
            builder.beginBody("edit_profile");

            builder.append(parseBody(body));

            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        private String parseBody(String body) {
            Matcher m = PatternExtensions.compile("br \\/>\\s*(<fieldset>[\\S\\s]*<.form>)").matcher(body);
            if (m.find()) {
                body = "<form>" + m.group(1);
                //body =  + "</form><input type=\"button\" value=\"asdghjk\" onclick=\"jsonElem();\">";
                body = body.replaceAll("<td class=\"row1\" width=\"30%\"><b>О себе:</b>[\\s\\S]*?</td>",
                        "<td class=\"row1\" width=\"30%\"><b>О себе</b></td>");
                body = body.replaceAll("<td width=\"30%\" class=\"row1\" style='padding:6px;'><b>Город</b>[\\s\\S]*?</td>",
                        "<td class=\"row1\" width=\"30%\" style='padding:6px;'><b>Город</b></td>");
                body = body.replaceAll("legend", "h2").replaceAll("<fieldset>", "<div class=\"field\">").replaceAll("</fieldset>", "</div>");
                Document doc = Jsoup.parse(body);
                doc.select(".formbuttonrow .button").remove();
                doc.select(".formbuttonrow").append("<input type=\"button\" value=\"Сохранить\" onclick=\"jsonElem();\">");
                doc.select("textarea").first().attr("maxlength", "500");
                body = doc.html();
            }
            return body;
        }


        protected void onPreExecute() {
            setLoading(true);
        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            setLoading(false);

            if (isCancelled()) return;

            if (success) {
                showThemeBody(m_ThemeBody);
            } else {
                getSupportActionBar().setTitle(ex.getMessage());
                m_WebView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(getMainActivity(), ex);
            }

            CookieSyncManager syncManager = CookieSyncManager.createInstance(m_WebView.getContext());
            CookieManager cookieManager = CookieManager.getInstance();

                for (HttpCookie cookie : Client.getInstance().getCookies()) {

                    if (cookie.getDomain() != null) {
                        cookieManager.setCookie(cookie.getDomain(), cookie.getName() + "=" + cookie.getValue());
                    }
                    //cookieManager.setCookie(cookie.getTitle(),cookie.getValue());
                }
            syncManager.sync();
        }
    }
}
