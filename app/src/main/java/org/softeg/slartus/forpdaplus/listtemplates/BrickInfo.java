package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

/**
 * Created by slinkin on 20.02.14.
 */
public abstract class BrickInfo {
    public abstract String getTitle();

    public abstract String getName();

    public Boolean getNeedLogin() {
        return false;
    }

    public abstract Fragment createFragment();
}
