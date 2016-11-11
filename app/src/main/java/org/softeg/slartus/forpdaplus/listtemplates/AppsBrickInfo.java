package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.AppsListFragment;

/**
 * Created by slinkin on 20.02.14.
 */
public class AppsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.my_apps);
    }

    @Override
    public int getIcon() {
        return R.drawable.apps;
    }

    @Override
    public String getName() {
        return "Applications";
    }

    @Override
    public Fragment createFragment() {
        return new AppsListFragment().setBrickInfo(this);
    }
}
