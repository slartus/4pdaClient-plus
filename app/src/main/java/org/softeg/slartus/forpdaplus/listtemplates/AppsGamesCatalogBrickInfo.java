package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 17.03.14.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.AppsGamesCatalogFragment;

public class AppsGamesCatalogBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.catalog_games_and_apps);
    }

    @Override
    public int getIcon() {
        return R.drawable.apps;
    }

    @Override
    public String getName() {
        return "AppsGamesCatalog";
    }

    @Override
    public Fragment createFragment() {
        return new AppsGamesCatalogFragment().setBrickInfo(this);
    }
}
