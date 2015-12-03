package org.softeg.slartus.forpdaplus.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;

/**
 * Created by radiationx on 12.11.15.
 */
public abstract class GeneralFragment extends Fragment implements IBrickFragment{
    public abstract Menu getMenu();
    public abstract boolean closeTab();

    private ActionBar actionBar;

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }
    public void setArrow(){
        if(getPreferences().getBoolean("showBackArrow", false)) {
            ((MainActivity) getActivity()).setArrow(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!closeTab())
                        ((MainActivity) getActivity()).removeTab(getTag());
                }
            });
        }
    }
    public void removeArrow(){
        if(getPreferences().getBoolean("showBackArrow", false)){
            ((MainActivity) getActivity()).setArrow(false, null);
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        actionBar = ((MainActivity)getActivity()).getSupportActionBar();
    }

    public ActionBar getSupportActionBar() {
        return actionBar;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if(getMenu()!=null)
            onCreateOptionsMenu(getMenu(), null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getSupportActionBar()!=null)
            getSupportActionBar().setSubtitle(null);
        if(getMenu()!=null)
            getMenu().clear();
        getActivity().onCreateOptionsMenu(MainActivity.mainMenu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String getListName() {
        return null;
    }

    @Override
    public String getListTitle() {
        return null;
    }

    @Override
    public void loadData(boolean isRefresh) {}

    @Override
    public void startLoad() {}

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }
}
