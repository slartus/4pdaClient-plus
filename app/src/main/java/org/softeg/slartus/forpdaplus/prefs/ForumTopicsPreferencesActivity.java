package org.softeg.slartus.forpdaplus.prefs;
/*
 * Created by slinkin on 22.04.2014.
 */

import android.os.Bundle;

public class ForumTopicsPreferencesActivity extends BasePreferencesActivity {
    public static final int RESULT_OK = 1;
    public static final int RESULT_NONE = 0;

    private static final String ARGS_KEY = "ARGS_KEY";
    private Bundle mArgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_NONE);
        if (savedInstanceState != null)
            mArgs = savedInstanceState.getBundle(ARGS_KEY);
        else
            mArgs = getIntent().getExtras();

        ForumTopicsPreferencesFragment fragment = new ForumTopicsPreferencesFragment();
        fragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                fragment).commitAllowingStateLoss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(ARGS_KEY, mArgs);
    }

}

