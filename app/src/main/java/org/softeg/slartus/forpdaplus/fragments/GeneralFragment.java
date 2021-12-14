package org.softeg.slartus.forpdaplus.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.tabs.TabItem;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 12.11.15.
 */
public abstract class GeneralFragment extends Fragment implements IBrickFragment {
    public abstract boolean closeTab();

    private static final String TAG = GeneralFragment.class.getSimpleName();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ActionBar actionBar;
    private MainActivity mainActivity;
    protected View view;
    private boolean fragmentPaused = true;
    protected boolean activityCreated = false;

    private String generalTitle = "ForPda";
    private String generalSubtitle = null;
    private String generalUrl = "DefaultURL";
    private String generalParentTag = "DefaultParentTag";
    private Menu menu;

    public GeneralFragment() {
        super();
    }

    public GeneralFragment(@LayoutRes int contentLayoutId) {
        super(contentLayoutId);
    }

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
        return view == null ? super.getView() : view;
    }

    public View findViewById(int id) {
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

    public void setTitle(CharSequence title) {
        setTitle(title.toString());
    }

    public void setTitle(String title) {
        generalTitle = title;
        if (generalTitle != null)
            if (generalTitle.equals(getMainActivity().getTitle()))
                return;
        if (!fragmentPaused)
            getMainActivity().setTitle(title);
    }

    public void setSubtitle(String subtitle) {
        generalSubtitle = subtitle;
        /*Пусть нахрен заменяет! Ибо я хз что это паттерн такой, когда subTitle надо сохранять*/
//        if(generalSubtitle!=null)
//            if(generalSubtitle.equals(getSupportActionBar().getSubtitle()))
//                return;
        if (!fragmentPaused)
            getSupportActionBar().setSubtitle(subtitle);
    }

    public MainActivity getMainActivity() {
        if (mainActivity == null)
            mainActivity = (MainActivity) getActivity();
        return mainActivity;
    }

    private TabItem thisTab;

    public void setThisTab(TabItem thisTab) {
        this.thisTab = thisTab;
        generalTitle = thisTab.getTitle();
        generalSubtitle = thisTab.getSubTitle();
        generalParentTag = thisTab.getParentTag();
    }

    public TabItem getThisTab() throws Exception {
        if (thisTab == null)
            thisTab = TabsManager.getInstance().getTabByTag(getTag());
        if (thisTab == null)
            throw new Exception("TabItem by " + getTag() + " not found");
        return thisTab;
    }

    public static SharedPreferences getPreferences() {
        return App.getInstance().getPreferences();
    }

    private final View.OnClickListener removeTabListener = v -> getMainActivity().tryRemoveTab(getTag(), true);

    public void setArrow() {
        getMainActivity().animateHamburger(false, removeTabListener);
    }

    public void removeArrow() {
        getMainActivity().animateHamburger(true, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            generalTitle = savedInstanceState.getString("generalTitle", generalTitle);
            generalUrl = savedInstanceState.getString("generalUrl", generalUrl);
            generalParentTag = savedInstanceState.getString("generalParentTag", generalParentTag);

            try {
                getThisTab().setTitle(generalTitle).setUrl(getGeneralUrl()).setParentTag(generalParentTag);
            } catch (Exception e) {
                AppLog.e(getContext(), e);
            }
            getMainActivity().notifyTabAdapter();
            activityCreated = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
        try {
            TabItem tab = getThisTab();
            Log.i(TAG, tab.getTitle());
            Log.i(TAG, tab.getUrl());
            Log.i(TAG, tab.getParentTag());
            outState.putString("generalTitle", tab.getTitle());
            outState.putString("generalUrl", tab.getUrl());
            outState.putString("generalParentTag", tab.getParentTag());

        } catch (Exception e) {
            AppLog.e(getContext(), e);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        actionBar = getMainActivity().getSupportActionBar();
        fragmentPaused = false;
    }

    public ActionBar getSupportActionBar() {
        if (actionBar == null)
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
        if (actionBar == null)
            actionBar = getMainActivity().getSupportActionBar();
        Menu menu = getMenu();
        if (menu != null)
            onCreateOptionsMenu(menu, new MenuInflater(getContext()));
        if (getMainActivity() != null)
            getMainActivity().setTitle(generalTitle);
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(generalSubtitle);

        if (activityCreated) {
            getMainActivity().notifyTabAdapter();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(null);
        getMainActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.gc();
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
    public void loadData(boolean isRefresh) {
    }

    @Override
    public void startLoad() {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    public void hidePopupWindows() {
    }

    public void clearNotification(int notifId) {
        Log.i("Clear Notification", "Notification Id: " + notifId);

        NotificationManager notificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notifId);
        }
    }

    protected void addToDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }


    public void onSupportActionModeStarted(androidx.appcompat.view.ActionMode mode) {

    }

    public void onActionModeStarted(android.view.ActionMode mode) {

    }
}
