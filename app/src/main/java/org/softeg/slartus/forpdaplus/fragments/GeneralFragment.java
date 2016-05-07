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
import android.view.MenuInflater;
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
    public abstract boolean closeTab();

    private ActionBar actionBar;
    private MainActivity mainActivity;
    protected View view;
    private boolean fragmentPaused = true;

    private String generalTitle = "ForPda";
    private String generalSubtitle = null;
    private String generalUrl = "DefaultURL";
    private String generalParentTag = "DefaultParentTag";
    private Menu menu;

    public Menu getMenu() {
        return menu;
    }

    public boolean isFragmentPaused() {
        return fragmentPaused;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
    }

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
        if(generalTitle!=null)
            if(generalTitle.equals(getMainActivity().getTitle()))
                return;
        if(!fragmentPaused)
            getMainActivity().setTitle(title);
    }
    public void setSubtitle(String subtitle){
        generalSubtitle = subtitle;
        /*Пусть нахрен заменяет! Ибо я хз что это паттерн такой, когда subTitle надо сохранять*/
//        if(generalSubtitle!=null)
//            if(generalSubtitle.equals(getSupportActionBar().getSubtitle()))
//                return;
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
        generalTitle = thisTab.getTitle();
        generalSubtitle = thisTab.getSubTitle();
        generalParentTag = thisTab.getParentTag();
    }

    public TabItem getThisTab() {
        if(thisTab==null)
            thisTab = App.getInstance().getTabByTag(getTag());
        return thisTab;
    }

    public static SharedPreferences getPreferences() {
        return App.getInstance().getPreferences();
    }
    private View.OnClickListener removeTabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.log("fragment tryremove tab");
            Log.e("kek", getTag()+" : "+getThisTab().getTag());
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
        setHasOptionsMenu(true);
        if(savedInstanceState!=null){
            generalTitle = savedInstanceState.getString("generalTitle");
            generalUrl = savedInstanceState.getString("generalUrl");
            generalParentTag = savedInstanceState.getString("generalParentTag");
            Log.d("kek", getGeneralTitle()+" : "+getGeneralUrl()+" : "+getGeneralParentTag());
            getThisTab().setTitle(generalTitle).setUrl(getGeneralUrl()).setParentTag(generalParentTag);
            getMainActivity().notifyTabAdapter();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("generalTitle", getThisTab().getTitle());
        outState.putString("generalUrl", getThisTab().getUrl());
        outState.putString("generalParentTag", getThisTab().getParentTag());
        Log.d("kek", getThisTab().getTitle()+" : "+getThisTab().getUrl()+" : "+getThisTab().getParentTag());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if(actionBar==null)
            actionBar = getMainActivity().getSupportActionBar();
        Log.d("kek", getMenu()+" : ");
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
        /*if(getMenu()!=null)
            getMenu().clear();*/
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
