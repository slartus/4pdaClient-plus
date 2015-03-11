package org.softeg.browser.pageviewcontrol.htmloutinterfaces;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.softeg.slartus.yarportal.pageviewcontrol.AppWebView;


/*
 * Created by slinkin on 02.10.2014.
 */
public interface IHtmlOutListener{
    Context getContext();

    FragmentActivity getActivity();

    Fragment getFragment();

    AppWebView getWebView();

    void nextPage();

    void prevPage();

    void firstPage();

    void lastPage();

    void loadPage(int i);


    int getPagesCount();

    int getCurrentPage();
}
