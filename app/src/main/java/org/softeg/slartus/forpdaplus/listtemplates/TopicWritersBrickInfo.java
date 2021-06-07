package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;

/*
 * Created by slinkin on 17.06.2015.
 */
public class TopicWritersBrickInfo extends BrickInfo {
    public static final String NAME = "TopicWritersBrickInfo";

    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.topic_posters);
    }

    @Override
    public int getIcon() {
        return R.drawable.close_white;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return new TopicWritersListFragment().setBrickInfo(this);
    }
}
