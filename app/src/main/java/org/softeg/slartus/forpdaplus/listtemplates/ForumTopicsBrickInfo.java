package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment;

/**
 * Created by slinkin on 03.03.14.
 */
public class ForumTopicsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Темы форума";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_delete;
    }

    @Override
    public String getName() {
        return "ForumTopics";
    }

    @Override
    public Fragment createFragment() {
        return new ForumTopicsListFragment().setBrickInfo(this);
    }


}
