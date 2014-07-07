package org.softeg.slartus.forpdaplus.prefs;
/*
 * Created by slinkin on 22.04.2014.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;

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

