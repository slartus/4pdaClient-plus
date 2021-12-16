package org.softeg.slartus.forpdaplus.controls;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.softeg.slartus.forpdacommon.StringUtilsKt;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import static org.softeg.slartus.forpdaplus.utils.Utils.getS;

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
    private CharSequence m_DefaultSummary = null;

    public ExtEditTextPreference(Context context) {
        super(context);
    }

    public ExtEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExtEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ExtEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
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

            m_DefaultValue = a.getString(R.styleable.ExtEditTextPreference_appDefaultValue);
        } finally {
            a.recycle();
        }

        m_DefaultSummary = getSummary();
        setCurrentSummary();
    }

    @Override
    public String getText() {
        return getPersistedString(m_DefaultValue);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        defaultReturnValue = m_DefaultValue;
        String result = defaultReturnValue;
        try {
            result = super.getPersistedString(defaultReturnValue);
        } catch (Throwable ex) {
            switch (m_InputType) {
                case Number:
                    result = String.valueOf(getPersistedInt(defaultReturnValue == null ? 0 : Integer.parseInt(defaultReturnValue)));
                    break;
                case NumberDecimal:
                    result = String.valueOf(getPersistedFloat(defaultReturnValue == null ? 0 : Float.parseFloat(defaultReturnValue)));
                    break;
            }
        }

        return StringUtilsKt.simplifyNumber(result);
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
            editor.apply();
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
            Toast.makeText(getContext(), R.string.invalid_number_format, Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        setCurrentSummary();
    }

    private void setCurrentSummary() {
        String value = "";
        switch (m_InputType) {
            case Number:
                value = Integer.toString(App.getInstance().getPreferences().getInt(getKey(), Integer.parseInt(m_DefaultValue)));
                break;
            case NumberDecimal:
                value = Float.toString(App.getInstance().getPreferences().getFloat(getKey(), Float.parseFloat(m_DefaultValue)));
                break;
        }

        setSummary(String.format(m_DefaultSummary.toString(), StringUtilsKt.simplifyNumber(value)));
    }
}
