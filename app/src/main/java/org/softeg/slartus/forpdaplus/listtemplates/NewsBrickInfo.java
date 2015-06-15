package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.news.NewsListFragment;

/**
 * Created by slinkin on 20.02.14.
 */
public class NewsBrickInfo extends BrickInfo {
    private String mTag;

    public NewsBrickInfo(String tag){
        mTag = tag;
    }
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
        return "news_";
    }

    @Override
    public Fragment createFragment() {
        return new NewsListFragment().setBrickInfo(this);
    }
}
