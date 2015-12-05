package org.softeg.slartus.forpdaplus.fragments.profile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.Profile;
import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes;
import org.softeg.slartus.forpdaplus.fragments.qms.QmsNewThreadFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by radiationx on 31.10.15.
 */
public class ProfileFragment extends WebViewFragment implements LoaderManager.LoaderCallbacks<Profile>{
    public static final String USER_ID_KEY = "UserIdKey";
    public static final String USER_NAME_KEY = "UserNameKey";
    private Menu menu;
    private String title;
    private Handler mHandler = new Handler();

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
        return title;
    }

    @Override
    public String getUrl() {
        return "http://4pda.ru/forum/index.php?showuser=" + getUserId();
    }

    @Override
    public void reload() {
        startLoadData();
    }

    @Override
    public AsyncTask getAsyncTask() {
        return null;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private static final String TAG = "ProfileWebViewFragment";
    private static AdvWebView m_WebView;


    protected Bundle args;

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = new Bundle();
        if (getArguments() != null) {
            args = getArguments();
        }
        if (savedInstanceState != null) {
            args = savedInstanceState;
        }


    }

    private String getUserId() {
        return args.getString(USER_ID_KEY);
    }

    private String getUserNick() {
        return args.getString(USER_NAME_KEY, "");
    }

    private Boolean isDialog() {
        return args.getBoolean("DIALOG", false);
    }

    @Override
    public AdvWebView getWebView(){
        return m_WebView;
    }

    public static void showProfile(String userId, String userNick){
        MainActivity.addTab(userNick, "http://4pda.ru/forum/index.php?showuser=" + userId, newInstance(userId, userNick));
    }
    public static ProfileFragment newInstance(String userId, String userNick) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId);
        args.putString(USER_NAME_KEY, userNick);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startLoadData();
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);


        super.onSaveInstanceState(outState);
    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_web_view_fragment, container, false);
        initSwipeRefreshLayout();
        // getDialog().setTitle("Профиль");
        setHasOptionsMenu(true);
        assert view != null;
        m_WebView = (AdvWebView) view.findViewById(R.id.wvBody);
        registerForContextMenu(m_WebView);
        m_WebView.getSettings().setLoadWithOverviewMode(false);
        m_WebView.getSettings().setUseWideViewPort(true);
        m_WebView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        m_WebView.addJavascriptInterface(this, "HTMLOUT");
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                m_WebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable e) {
                android.util.Log.e(TAG, e.getMessage());
            }
        }
        m_WebView.setWebViewClient(new MyWebViewClient());
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = m_WebView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default:
                ExtUrl.showSelectActionDialog(mHandler, getMainActivity(),
                        getTitle(), "", hitTestResult.getExtra(), "", "", "", "", "");
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
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getMainActivity(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            if (Build.VERSION.SDK_INT < 19)
                m_WebView.loadUrl("javascript:window['HtmlInParseLessContent']('" + cssData + "');");
            else
                m_WebView.evaluateJavascript("window['HtmlInParseLessContent']('" + cssData + "')",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        }
                );
        }
    }


    protected void startLoadData() {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, getUserId());
        setLoading(true);
        if (getLoaderManager().getLoader(ItemsLoader.ID) != null)
            getLoaderManager().restartLoader(ItemsLoader.ID, args, this);
        else
            getLoaderManager().initLoader(ItemsLoader.ID, args, this);
    }

    private void showBody(Profile profile) {
        title = profile.getNick().toString();
        super.showBody();
        m_WebView.loadDataWithBaseURL("http://4pda.ru/forum/", profile.getHtmlBody(), "text/html", "UTF-8", null);
        if (profile.getNick() != null)
            args.putString(USER_NAME_KEY, profile.getNick().toString());
        if (getMainActivity() != null)
            getMainActivity().setTitle(profile.getNick());

    }

    @Override
    public Loader<Profile> onCreateLoader(int id, Bundle args) {
        ItemsLoader loader = null;
        if (id == ItemsLoader.ID) {
            setLoading(true);
            loader = new ItemsLoader(getMainActivity(), args);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Profile> loader, Profile data) {
        if (data != null && data.getError() != null) {
            AppLog.e(getMainActivity(), data.getError());
        } else if (data != null) {
            showBody(data);
        }


        setLoading(false);
    }

    @Override
    public void onLoaderReset(Loader<Profile> loader) {
        setLoading(false);
    }


    private static class ProfileHtmlBuilder extends HtmlBuilder {
        @Override
        protected String getStyle() {
            //return "/android_asset/profile/css/" + (App.getInstance().isWhiteTheme() ? "profile_white.css" : "profile_black.css");
            return App.getInstance().getThemeCssFileName();

        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if (tryShowQms_2_0(getMainActivity(), url))
                return true;
            if (IntentActivity.tryShowUrl(getMainActivity(), new Handler(), url, true, false,
                    Client.getInstance().getAuthKey()))
                return true;

            return true;
        }

        public boolean tryShowQms_2_0(Activity context, String url) {
            Matcher m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms&mid=(\\d+)&t=(\\d+)").matcher(url);
            if (m.find()) {
                //QmsChatActivity.openChat(context, m.group(1), getUserNick(), m.group(2), null);
                QmsChatFragment.openChat(m.group(1), getUserNick(), m.group(2), null);


                return true;
            }
            m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms&mid=(\\d+)").matcher(url);
            if (m.find()) {
                //QmsContactThemesActivity.showThemes(context, m.group(1), getUserNick());

                QmsContactThemes.showThemes(m.group(1), getUserNick());


                return true;
            }
            return false;
        }
    }

    private static class ItemsLoader extends AsyncTaskLoader<Profile> {
        public static final int ID = App.getInstance().getUniqueIntValue();
        Profile mApps;
        private Bundle args;

        public ItemsLoader(Context context, Bundle args) {
            super(context);

            this.args = args;
        }

        public Bundle getArgs() {
            return args;
        }


        @Override
        public Profile loadInBackground() {
            try {
                Profile profile = ProfileApi.getProfile(Client.getInstance(),
                        args.getString(USER_ID_KEY),
                        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("isSquareAvarars", false) ? "" : "circle");
                ProfileHtmlBuilder builder = new ProfileHtmlBuilder();
                builder.beginHtml(profile.getNick().toString());
                builder.beginBody("profile");
                builder.append(profile.getHtmlBody());
                builder.append("<script>\n" +
                        "        (function () {\n" +
                        "            var trans = document.querySelectorAll(\"input[type=radio]\")\n" +
                        "            for (var i = 0, len = trans.length; i < len; i++) {\n" +
                        "                trans[i].onchange = function () {\n" +
                        "                    window.HTMLOUT.setPrimaryDevice(this.value)\n" +
                        "                };\n" +
                        "            }\n" +
                        "        }());\n" +
                        "    </script>");
                builder.endBody();
                builder.endHtml();
                profile.setHtmlBody(builder.getHtml().toString());
                return profile;
            } catch (Throwable e) {
                Profile res = new Profile();
                res.setError(e);

                return res;
            }

        }

        @Override
        public void deliverResult(Profile apps) {

            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

        }

        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            if (takeContentChanged() || mApps == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }


        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                mApps = null;
            }
        }

    }

    public void run(final Runnable runnable) {
        if (Build.VERSION.SDK_INT < 17) {
            runnable.run();
        } else {
            getMainActivity().runOnUiThread(runnable);
        }
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SaveHtml(getMainActivity(),html,"Profile");
            }
        });
    }
    @JavascriptInterface
    public void setPrimaryDevice(final String id) {
        run(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> additionalHeaders = new HashMap<String, String>();
                        additionalHeaders.put("auth_key", Client.getInstance().getAuthKey());
                        try {
                            Client.getInstance().performPost("http://4pda.ru/forum/index.php?act=profile-xhr&action=dev-primary&md_id=" + id, additionalHeaders);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Toast.makeText(getMainActivity(), "Основное устройство изменено", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        MenuItem item;

        if (Client.getInstance().getLogined() && getUserId() != null && !getUserId().equals(Client.getInstance().UserId)) {
            item = menu.add(getString(R.string.MessagesQms)).setIcon(R.drawable.ic_pencil_white_24dp);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    QmsNewThreadFragment.showUserNewThread(getMainActivity(), getUserId(), getUserNick());

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }


        item = menu.add(getString(R.string.Reputation));
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                CharSequence[] items = {"Поднять", "Опустить", "Посмотреть", "Кому изменял репутацию"};
                new MaterialDialog.Builder(getMainActivity())
                        .title("Репутация")
                        .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int i, CharSequence items) {
                                switch (i) {
                                    case 0:
                                        UserReputationFragment.plusRep(getMainActivity(), new Handler(), getUserId(), getUserNick());
                                        break;
                                    case 1:
                                        UserReputationFragment.minusRep(getMainActivity(), new Handler(), getUserId(), getUserNick());
                                        break;
                                    case 2:
                                        UserReputationFragment.showActivity(getMainActivity(), getUserId(), false);
                                        break;
                                    case 3:
                                        UserReputationFragment.showActivity(getMainActivity(), getUserId(), true);
                                        break;
                                }
                            }
                        })
                        .show();

                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        item = menu.add(getString(R.string.FindUserTopics));
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserTopicsSearchSettings(getUserNick()));
                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        item = menu.add(getString(R.string.FindUserPosts));
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                MainActivity.startForumSearch(SearchSettingsDialogFragment.createUserPostsSearchSettings(getUserNick()));
                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        item = menu.add("Ссылка на профиль");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                ExtUrl.showSelectActionDialog(getMainActivity(), "Ссылка на профиль", "http://4pda.ru/forum/index.php?showuser=" + getUserId());
                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

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
            getWebView().loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }
}
