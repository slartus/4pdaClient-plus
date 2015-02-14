package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.listfragments.news.NewsNavigationFragment;

/**
 * Created by slinkin on 20.02.14.
 */
public class NewsPagerBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Новости";
    }

    @Override
    public String getName() {
        return "News_Pages";
    }

    @Override
    public Fragment createFragment() {
        return new NewsNavigationFragment(this);
    }
}
