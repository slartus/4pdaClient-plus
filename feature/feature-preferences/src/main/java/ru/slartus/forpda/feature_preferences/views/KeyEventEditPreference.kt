package ru.slartus.forpda.feature_preferences.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.preference.EditTextPreference;

/**
 * Created by slinkin on 14.01.14.
 */
public class KeyEventEditPreference extends EditTextPreference {
    public KeyEventEditPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public KeyEventEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyEventEditPreference(Context context) {
        super(context);
        init();
    }

    public void init(){
        setOnBindEditTextListener(editText ->
        {
            //setText(Integer.toString(editText.getText().toString()));
        });
    }


//
//
//    @Override
//    protected void showDialog(android.os.Bundle state) {
//        super.showDialog(state);
//
//        getEditText().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                getEditText().setText(Integer.toString(keyEvent.getKeyCode()));
//                return false;
//            }
//        });
//    }

}
