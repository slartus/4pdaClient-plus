package org.softeg.slartus.forpdaplus.prefs;/*
 * Created by slinkin on 16.04.2014.
 */

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.R;

public class TopicsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public static String ListName = "";

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.topics_list_prefs);

        Preference sortPreference = findPreference("topics.list.sort");
        sortPreference.setOnPreferenceClickListener(this);
        sortPreference.setSummary(getSortTitle());
    }


    private String getSortTitle() {
        String title = "";
        Boolean asc = true;
        switch (Preferences.List.getListSort(getListName(), Preferences.List.defaultListSort())) {
            case "sortorder.desc":
                asc = false;
            case "sortorder.asc":
                title = getString(R.string.sort_like_on_website);
                break;
            case "date.desc":
                asc = false;
            case "date.asc":
                title = getString(R.string.sort_by_date_of_last_post);
                break;
            case "date_and_new.desc":
                asc = false;
            case "date_and_new.asc":
                title = getString(R.string.sort_by_date_of_last_post_and_read_status);
                break;
            case "title.desc":
                asc = false;
            case "title.asc":
                title = getString(R.string.sort_by_topic_title);
                break;
        }
        return String.format("%s (%s)", title, (asc ? getString(R.string.sort_ascending) : getString(R.string.sort_descending)));
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.topics_list_prefs, false);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {
            case "topics.list.sort":
                showSortDialog();
                return true;
        }
        return false;
    }

    private void showSortDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.topics_sort_dialog, null);
        assert v != null;
        final RadioGroup radioGroup = v.findViewById(R.id.rgSortType);

        switch (Preferences.List.getListSort(getListName(), Preferences.List.defaultListSort())) {
            case "sortorder.desc":
            case "sortorder.asc":
                ((RadioButton) radioGroup.findViewById(R.id.rbSortBySortOrder)).setChecked(true);
                break;
            case "date.desc":
            case "date.asc":
                ((RadioButton) radioGroup.findViewById(R.id.rbSortByDate)).setChecked(true);
                break;
            case "date_and_new.desc":
            case "date_and_new.asc":
                ((RadioButton) radioGroup.findViewById(R.id.rbSortByDateAndNew)).setChecked(true);
                break;
            case "title.desc":
            case "title.asc":
                ((RadioButton) radioGroup.findViewById(R.id.rbSortByTitle)).setChecked(true);
                break;
            default:
                ((RadioButton) radioGroup.findViewById(R.id.rbSortBySortOrder)).setChecked(true);
                break;
        }

        new MaterialDialog.Builder(getActivity())
                .title(R.string.sort)
                .customView(v,true)
                .positiveText(R.string.sort_descending)
                .neutralText(R.string.sort_ascending)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String sortValue = "date";
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.rbSortBySortOrder:
                                sortValue = "sortorder";
                                break;
                            case R.id.rbSortByDate:
                                sortValue = "date";
                                break;
                            case R.id.rbSortByDateAndNew:
                                sortValue = "date_and_new";
                                break;
                            case R.id.rbSortByTitle:
                                sortValue = "title";
                                break;
                        }
                        Preferences.List.setListSort(getListName(), sortValue + ".desc");
                        Toast.makeText(getActivity(), R.string.need_refresh_list,
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        String sortValue = "date";
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.rbSortBySortOrder:
                                sortValue = "sortorder";
                                break;
                            case R.id.rbSortByDate:
                                sortValue = "date";
                                break;
                            case R.id.rbSortByDateAndNew:
                                sortValue = "date_and_new";
                                break;
                            case R.id.rbSortByTitle:
                                sortValue = "title";
                                break;
                        }
                        Preferences.List.setListSort(getListName(), sortValue + ".asc");
                        Toast.makeText(getActivity(), R.string.need_refresh_list,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private String getListName() {
        return ListName;
    }

}
