package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

public class BbCodesItem extends QuickPostItem {
    private BbCodesQuickView view;
    @Override
    public int getTitle() {
        return R.string.bbcodes;
    }

    @Override
    public String getName() {
        return "bbcodes";
    }

    @Override
    public BaseQuickView createView(Context context) {
        view = new BbCodesQuickView(context);
        return view;
    }

    @Override
    public BaseQuickView getBaseQuickView() {
        return view;
    }
}

