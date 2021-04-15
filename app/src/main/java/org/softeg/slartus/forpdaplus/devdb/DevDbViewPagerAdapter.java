package org.softeg.slartus.forpdaplus.devdb;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.softeg.slartus.forpdaplus.devdb.fragments.CommentsFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.DiscussionFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.FirmwareFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.PricesFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.ReviewsFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.SpecFragment;
import org.softeg.slartus.forpdaplus.devdb.fragments.base.BaseDevDbFragment;
import org.softeg.slartus.forpdaplus.devdb.helpers.ParsedModel;

import java.util.HashMap;
import java.util.Map;

public class DevDbViewPagerAdapter extends FragmentPagerAdapter {

    private Map<Integer, BaseDevDbFragment> tabs;
    private final Context c;

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
        tabs.put(0, SpecFragment.newInstance(context, parsed.getSpecModel()));
        tabs.put(1, CommentsFragment.newInstance(context, parsed.getCommentsModels()));
        tabs.put(2, DiscussionFragment.newInstance(context, parsed.getDiscussionModels()));
        tabs.put(3, ReviewsFragment.newInstance(context, parsed.getReviewsModels()));
        tabs.put(4, FirmwareFragment.newInstance(context, parsed.getFirmwareModels()));
        tabs.put(5, PricesFragment.newInstance(context, parsed.getPricesModels()));

    }
}
