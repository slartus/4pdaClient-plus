package org.softeg.slartus.forpdaplus.controls;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by slinkin on 14.01.14.
 */
public class KeyEventEditPreference extends EditTextPreference {
    public KeyEventEditPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public KeyEventEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public KeyEventEditPreference(Context context) {
        super(context);

    }

    @Override
    protected void showDialog(android.os.Bundle state) {
        super.showDialog(state);

        getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                getEditText().setText(Integer.toString(keyEvent.getKeyCode()));
                return false;
            }
        });
    }

}
