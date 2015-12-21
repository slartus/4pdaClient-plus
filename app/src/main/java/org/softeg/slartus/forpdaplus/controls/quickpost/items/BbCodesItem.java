package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;

import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

public class BbCodesItem extends QuickPostItem {
    private BbCodesQuickView view;
    @Override
    public String getTitle() {
        return "BB-коды";
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

