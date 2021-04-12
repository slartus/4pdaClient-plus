package org.softeg.slartus.forpdaplus.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

/*
 * Created by Артём on 01.05.14.
 */
public class ForumTopicsPreferencesFragment extends PreferenceFragment {
    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity() != null;
        Bundle args = getArguments();
        SharedPreferences preferences = App.getInstance().getPreferences();
        if (args != null) {
            String listName = args.getString("listname");
            assert listName != null;
            assert !"".equals(listName);

            setKey(preferences, "sort_key", listName,"last_post");
            setKey(preferences, "sort_by", listName,"Z-A");
            setKey(preferences, "prune_day", listName,"100");
            setKey(preferences, "topicfilter", listName,"all");
            setKey(preferences, "unread_in_top", listName,"false");
        }
        PreferenceManager.setDefaultValues(getActivity(), R.xml.forum_topics_list_prefs, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.forum_topics_list_prefs);


    }

    @SuppressWarnings("ConstantConditions")
    private void setKey(SharedPreferences preferences, String prefsKey, String listName, String defValue) {
        Preference pref = findPreference(prefsKey);
        String newKey = listName + "." + prefsKey;

        if (pref instanceof ListPreference) {
            if (preferences.getString(newKey, null) == null) {

                preferences.edit().putString(newKey, defValue).apply();
            }
        } else if (pref instanceof CheckBoxPreference) {
            Boolean defSValue = Boolean.parseBoolean(defValue);

            ((CheckBoxPreference) pref).setChecked(preferences.getBoolean(newKey, defSValue));

        }

        pref.setKey(newKey);
        pref.setOnPreferenceChangeListener((preference, o) -> {
            getActivity().setResult(ForumTopicsPreferencesActivity.RESULT_OK);
            return true;
        });


    }
}
