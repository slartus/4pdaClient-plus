package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

/**
 * Created by slartus on 23.02.14.
 */
public class EmoticsItem extends QuickPostItem {
    private EmoticsQuickView view;
    @Override
    public int getTitle() {
        return R.string.smiles;
    }

    @Override
    public String getName() {
        return "emotics";
    }

    @Override
    public BaseQuickView createView(Context context) {
        view = new EmoticsQuickView(context);
        return view;
    }

    @Override
    public BaseQuickView getBaseQuickView() {
        return view;
    }

}
