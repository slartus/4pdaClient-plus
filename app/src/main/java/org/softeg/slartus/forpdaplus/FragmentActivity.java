/*
package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;

*/
/**
 * Created by radiationx on 24.10.15.
 *//*

public class FragmentActivity extends AppCompatActivity
        implements SearchSettingsDialogFragment.ISearchDialogListener {
    public static final String SENDER_ACTIVITY = "sender_activity";
    public LinearLayout statusBar;


    */
/*public ActionBar getSupportActionBar() {
        return getSupportActionBar();
    }
    public void setSupportProgressBarIndeterminateVisibility(boolean b) {
        setSupportProgressBarIndeterminateVisibility(b);
    }
    public void setSupportProgressBarIndeterminate(boolean b) {
        setSupportProgressBarIndeterminate(b);
    }*//*


    @Override
    public void startActivity(android.content.Intent intent) {
        intent.putExtra(SENDER_ACTIVITY, getClass().toString());
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(android.content.Intent intent, int requestCode) {
        intent.putExtra(SENDER_ACTIVITY, getClass().toString());
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    protected Bundle args = new Bundle();

    @Override
    public void doSearchDialogPositiveClick(SearchSettings searchSettings) {
        MainActivity.startForumSearch(searchSettings);
    }

    @Override
    public void doSearchDialogNegativeClick() {

    }

    protected boolean isTransluent() {
        return true;
    }

    @Override
    protected void onCreate(Bundle saveInstance) {
        setTheme(isTransluent() ? App.getInstance().getTransluentThemeStyleResID() : App.getInstance().getThemeStyleResID());
        super.onCreate(saveInstance);

*/
/*
        if(PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("statusbarTransparent",false)) {
            if (android.os.Build.VERSION.SDK_INT >= 21)
                getWindow().setStatusBarColor(Color.TRANSPARENT);
        }else {
            if (android.os.Build.VERSION.SDK_INT > 18) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout statusBarLay = (LinearLayout) inflater.inflate(R.layout.statusbar, null);
                statusBar = (LinearLayout) statusBarLay.findViewById(R.id.statusBar);
                statusBar.setMinimumHeight(getStatusBarHeight());
                if (App.getInstance().getCurrentThemeName().equals("white")) {
                    statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_wh));
                } else if (App.getInstance().getCurrentThemeName().equals("black")) {
                    statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_bl));
                }
                ViewGroup decor = (ViewGroup) getWindow().getDecorView();
                decor.addView(statusBarLay);
                statusBarShowed = true;
            }
        }
*//*

        args.clear();
        if (getIntent().getExtras() != null) {
            args.putAll(getIntent().getExtras());
        }
        if (saveInstance != null) {
            args.putAll(saveInstance);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences(prefs);
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);
        super.onSaveInstanceState(outState);
        if(hack){
            onStop();
            onStart();
        }
        hack= false;
        MainActivity.log("onsaveinstancestate");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle outState) {
        args = outState;
        super.onRestoreInstanceState(outState);
        MainActivity.log("onrestoreinstancestate");
    }


    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @SuppressWarnings("ResourceType")
    protected void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }


}
*/
