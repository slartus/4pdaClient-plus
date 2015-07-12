package org.softeg.slartus.forpdaplus.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.topicview.HtmloutWebInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by radiationx on 12.07.15.
 */
public class ProfileEditActivity extends BaseFragmentActivity {

    private static WebView m_WebView;
    private Handler mHandler = new android.os.Handler();
    public String m_Title;
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProfileEditActivity.class);

        //intent.putExtra(USER_ID_KEY, userId);
        //intent.putExtra(USER_NAME_KEY, userName);
        //intent.putExtra("activity", context.getClass().toString());
        context.startActivity(intent);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_activity);
        m_WebView = (WebView) findViewById(R.id.wvBody);
        m_WebView.getSettings().setLoadWithOverviewMode(false);
        m_WebView.getSettings().setUseWideViewPort(true);
        m_WebView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        m_WebView.setWebViewClient(new MyWebViewClient());
        m_WebView.addJavascriptInterface(this, "HTMLOUT");
        GetNewsTask getThemeTask = new GetNewsTask(this);
        getThemeTask.execute("".replace("|", ""));
        createActionMenu();
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
    private void showThemeBody(String body) {
        try {

            setTitle(m_Title);
            m_WebView.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);


        } catch (Exception ex) {
            AppLog.e(this, ex);
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
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }
        }

        private String transformBody(String body) {
            HtmlBuilder builder = new HtmlBuilder();
            Matcher matcher = PatternExtensions.compile("<title>([^<>]*)</title>").matcher(body);
            ;
            m_Title = "ФЛОАПОЛДЫП";
            if (matcher.find()) {
                m_Title = Html.fromHtml(matcher.group(1)).toString();
            }
            builder.beginHtml(m_Title);
            builder.beginBody("edit_profile");

            builder.append(parseBody(body));

            builder.endBody();
            builder.endHtml();
            return builder.getHtml().toString();
        }

        private String parseBody(String body) {
            Matcher m = PatternExtensions.compile("<div style='padding:6px;'>([\\s\\S]*?)</form>").matcher(body);

            if (m.find()) {
                body = m.group(1)+"</form>";
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
            Comment = null;
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
        }


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

            //NewsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


            //NewsActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            IntentActivity.tryShowUrl(ProfileEditActivity.this, mHandler, url, true, false);

            return true;
        }
    }
}
