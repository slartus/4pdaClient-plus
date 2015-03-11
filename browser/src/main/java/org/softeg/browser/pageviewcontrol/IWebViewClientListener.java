package org.softeg.browser.pageviewcontrol;

import android.content.Context;
import android.os.Bundle;


/*
 * Created by slinkin on 02.10.2014.
 */
public interface IWebViewClientListener {
    void setProgressBarIndeterminateVisibility(boolean b);


    void loadData(String url, int pageNum);

    void postData(String postUrl, Bundle postArgs, String topicPageUrl);

    Context getContext();

    AppWebView getWebView();
}
