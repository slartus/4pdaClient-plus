package org.softeg.slartus.forpdaplus.controls.quickpost.items;

import android.content.Context;

import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostItem;

/**
 * Created by slartus on 23.02.14.
 */
public class EmoticsItem extends QuickPostItem {
    private EmoticsQuickView emoticsQuickView;
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
        emoticsQuickView = new EmoticsQuickView(context);
        return emoticsQuickView;
    }
    public EmoticsQuickView getEmoticsQuickView(){
        return emoticsQuickView;
    }
}
