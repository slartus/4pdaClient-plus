package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 30.01.16.
 */
public class PreferencesBrickInfo extends BrickInfo {
    public static final String NAME = "Preferences";

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.program_settings);
    }

    @Override
    public int getIcon() {
        return R.drawable.settings_grey;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return null;
    }
}
