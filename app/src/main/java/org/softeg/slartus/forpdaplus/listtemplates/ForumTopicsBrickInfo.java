package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment;

/*
 * Created by slinkin on 03.03.14.
 */
public class ForumTopicsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.forum_topics);
    }

    @Override
    public int getIcon() {
        return R.drawable.close_white;
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
