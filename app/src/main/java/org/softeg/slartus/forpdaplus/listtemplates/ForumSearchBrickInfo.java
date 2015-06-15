package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 07.04.2014.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;

public class ForumSearchBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Поиск по форуму";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_delete;
    }

    @Override
    public String getName() {
        return "Search";
    }

    @Override
    public Fragment createFragment() {
        return null;
    }
}
