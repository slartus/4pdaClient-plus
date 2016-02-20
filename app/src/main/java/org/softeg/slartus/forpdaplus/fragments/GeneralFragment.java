package org.softeg.slartus.forpdaplus.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

/**
 * Created by radiationx on 12.11.15.
 */
public abstract class GeneralFragment extends Fragment implements IBrickFragment{
    public abstract Menu getMenu();
    public abstract boolean closeTab();

    private ActionBar actionBar;
    private MainActivity mainActivity;
    public View view;
    private boolean fragmentPaused = true;

    private String generalTitle = "ForPda";
    private String generalSubtitle = null;
    private String generalUrl = "DefaultURL";
    private String generalParentTag = "DefaultParentTag";

    @Nullable
    @Override
    public View getView() {
        return view;
    }
    public View findViewById(int id){
        return view.findViewById(id);
    }

    public String getGeneralTitle() {
        return generalTitle;
    }

    public String getGeneralUrl() {
        return generalUrl;
    }

    public String getGeneralParentTag() {
        return generalParentTag;
    }

    public void setTitle(CharSequence title){
        setTitle(title.toString());
    }
    public void setTitle(String title){
        generalTitle = title;
        if(!fragmentPaused)
            getMainActivity().setTitle(title);
    }
    public void setSubtitle(String subtitle){
        generalSubtitle = subtitle;
        if(!fragmentPaused)
            getSupportActionBar().setSubtitle(subtitle);
    }

    public MainActivity getMainActivity() {
        if(mainActivity==null)
            mainActivity = (MainActivity)getActivity();
        return mainActivity;
    }

    private TabItem thisTab;

    public void setThisTab(TabItem thisTab) {
        this.thisTab = thisTab;
    }

    public TabItem getThisTab() {
        return thisTab;
    }

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }
    private View.OnClickListener removeTabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.log("fragment tryremove tab");
            getMainActivity().tryRemoveTab(getTag());
        }
    };
    public void setArrow(){
        if(getPreferences().getBoolean("showToolbarBackArrow", true))
            getMainActivity().animateHamburger(false, removeTabListener);
    }
    public void removeArrow(){
        if(getPreferences().getBoolean("showToolbarBackArrow", true))
            getMainActivity().animateHamburger(true, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            generalTitle = savedInstanceState.getString("generalTitle");
            generalUrl = savedInstanceState.getString("generalUrl");
            generalParentTag = savedInstanceState.getString("generalParentTag");
            getThisTab().setTitle(generalTitle).setUrl(getGeneralUrl()).setParentTag(generalParentTag);
            TabDrawerMenu.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("generalTitle", getThisTab().getTitle());
        outState.putString("generalUrl", getThisTab().getUrl());
        outState.putString("generalParentTag", getThisTab().getParentTag());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainActivity = (MainActivity)getActivity();
        actionBar = getMainActivity().getSupportActionBar();
        fragmentPaused = false;
        Log.e("kek", getTag() + " FRAGMENT " + thisTab);
    }

    public ActionBar getSupportActionBar() {
        if(actionBar==null)
            actionBar = getMainActivity().getSupportActionBar();
        return actionBar;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentPaused = false;
        Log.e("kekos", "onresume "+getTag());
        actionBar = getMainActivity().getSupportActionBar();
        if(getMenu()!=null)
            onCreateOptionsMenu(getMenu(), null);
        if(getMainActivity()!=null)
            getMainActivity().setTitle(generalTitle);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setSubtitle(generalSubtitle);
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;
        Log.e("kekos", "onpause " + getTag());
        if(getSupportActionBar()!=null)
            getSupportActionBar().setSubtitle(null);
        if(getMenu()!=null)
            getMenu().clear();
        getMainActivity().onCreateOptionsMenu(MainActivity.mainMenu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.gc();
        Log.e("kek", "ondetach "+ getTag());
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

    public void hidePopupWindows(){}
}
