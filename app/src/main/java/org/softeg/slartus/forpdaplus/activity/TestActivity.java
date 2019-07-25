package org.softeg.slartus.forpdaplus.activity;/*
 * Created by slinkin on 14.05.2014.
 */

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;

import org.softeg.slartus.forpdaplus.R;

public class TestActivity extends FragmentActivity {

    protected boolean isTransluent() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.test);

        ((WebView)findViewById(R.id.wvBody)).loadUrl("http://stackoverflow.com/");
    }


}
