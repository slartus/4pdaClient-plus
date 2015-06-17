package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.TopicReadersListFragment;

/*
 * Created by slinkin on 17.06.2015.
 */
public class TopicReadersBrickInfo  extends BrickInfo {
    public static final String NAME = "TopicReadersBrickInfo";

    @Override
    public String getTitle() {
        return "Кто читает тему";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_delete;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return new TopicReadersListFragment().setBrickInfo(this);
    }
}