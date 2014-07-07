package org.softeg.slartus.forpdaplus.profile;/*
 * Created by slinkin on 17.04.2014.
 */


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.qms.QmsChatActivity;
import org.softeg.slartus.forpdaplus.qms.QmsContactThemesActivity;

import java.util.regex.Matcher;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ProfileWebViewFragment extends DialogFragment {

    private static final String TAG = "ProfileWebViewFragment";
    private WebView m_WebView;
    private Task mTask;


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

    protected uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout mPullToRefreshLayout;

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
                    Log.e(getActivity(), ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == Activity.RESULT_OK &&requestCode == FILECHOOSER_RESULTCODE) {
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
            mPullToRefreshLayout = createPullToRefreshLayout(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mTask != null)
            mTask.cancel(null);
    }

    protected PullToRefreshLayout createPullToRefreshLayout(View view) {

        // This is the View which is created by ListFragment

        // We need to create a PullToRefreshLayout manually
        PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create().scrollDistance(0.3f).refreshOnUp(true).build())
                        // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .allChildrenArePullable()

                        // We can now complete the setup as desired
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        startLoadData();
                    }
                })
                .setup(pullToRefreshLayout);
        return pullToRefreshLayout;
    }

    private void startLoadData() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                mTask = new Task(getUserId());
                mTask.execute();
            }
        };
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED)
            mTask.cancel(runnable);
        else {
            runnable.run();
        }
    }

    private void setLoading(Boolean loading) {
        try {
            if (getActivity() == null) return;
            if (!isDialog())
                mPullToRefreshLayout.setRefreshing(loading);

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


    private class ProfileHtmlBuilder extends HtmlBuilder {
        @Override
        protected String getStyle() {
            return "/android_asset/profile/css/" + (MyApp.getInstance().isWhiteTheme() ? "profile_white.css" : "profile_black.css");

        }
    }

    public class Task extends AsyncTask<Boolean, Void, Profile> {

        private Runnable onCancelAction;
        protected Throwable mEx;
        private CharSequence userId;

        public Task(CharSequence userId) {

            this.userId = userId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setLoading(true);
        }

        public void cancel(Runnable runnable) {
            onCancelAction = runnable;

            cancel(false);
        }

        @Override
        protected Profile doInBackground(Boolean[] p1) {
            try {
                Profile profile = ProfileApi.getProfile(Client.getInstance(), userId);
                ProfileHtmlBuilder builder = new ProfileHtmlBuilder();
                builder.beginHtml(profile.getNick().toString());
                builder.beginBody();
                builder.append(profile.getHtmlBody());
                builder.endBody();
                builder.endHtml();
                profile.setHtmlBody(builder.getHtml().toString());
                return profile;
            } catch (Throwable e) {
                mEx = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            if (result != null && !isCancelled()) {
                deliveryResult(result);
            }
            if (!isCancelled())
                setLoading(false);

            if (mEx != null)
                Log.e(getActivity(), mEx, new Runnable() {
                    @Override
                    public void run() {
                        startLoadData();
                    }
                });
        }

        @Override
        protected void onCancelled(Profile result) {
            if (onCancelAction != null)
                onCancelAction.run();
        }

        @Override
        protected void onCancelled() {
            if (onCancelAction != null)
                onCancelAction.run();
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
}
