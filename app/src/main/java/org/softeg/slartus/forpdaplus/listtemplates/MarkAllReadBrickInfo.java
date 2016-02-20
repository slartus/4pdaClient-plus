package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by radiationx on 30.01.16.
 */
public class MarkAllReadBrickInfo extends BrickInfo {
    public static final String NAME = "MarkAllRead";

    @Override
    public String getTitle() {
        return "Отметить весь форум прочитанным";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_check_all_grey600_24dp;
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
