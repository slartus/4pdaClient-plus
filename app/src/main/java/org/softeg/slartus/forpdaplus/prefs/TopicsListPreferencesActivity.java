package org.softeg.slartus.forpdaplus.prefs;/*
 * Created by slinkin on 16.04.2014.
 */

import android.os.Bundle;

public class TopicsListPreferencesActivity extends BasePreferencesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new TopicsPreferenceFragment()).commitAllowingStateLoss();
    }

}
