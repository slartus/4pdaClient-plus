package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 10.04.2014.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.users.LeadersListFragment;

public class LeadsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Администрация";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_account_multiple_grey600_24dp;
    }

    @Override
    public String getName() {
        return "Leads";
    }

    @Override
    public Fragment createFragment() {
        return new LeadersListFragment().setBrickInfo(this);
    }
}
