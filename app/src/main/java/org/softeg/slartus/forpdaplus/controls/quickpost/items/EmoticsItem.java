package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;

import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

/**
 * Created by slartus on 23.02.14.
 */
public class EmoticsItem extends QuickPostItem {
    @Override
    public String getTitle() {
        return "Смайлы";
    }

    @Override
    public String getName() {
        return "emotics";
    }

    @Override
    public BaseQuickView createView(Context context) {
        return new EmoticsQuickView(context);
    }
}
