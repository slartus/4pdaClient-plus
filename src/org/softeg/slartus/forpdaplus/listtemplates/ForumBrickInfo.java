package org.softeg.slartus.forpdaplus.listtemplates;

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.listfragments.ForumCatalogFragment;

/**
 * Created by slinkin on 21.02.14.
 */
public class ForumBrickInfo extends BrickInfo {
    public static final String NAME = "Forum";

    @Override
    public String getTitle() {
        return "Форум";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Fragment createFragment() {
        return new ForumCatalogFragment().setBrickInfo(this);
    }
}
