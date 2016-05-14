package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 18.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.DigestCatalogFragment;

public class DigestCatalogBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return App.getContext().getString(R.string.digest_catalog);
    }

    @Override
    public int getIcon() {
        return R.drawable.apps;
    }

    @Override
    public String getName() {
        return "DigestCatalog";
    }

    @Override
    public Fragment createFragment() {
        return new DigestCatalogFragment().setBrickInfo(this);
    }
}
