package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 17.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.listfragments.AppsGamesCatalogFragment;

public class AppsGamesCatalogBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "Каталог игр и приложений";
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
