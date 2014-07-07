package org.softeg.slartus.forpdaplus;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.search.ui.SearchActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;

/**
 * User: slinkin
 * Date: 14.03.12
 * Time: 12:51
 */
public class BaseFragmentActivity extends FragmentActivity
        implements SearchSettingsDialogFragment.ISearchDialogListener {
    public static final String SENDER_ACTIVITY = "sender_activity";
    public static final String FORCE_EXIT_APPLICATION = "org.softeg.slartus.forpdaplus.FORCE_EXIT_APPLICATION";

    protected void afterCreate() {

    }

    public Context getContext() {
        return this;
    }

    public ActionBar getSupportActionBar() {
        return getActionBar();
    }

    protected void setSupportProgressBarIndeterminateVisibility(boolean b) {
        setProgressBarIndeterminateVisibility(b);
    }

    protected void setSupportProgressBarIndeterminate(boolean b) {
        setProgressBarIndeterminate(b);
    }

    @Override
    public void startActivity(android.content.Intent intent) {
        intent.putExtra(BaseFragmentActivity.SENDER_ACTIVITY, getClass().toString());
        super.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfForceKill();
    }

    private void checkIfForceKill() {
        //CHECK IF I NEED TO KILL THE APP
        // Restore preferences
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean forceKill = settings.getBoolean(
                FORCE_EXIT_APPLICATION, false);

        if (forceKill) {
            //CLEAR THE FORCE_EXIT SETTINGS
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FORCE_EXIT_APPLICATION, false);
            // Commit the edits!
            editor.commit();
            //HERE STOP ALL YOUR SERVICES
            finish();
        }
    }

    protected Bundle args = new Bundle();

    @Override
    public void doSearchDialogPositiveClick(SearchSettings searchSettings) {
        SearchActivity.startForumSearch(this, searchSettings);
    }

    @Override
    public void doSearchDialogNegativeClick() {

    }

    protected boolean isTransluent() {
        return false;
    }

    @Override
    protected void onCreate(Bundle saveInstance) {
        setTheme(isTransluent() ? MyApp.getInstance().getTransluentThemeStyleResID() : MyApp.getInstance().getThemeStyleResID());
        super.onCreate(saveInstance);

        args.clear();
        if (getIntent().getExtras() != null) {
            args.putAll(getIntent().getExtras());
        }
        if (saveInstance != null) {
            args.putAll(saveInstance);
        }

        afterCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences(prefs);
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle outState) {
        args = outState;
        super.onRestoreInstanceState(outState);
    }

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    protected void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }


}
