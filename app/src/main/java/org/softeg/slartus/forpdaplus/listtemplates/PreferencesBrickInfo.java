package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 30.01.16.
 */
public class PreferencesBrickInfo extends BrickInfo {
    public static final String NAME = "Preferences";

    @Override
    public String getTitle() {
        return "Настройки программы";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_settings_grey600_24dp;
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
