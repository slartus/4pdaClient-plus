package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 17.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.AppsGamesTopicsListFragment;

public class AppsGamesTopicsBrickInfo extends BrickInfo {

    @Override
    public String getTitle() {
        return "Каталог@темы";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_apps_grey600_24dp;
    }

    @Override
    public String getName() {
        return "AppsGamesTopics";
    }

    @Override
    public Fragment createFragment() {
        return new AppsGamesTopicsListFragment().setBrickInfo(this);
    }
}
