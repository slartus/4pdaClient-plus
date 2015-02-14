package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;
import android.view.View;

import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

public class BbCodesItem extends QuickPostItem {
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
        return new BbCodesQuickView(context);
    }
}

