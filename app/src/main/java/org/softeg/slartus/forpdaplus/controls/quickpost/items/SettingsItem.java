package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

/**
 * Created by slartus on 23.02.14.
 */
public class SettingsItem extends QuickPostItem {
    private SettingsQuickView view;
    @Override
    public int getTitle() {
        return R.string.settings;
    }

    @Override
    public String getName() {
        return "settings";
    }



    @Override
    public BaseQuickView createView(Context context) {
        view = new SettingsQuickView(context);
        return view;
    }

    @Override
    public BaseQuickView getBaseQuickView() {
        return view;
    }
}
