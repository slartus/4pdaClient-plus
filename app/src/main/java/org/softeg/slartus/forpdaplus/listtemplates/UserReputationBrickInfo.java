package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 23.04.2014.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;

public class UserReputationBrickInfo extends BrickInfo {
    public static final String NAME="UserReputation";
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.reputation);
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
        return new UserReputationFragment().setBrickInfo(this);
    }
}

