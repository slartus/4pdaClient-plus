package org.softeg.browser.pageviewcontrol.htmloutinterfaces;

import android.content.Intent;

/*
 * Created by slinkin on 02.10.2014.
 */
public interface IHtmlOut {
    void onActivityResult(int requestCode, int resultCode, Intent data);
    String getName();

    boolean hasRequestCode(int requestCode);
}
