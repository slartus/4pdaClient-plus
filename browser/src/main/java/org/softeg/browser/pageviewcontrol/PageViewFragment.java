package org.softeg.browser.pageviewcontrol;


import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import org.softeg.slartus.yarportal.App;
import org.softeg.slartus.yarportal.FragmentBase;
import org.softeg.slartus.yarportal.R;
import org.softeg.slartus.yarportal.pageviewcontrol.htmloutinterfaces.AppWebChromeClient;


/*
 * Created by slinkin on 07.10.2014.
 */
public class PageViewFragment extends FragmentBase implements View.OnClickListener {
    protected AppWebView mWebView;
    protected uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout mPullToRefreshLayout;

    protected int getViewResourceId() {
        return R.layout.topic_view_fragment;
    }

    protected Boolean autoHideActionBar(){
        return true;
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                                          Bundle savedInstanceState) {
        View v = inflater.inflate(getViewResourceId(), container, false);
        assert v != null;
        mWebView = (AppWebView) v.findViewById(R.id.webView);
        mWebView.initTopicPageWebView();
        if(autoHideActionBar())
            setHideActionBar();
        mWebView.setWebChromeClient(new AppWebChromeClient());
        if (getActivity() != null && getActivity().getActionBar() != null)
            mWebView.setActionBarheight(getActivity().getActionBar().getHeight());
        registerForContextMenu(mWebView);

        v.findViewById(R.id.btnUp).setOnClickListener(this);
        v.findViewById(R.id.btnDown).setOnClickListener(this);
        return v;
    }

    @Override
    public void onDestroy()
    {
        // null out before the super call
        if (mWebView != null)
        {
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.removeAllViews();
            mWebView.loadUrl("about:blank");
            mWebView = null;
        }
        super.onDestroy();
    }

    public AppWebView getWebView() {
        return mWebView;
    }


    public Context getContext() {
        return getActivity();
    }


    public Fragment getFragment() {
        return this;
    }

    public void setProgressBarIndeterminateVisibility(boolean b) {
        if (getActivity() != null)
            getActivity().setProgressBarIndeterminateVisibility(b);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPullToRefreshLayout = App.createPullToRefreshLayout(getActivity(), view, new Runnable() {
            @Override
            public void run() {
                reloadData();
            }
        });
    }

    public void reloadData() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDown:
                mWebView.pageDown(true);
                break;
            case R.id.btnUp:
                mWebView.pageUp(true);
                break;
        }
    }

    public void setHideActionBar() {
        if(!autoHideActionBar())return;
        if (getWebView() == null || !(getWebView() instanceof AppWebView))
            return;
        if (getActivity() == null)
            return;
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar == null) return;
        setHideActionBar(getWebView(), actionBar);
    }

    public static void setHideActionBar(AppWebView advWebView, final ActionBar actionBar) {
        advWebView.setOnScrollChangedCallback(new AppWebView.OnScrollChangedCallback() {
            @Override
            public void onScrollDown(Boolean inTouch) {
                if (!inTouch)
                    return;
                if (actionBar.isShowing()) {
                    actionBar.hide();
                }
            }

            @Override
            public void onScrollUp(Boolean inTouch) {
                if (!inTouch)
                    return;
                if (!actionBar.isShowing()) {

                    actionBar.show();
                }
            }

            @Override
            public void onTouch() {
                actionBar.show();
            }
        });

    }


}
