package org.softeg.slartus.forpdaplus.classes.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.R;

/*
 * Created by slinkin on 15.08.13.
 */
public class ExtEditTextPreference extends EditTextPreference {
    private enum InputType {
        Number,
        NumberDecimal
    }

    private InputType m_InputType = InputType.Number;
    private String m_DefaultValue = null;

    public ExtEditTextPreference(Context context) {
        super(context);
    }

    public ExtEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtEditTextPreference);
        try {
            int i = a.getInt(R.styleable.ExtEditTextPreference_myInputType, -1);
            switch (i) {
                case 0:
                    m_InputType = InputType.Number;

                    break;
                case 1:
                    m_InputType = InputType.NumberDecimal;
                    break;
            }

            m_DefaultValue = a.getString(R.styleable.ExtEditTextPreference_defaultValue);
        } finally {
            a.recycle();
        }
    }

    public ExtEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        defaultReturnValue = m_DefaultValue;
        try {
            return super.getPersistedString(defaultReturnValue);
        } catch (Throwable ex) {

            switch (m_InputType) {
                case Number:
                    return String.valueOf(getPersistedInt(defaultReturnValue == null ? 0 : Integer.parseInt(defaultReturnValue)));
                case NumberDecimal:
                    return String.valueOf(getPersistedFloat(defaultReturnValue == null ? 0 : Float.parseFloat(defaultReturnValue)));
            }

        }

        return defaultReturnValue;
    }

    @Override
    protected float getPersistedFloat(float defaultReturnValue) {
        try {
            return super.getPersistedFloat(defaultReturnValue);
        } catch (Throwable ex) {

            return Float.parseFloat(getPersistedString(Float.toString(defaultReturnValue)));

        }
    }

    @Override
    protected boolean persistFloat(float value) {
        if (shouldPersist()) {
            if (value == getPersistedFloat(Float.NaN)) {
                // It's already there, so the same as persisting
                return true;
            }

            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putFloat(getKey(), value);
            editor.commit();
            return true;
        }
        return false;
    }


    @Override
    protected boolean persistString(String value) {
        try {
            switch (m_InputType) {
                case Number:
                    return persistInt(Integer.parseInt(value));
                case NumberDecimal:
                    float fvalue = Float.parseFloat(value);
                    return persistFloat(fvalue);
            }
            return true;
        } catch (Throwable ex) {
            Toast.makeText(getContext(), "Неверный формат числа!", Toast.LENGTH_SHORT).show();
        }
        return false;

    }
}
