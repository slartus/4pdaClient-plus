package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

/**
 * Created by slinkin on 20.02.14.
 */
public abstract class BrickInfo {
    public abstract String getTitle();

    public abstract String getName();

    public abstract int getIcon();

    public Boolean getNeedLogin() {
        return false;
    }

    public abstract Fragment createFragment();

    public String toString(){
        return "{BrickInfo, Name="+getName()+", Title="+getTitle()+"}";
    }
}
