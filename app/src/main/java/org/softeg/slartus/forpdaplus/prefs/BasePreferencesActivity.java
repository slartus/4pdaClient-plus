package org.softeg.slartus.forpdaplus.prefs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by slinkin on 27.12.13.
 */
public class BasePreferencesActivity extends PreferenceActivity {
    protected Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(App.getInstance().getPrefsThemeStyleResID());
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("coloredNavBar", true) &&
                android.os.Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(App.getInstance().getResources().getColor(getNavBarColor()));
    }
    public int getNavBarColor(){
        if(App.getInstance().isWhiteTheme())
            return R.color.actionbar_background_wh;
        else
            return R.color.actionbar_background_bl;
    }
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
        if (result != null) {
            return result;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            switch (name) {
                case "EditText":
                    return new AppCompatEditText(this,attrs);
                case "Spinner":
                    return new AppCompatSpinner(this,attrs);
                case "CheckBox":
                    return new AppCompatCheckBox(this,attrs);
                case "RadioButton":
                    return new AppCompatRadioButton(this,attrs);
                case "CheckedTextView":
                    return new AppCompatCheckedTextView(this,attrs);
            }
        }

        return null;
    }
    private static List<String> fragments = new ArrayList<String>();

    @Override
    public void loadHeadersFromResource(int resid, List<Header> target) {

        super.loadHeadersFromResource(resid, target);
        fragments.clear();
        for (Header header : target) {
            fragments.add(header.fragment);
        }
    }


    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragments.contains(fragmentName);
    }
}
