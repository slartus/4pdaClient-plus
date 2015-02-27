package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.listfragments.DevDbCatalogFragment;

/*
 * Created by slartus on 06.03.14.
 */
public class DevDbCatalogBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "DevDB";
    }

    @Override
    public String getName() {
        return "devdb_catalog";
    }

    @Override
    public Fragment createFragment() {
        return new DevDbCatalogFragment().setBrickInfo(this);
    }
}
