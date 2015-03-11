package org.softeg.browser.pageviewcontrol.htmloutinterfaces;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

/*
 * Created by slinkin on 06.10.2014.
 */
public class AppWebChromeClient extends WebChromeClient {
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        Log.d("AppWebChromeClient", message + " -- From line "
                + lineNumber + " of "
                + sourceID);
    }
    public boolean onConsoleMessage(ConsoleMessage cm) {
        Log.d("AppWebChromeClient", cm.message() + " -- From line "
                + cm.lineNumber() + " of "
                + cm.sourceId() );
        return true;
    }
}
