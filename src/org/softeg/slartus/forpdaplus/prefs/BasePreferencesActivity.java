package org.softeg.slartus.forpdaplus.prefs;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;

import org.softeg.slartus.forpdaplus.MyApp;

/**
 * Created by slinkin on 27.12.13.
 */
public class BasePreferencesActivity extends PreferenceActivity {
    protected Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MyApp.getInstance().getThemeStyleResID());
        super.onCreate(savedInstanceState);

        
    }


}
