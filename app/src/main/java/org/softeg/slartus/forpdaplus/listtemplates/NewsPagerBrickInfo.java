package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.news.NewsListFragment;

import java.io.Serializable;

/*
 * Created by slinkin on 20.02.14.
 */
public class NewsPagerBrickInfo extends BrickInfo implements Serializable{
    @Override
    public String getTitle() {
        return "Новости";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_newspaper_grey600_24dp;
    }

    @Override
    public String getName() {
        return "News_Pages";
    }

    @Override
    public Fragment createFragment() {
        return NewsListFragment.newInstance("");
    }
}
