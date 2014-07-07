package org.softeg.slartus.forpdaplus.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by Артём on 01.05.14.
 */
public class FavoritesPreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.favorites_list_prefs);
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.favorites_list_prefs, false);
    }
}

