package org.softeg.slartus.forpdaplus.listtemplates;

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
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
    public int getIcon() {
        return R.drawable.cellphone_android;
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
