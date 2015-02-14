package org.softeg.slartus.forpdaplus.listtemplates;
/*
 * Created by slinkin on 18.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.listfragments.DigestTopicsListFragment;

public class DigestTopicsListBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Дайджест игр и приложений";
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
