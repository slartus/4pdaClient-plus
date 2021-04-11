package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 20.03.14.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicsHistoryListFragment;

public class TopicsHistoryBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.topics_history);
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
