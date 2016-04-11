package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 20.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicsHistoryListFragment;

public class TopicsHistoryBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Посещенные темы";
    }

    @Override
    public int getIcon() {
        return R.drawable.forum_checked;
    }

    @Override
    public String getName() {
        return "TopicsHistory";
    }

    @Override
    public Fragment createFragment() {
        return new TopicsHistoryListFragment().setBrickInfo(this);
    }
}
