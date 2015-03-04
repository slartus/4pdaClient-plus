package org.softeg.slartus.forpdaplus.prefs;
/*
 * Created by slinkin on 22.04.2014.
 */

import android.os.Bundle;

public class ForumTopicsPreferencesActivity extends BasePreferencesActivity {
    public static final int RESULT_OK = 1;
    public static final int RESULT_NONE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_NONE);
        ForumTopicsPreferencesFragment fragment = new ForumTopicsPreferencesFragment();
        fragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                fragment).commit();
    }
}

