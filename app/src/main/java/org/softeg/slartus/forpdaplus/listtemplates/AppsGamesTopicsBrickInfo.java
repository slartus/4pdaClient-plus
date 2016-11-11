package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 17.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.AppsGamesTopicsListFragment;

public class AppsGamesTopicsBrickInfo extends BrickInfo {

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.apps_games_topics);
    }

    @Override
    public int getIcon() {
        return R.drawable.apps;
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
