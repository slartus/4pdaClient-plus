package org.softeg.slartus.forpdaplus.listtemplates;
/*
 * Created by slinkin on 18.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.DigestTopicsListFragment;

public class DigestTopicsListBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.digest_topics);
    }

    @Override
    public int getIcon() {
        return R.drawable.apps;
    }

    @Override
    public String getName() {
        return "DigestTopics";
    }

    @Override
    public Fragment createFragment() {
        return  new DigestTopicsListFragment().setBrickInfo(this);
    }
}
