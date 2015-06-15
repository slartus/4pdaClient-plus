package org.softeg.slartus.forpdaplus.profile;/*
 * Created by slinkin on 17.04.2014.
 */


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.Profile;
import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.qms.QmsChatActivity;
import org.softeg.slartus.forpdaplus.qms.QmsContactThemesActivity;

import java.util.regex.Matcher;

public class ProfileWebViewFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Profile> {

    private static final String TAG = "ProfileWebViewFragment";
    private WebView m_WebView;


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
        return args.getString(ProfileWebViewActivity.USER_ID_KEY);
    }

    private String getUserNick() {
        return args.getString(ProfileWebViewActivity.USER_NAME_KEY, "");
    }

    private Boolean isDialog() {
        return args.getBoolean("DIALOG", false);
    }

    public static void showDialog(FragmentActivity fragmentActivity, String userId, String userNick) {
        DialogFragment newFragment = ProfileWebViewFragment.newInstance(userId, userNick);
        newFragment.getArguments().putBoolean("DIALOG", true);
        newFragment.show(fragmentActivity.getSupportFragmentManager(), "dialog");
    }

    private static ProfileWebViewFragment newInstance(String userId, String userNick) {
        Bundle args = new Bundle();
        args.putString(ProfileWebViewActivity.USER_ID_KEY, userId);
        args.putString(ProfileWebViewActivity.USER_NAME_KEY, userNick);
        ProfileWebViewFragment fragment = new ProfileWebViewFragment();
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

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_web_view_fragment, container, false);
        // getDialog().setTitle("Профиль");
        assert view != null;
        m_WebView = (WebView) view.findViewById(R.id.wvBody);
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

    private final static int FILECHOOSER_RESULTCODE = 1;

    @JavascriptInterface
    public void showChooseCssDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getActivity(), ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getActivity(), data.getData());
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isDialog())
            mSwipeRefreshLayout = App.getInstance().createSwipeRefreshLayout(getActivity(), view, new Runnable() {
                @Override
                public void run() {
                    startLoadData();
                }
            });
    }

    private void startLoadData() {
        Bundle args = new Bundle();
        args.putString(ProfileWebViewActivity.USER_ID_KEY, getUserId());
        setLoading(true);
        if (getLoaderManager().getLoader(ItemsLoader.ID) != null)
            getLoaderManager().restartLoader(ItemsLoader.ID, args, this);
        else
            getLoaderManager().initLoader(ItemsLoader.ID, args, this);
    }

    private void setLoading(final Boolean loading) {
        try {
            if (getActivity() == null) return;
            if (!isDialog()) {
                //mSwipeRefreshLayout.setRefreshing(loading);
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(loading);
                    }
                });
            }

        } catch (Throwable ignore) {
            android.util.Log.e("TAG", ignore.toString());
        }
    }

    private void deliveryResult(Profile profile) {

        m_WebView.loadDataWithBaseURL("http://4pda.ru/forum/", profile.getHtmlBody(), "text/html", "UTF-8", null);
        if (profile.getNick() != null)
            args.putString(ProfileWebViewActivity.USER_NAME_KEY, profile.getNick().toString());
        if (getActivity() != null)
            getActivity().setTitle(profile.getNick());

    }

    @Override
    public Loader<Profile> onCreateLoader(int id, Bundle args) {
        ItemsLoader loader = null;
        if (id == ItemsLoader.ID) {
            setLoading(true);
            loader = new ItemsLoader(getActivity(), args);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Profile> loader, Profile data) {
        if (data != null && data.getError() != null) {
            AppLog.e(getActivity(), data.getError());
        } else if (data != null) {
            deliveryResult(data);
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
            if (tryShowQms_2_0(getActivity(), url))
                return true;
            if (IntentActivity.tryShowUrl(getActivity(), new Handler(), url, true, false,
                    Client.getInstance().getAuthKey()))
                return true;

            return true;
        }

        public boolean tryShowQms_2_0(Activity context, String url) {
            Matcher m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms&mid=(\\d+)&t=(\\d+)").matcher(url);
            if (m.find()) {
                QmsChatActivity.openChat(context, m.group(1), getUserNick(), m.group(2), null);

                return true;
            }
            m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms&mid=(\\d+)").matcher(url);
            if (m.find()) {
                QmsContactThemesActivity.showThemes(context, m.group(1), getUserNick());


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
                        args.getString(ProfileWebViewActivity.USER_ID_KEY));
                ProfileHtmlBuilder builder = new ProfileHtmlBuilder();
                builder.beginHtml(profile.getNick().toString());
                builder.beginBody();
                builder.append(profile.getHtmlBody());
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

}
