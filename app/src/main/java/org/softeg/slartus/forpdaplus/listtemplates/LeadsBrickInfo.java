package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 10.04.2014.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.users.LeadersListFragment;

public class LeadsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.administration);
    }

    @Override
    public int getIcon() {
        return R.drawable.account_multiple;
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
