package org.softeg.slartus.forpdaplus.devdb;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.softeg.slartus.forpdaplus.devdb.fragments.CommentsFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.DiscussionFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.FirmwareFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.PricesFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.ReviewsFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.ParsedModel;

import java.util.HashMap;
import java.util.Map;

public class DevDbViewPagerAdapter extends FragmentPagerAdapter {

    private Map<Integer, BaseDevDbFragment> tabs;
    private Context c;

    public DevDbViewPagerAdapter(Context context, FragmentManager fm, ParsedModel parsed) {
        super(fm);
        this.c = context;
        initTabsMap(context, parsed);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    private void initTabsMap(Context context, ParsedModel parsed) {
        tabs = new HashMap<>();
        tabs.put(0, CommentsFragment.newInstance(context, parsed.getCommentsModels()));
        tabs.put(1, DiscussionFragment.newInstance(context, parsed.getDiscussionModels()));
        tabs.put(2, ReviewsFragment.newInstance(context, parsed.getReviewsModels()));
        tabs.put(3, FirmwareFragment.newInstance(context, parsed.getFirmwareModels()));
        tabs.put(4, PricesFragment.newInstance(context, parsed.getPricesModels()));
    }
}
