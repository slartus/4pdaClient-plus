package org.softeg.slartus.forpdaplus.controls;/*
 * Created by slinkin on 16.04.2014.
 */

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils;
import org.softeg.slartus.forpdaplus.common.Log;

import java.lang.reflect.Field;

public class SummaryValueListPreference extends ListPreference {
    public SummaryValueListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setCurrentSummary();

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        setCurrentSummary();
    }

    @Override
    public void setKey(java.lang.String key) {
        super.setKey(key);
        setCurrentSummary();
    }

    private CharSequence getTextValue(String value) {

        int ind = ArrayUtils.indexOf(value, this.getEntryValues());
        if (ind == -1)
            return "";
        return this.getEntries()[ind];
    }

    private String getDefaultValue() {
        try {
            Field defaultField = Preference.class.getDeclaredField("mDefaultValue");
            if (defaultField == null)
                return "";
            defaultField.setAccessible(true);
            Object defaultValue = defaultField.get(this);
            if (defaultValue != null)
                return defaultValue.toString();

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void setCurrentSummary() {
        try {
            String value = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getKey(), null);
            if (TextUtils.isEmpty(value)) {
                Object defValue = ExtPreferences.getPreferenceDefaultValue(this);
                if (defValue == null)
                    defValue = "";

                int ind = findIndexOfValue(value);
                if (ind != -1)
                    setValueIndex(ind);
                setSummary(getTextValue(defValue.toString()));
                return;
            }
            setSummary(getTextValue(value));
            int ind = findIndexOfValue(value);
            if (ind != -1)
                setValueIndex(ind);
        } catch (Throwable ex) {
            Log.eToast(getContext(), ex);
        }

    }
}
