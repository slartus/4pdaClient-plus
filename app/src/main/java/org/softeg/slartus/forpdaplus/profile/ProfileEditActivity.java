package org.softeg.slartus.forpdaplus.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * Created by radiationx on 12.07.15.
 */
public class ProfileEditActivity extends BaseFragmentActivity {

    private static WebView m_WebView;
    private Handler mHandler = new android.os.Handler();
    public String m_Title;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProfileEditActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_activity);
        m_WebView = (WebView) findViewById(R.id.wvBody);
        registerForContextMenu(m_WebView);

        m_WebView.getSettings();
        m_WebView.getSettings().setDomStorageEnabled(true);
        m_WebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        m_WebView.getSettings().setAppCachePath(appCachePath);
        m_WebView.getSettings().setAppCacheEnabled(true);

        m_WebView.getSettings().setAllowFileAccess(true);

        m_WebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        m_WebView.addJavascriptInterface(this, "HTMLOUT");
        m_WebView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());

        getEditProfileTask task = new getEditProfileTask(this);
        task.execute("".replace("|", ""));

        createActionMenu();

        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(this, "Режим разработчика", Toast.LENGTH_SHORT).show();
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

    @JavascriptInterface
    public void saveHtml(final String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SaveHtml(ProfileEditActivity.this, html, "EditProfile");
            }
        });
    }

    public void saveHtml() {
        try {
            m_WebView.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(this, ex);
        }
    }

    @JavascriptInterface
    public void sendProfile(final String json) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("asdasd", json);
                new editProfileTask(ProfileEditActivity.this,json).execute();
            }
        });
    }

    public static final class MenuFragment extends Fragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem item;
            if (Preferences.System.isDevSavePage()) {
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
            item = menu.add(R.string.Close).setIcon(R.drawable.ic_close_white_24dp);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    getActivity().finish();
                    return true;
                }
            });
        }

        public ProfileEditActivity getInterface() {
            return (ProfileEditActivity) getActivity();
        }
    }

    private class editProfileTask extends AsyncTask<String, Void, Void> {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        MaterialDialog dialog;

        public editProfileTask(Context context,String json) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Отправка данных")
                    .build();

            try {
                JSONArray jr = new JSONArray(json);
                JSONObject jb;
                for (int i = 0; i <jr.length(); i++){
                    jb = (JSONObject)jr.getJSONObject(i);
                    if(!jb.getString("name").equals("null")){
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
                Client.getInstance().performPost("http://4pda.ru/forum/index.php", additionalHeaders);
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
            Toast.makeText(getContext(),"Данные отправлены",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showThemeBody(String body) {
        try {
            setTitle(m_Title);
            m_WebView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);
        } catch (Exception ex) {
            AppLog.e(this, ex);
        }
    }

    private class getEditProfileTask extends AsyncTask<String, String, Boolean> {
        private final MaterialDialog dialog;

        public getEditProfileTask(Context context) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Загрузка")
                    .build();
        }

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.getInstance();
                m_ThemeBody = transformBody(client.performGet("http://4pda.ru/forum/index.php?act=UserCP&CODE=01"));

                return true;
            } catch (Throwable e) {
                return false;
            }
        }

        private String transformBody(String body) {
            HtmlBuilder builder = new HtmlBuilder();
            m_Title = "Изменить личные данные";
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
                body = "<form>"+m.group(1);
                //body =  + "</form><input type=\"button\" value=\"asdghjk\" onclick=\"jsonElem();\">";
                body = body.replaceAll("<td class=\"row1\" width=\"30%\"><b>О себе:</b>[\\s\\S]*?</td>",
                        "<td class=\"row1\" width=\"30%\"><b>О себе</b></td>");
                body = body.replaceAll("<td width=\"30%\" class=\"row1\" style='padding:6px;'><b>Город</b>[\\s\\S]*?</td>",
                                "<td class=\"row1\" width=\"30%\" style='padding:6px;'><b>Город</b></td>");
                body = body.replaceAll("legend","h2").replaceAll("<fieldset>","<div class=\"field\">").replaceAll("</fieldset>","</div>");
                Document doc = Jsoup.parse(body);
                doc.select(".formbuttonrow .button").remove();
                doc.select(".formbuttonrow").append("<input type=\"button\" value=\"Сохранить\" onclick=\"jsonElem();\">");
                doc.select("textarea").first().attr("maxlength","500");
                body = doc.html();
                /*
                for(Element fieldset:doc.select("fieldset")){
                    mBody.append("<div class=\"block\">");
                    mBody.append("<div class=\"block-header\">").append(fieldset.select("legend").html()).append("</div>");
                    for (Element row:fieldset.select("table tr")){
                        mBody.append("<div class=\"row\">");
                        mBody.append("<div class=\"head\">").append(row.select(".row1").html()).append("</div>");
                        mBody.append("<div class=\"content\">").append(row.select(".row2").html()).append("</div>");

                        Element el = row.select(".row2 [name]").first();

                        switch (el.attr("name")){
                            case "field_1":
                                getValue(el);
                                break;
                            case "field_5":
                                getValue(el);
                                break;
                            case "member_title":
                                getValue(el);
                                break;
                            case "day":

                                break;
                            case "month":

                                break;
                            case "year":

                                break;
                            case "gender":

                                break;
                            case "WebSite":
                                getValue(el);
                                break;
                            case "ICQNumber":

                                break;
                            case "AOLName":

                                break;
                            case "YahooName":

                                break;
                            case "MSNName":

                                break;
                            case "Links":

                                break;
                            case "Location":

                                break;
                            case "bio":

                                break;
                        }
                        mBody.append("</div>");
                    }
                    mBody.append("</div>");
                }
                //Log.d("rows!!!!!", fieldsets.size()+" "+rows.size());
                /*mBody.append("<div class=\"block\"><div class=\"heading\">");
                mBody.append(doc.select("fieldset:first-child legend").first().html()).append("</div>");
                mBody.append();*/
            }




            return body;
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
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e("!!@@#!", ex.toString());
            }

            if (isCancelled()) return;

            if (success) {
                showThemeBody(m_ThemeBody);
            } else {
                ProfileEditActivity.this.setTitle(ex.getMessage());
                m_WebView.loadDataWithBaseURL("\"file:///android_asset/\"", m_ThemeBody, "text/html", "UTF-8", null);
                AppLog.e(ProfileEditActivity.this, ex);
            }

            CookieSyncManager syncManager = CookieSyncManager.createInstance(m_WebView.getContext());
            CookieManager cookieManager = CookieManager.getInstance();
            try {
                for (Cookie cookie : Client.getInstance().getCookies()) {

                    if (cookie.getDomain() != null) {
                        Log.d("asdas!", cookie.getDomain() + " " + cookie.getValue());
                        cookieManager.setCookie(cookie.getDomain(), cookie.getName() + "=" + cookie.getValue());
                    }
                    //cookieManager.setCookie(cookie.getName(),cookie.getValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            syncManager.sync();
        }
    }
    private String getValue(Element element){
        return element.attr("value");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
            IntentActivity.tryShowUrl(ProfileEditActivity.this, mHandler, url, true, false);
            return true;
        }
    }

}
