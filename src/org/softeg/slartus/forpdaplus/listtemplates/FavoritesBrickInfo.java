package org.softeg.slartus.forpdaplus.listtemplates;


import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import org.softeg.slartus.forpdaplus.listfragments.FavoritesListFragment;

/*
 * Created by slinkin on 20.02.14.
 */
public class FavoritesBrickInfo extends BrickInfo {
    public static final String NAME = "Favorites";
    @Override
    public String getTitle() {
        return "Избранное";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return new FavoritesListFragment().setBrickInfo(this);
    }

    @Override
    public Boolean getNeedLogin() {
        return true;
    }


}
