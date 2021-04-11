package org.softeg.slartus.forpdaplus.listtemplates;/*
 * Created by slinkin on 13.03.14.
 */

import androidx.fragment.app.Fragment;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.DevDbModelsFragment;

public class DevDbModelsBrickInfo extends BrickInfo {
    @Override
    public String getTitle() {
        return "DevDb";
    }

    @Override
    public int getIcon() {
        return R.drawable.cellphone_android;
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

