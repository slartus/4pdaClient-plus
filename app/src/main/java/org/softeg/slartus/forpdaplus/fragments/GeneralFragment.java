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
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;

/**
 * Created by radiationx on 12.11.15.
 */
public abstract class GeneralFragment extends Fragment implements IBrickFragment{
    public abstract Menu getMenu();
    public abstract boolean closeTab();

    private ActionBar actionBar;
    private MainActivity mainActivity;
    protected View view;

    private String generalTitle = "ForPda";
    private String generalUrl = "defurl";
    private String generalParentTag = "defparenttag";

    public String getGeneralTitle() {
        return generalTitle;
    }

    public String getGeneralUrl() {
        return generalUrl;
    }

    public String getGeneralParentTag() {
        return generalParentTag;
    }
    boolean fragmentPaused = true;

    public void setTitle(CharSequence title){
        setTitle(title.toString());
    }
    public void setTitle(String title){
        if(!fragmentPaused)
            getMainActivity().setTitle(title);
    }
    public void setSubtitle(String subtitle){
        if(!fragmentPaused)
            getSupportActionBar().setSubtitle(subtitle);
    }

    public MainActivity getMainActivity() {
        if(mainActivity==null)
            mainActivity = (MainActivity)getActivity();
        return mainActivity;
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
        if(getPreferences().getBoolean("showBackArrow", true))
            getMainActivity().animateHamburger(false, removeTabListener);
    }
    public void removeArrow(){
        if(getPreferences().getBoolean("showBackArrow", true))
            getMainActivity().animateHamburger(true, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            generalTitle = savedInstanceState.getString("generalTitle");
            generalUrl = savedInstanceState.getString("generalUrl");
            generalParentTag = savedInstanceState.getString("generalParentTag");
            for(int i = 0; i <= App.getInstance().getTabItems().size()-1; i++){
                if(App.getInstance().getTabItems().get(i).getTag().equals(getTag())) {
                    App.getInstance().getTabItems().get(i).setTitle(generalTitle);
                    App.getInstance().getTabItems().get(i).setUrl(generalUrl);
                    App.getInstance().getTabItems().get(i).setParentTag(generalParentTag);
                    TabDrawerMenu.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("generalTitle", App.getInstance().getTabByTag(getTag()).getTitle());
        outState.putString("generalUrl", App.getInstance().getTabByTag(getTag()).getUrl());
        outState.putString("generalParentTag", App.getInstance().getTabByTag(getTag()).getParentTag());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainActivity = (MainActivity)getActivity();
        actionBar = mainActivity.getSupportActionBar();
        fragmentPaused = false;
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
