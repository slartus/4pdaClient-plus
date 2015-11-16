package org.softeg.slartus.forpdaplus.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;

import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;

/**
 * Created by radiationx on 12.11.15.
 */
public abstract class GeneralFragment extends Fragment implements IBrickFragment{
    public abstract Menu getMenu();

    private ActionBar actionBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = ((MainActivity)getActivity()).getSupportActionBar();
    }

    public ActionBar getSupportActionBar() {
        return actionBar;
    }
    @Override
    public void onResume() {
        super.onResume();
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
