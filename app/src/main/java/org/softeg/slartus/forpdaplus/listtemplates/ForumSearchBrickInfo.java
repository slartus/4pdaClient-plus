package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 07.04.2014.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

public class ForumSearchBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.search_in_forum);
    }

    @Override
    public int getIcon() {
        return R.drawable.close_white;
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
