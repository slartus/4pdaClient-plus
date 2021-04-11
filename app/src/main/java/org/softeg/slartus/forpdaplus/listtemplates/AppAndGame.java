package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 01.12.15.
 */
public class AppAndGame extends BrickInfo{
    public static final String NAME = "AppAndGame";
    @Override
    public String getTitle() {
        return "App&Game 4pda";
    }

    @Override
    public int getIcon() {
        return R.drawable.apps;
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
