package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 13.03.14.
 */

import android.support.v4.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.DevDbModelsFragment;

public class DevDbModelsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "DevDb.ru";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_cellphone_android_grey600_24dp;
    }

    @Override
    public String getName() {
        return "devdb_models";
    }

    @Override
    public Fragment createFragment() {
        return new DevDbModelsFragment().setBrickInfo(this);
    }
}

