package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 01.12.15.
 */
public class AppAndGame extends BrickInfo{
    @Override
    public String getTitle() {
        return "App&Game 4pda";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_apps_grey600_24dp;
    }

    @Override
    public String getName() {
        return "AppAndGame";
    }

    @Override
    public Fragment createFragment() {
        return null;
    }
}
