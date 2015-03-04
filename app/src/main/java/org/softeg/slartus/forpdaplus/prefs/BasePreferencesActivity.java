package org.softeg.slartus.forpdaplus.prefs;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;

import org.softeg.slartus.forpdaplus.App;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by slinkin on 27.12.13.
 */
public class BasePreferencesActivity extends PreferenceActivity {
    protected Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(App.getInstance().getThemeStyleResID());
        super.onCreate(savedInstanceState);


    }

    private static List<String> fragments = new ArrayList<String>();

    @Override
    public void loadHeadersFromResource(int resid, List<Header> target) {

        super.loadHeadersFromResource(resid, target);
        fragments.clear();
        for (Header header : target) {
            fragments.add(header.fragment);
        }
    }


    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragments.contains(fragmentName);
    }
}
