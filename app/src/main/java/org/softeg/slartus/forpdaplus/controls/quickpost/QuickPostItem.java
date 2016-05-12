package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.content.Context;
import android.widget.EditText;

import org.softeg.slartus.forpdaplus.controls.quickpost.items.BaseQuickView;


/**
 * Created by slartus on 23.02.14.
 */
public abstract class QuickPostItem {
    public abstract int getTitle();

    public abstract String getName();

    public BaseQuickView createView(Context context,EditText editText){
        BaseQuickView view= createView(context);
        view.setEditor(editText);
        return view;
    }

    public abstract BaseQuickView createView(Context context);
    public abstract BaseQuickView getBaseQuickView();
}
