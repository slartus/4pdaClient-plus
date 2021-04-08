package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.news.NewsListFragment;

import java.io.Serializable;

/*
 * Created by slinkin on 20.02.14.
 */
public class NewsPagerBrickInfo extends BrickInfo implements Serializable{
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.news);
    }

    @Override
    public int getIcon() {
        return R.drawable.newspaper;
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
