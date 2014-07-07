package org.softeg.slartus.forpdaplus.search.ui;/*
 * Created by slinkin on 24.04.2014.
 */


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.search.ISearchResultView;

import java.net.URISyntaxException;

public class SearchActivity extends BaseFragmentActivity
        implements SearchSettingsDialogFragment.ISearchDialogListener {
    private static final String SEARCH_SETTINGS_KEY = "SEARCH_SETTINGS_KEY";

    public static void startForumSearch(Context context, SearchSettings searchSettings) {
        Intent settingsActivity = new Intent(
                context, SearchActivity.class);
        settingsActivity.putExtra(SEARCH_SETTINGS_KEY, searchSettings);
        context.startActivity(settingsActivity);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_fragment_activity);

        createMenu();
        doSearch();
    }

    private void doSearch() {
        try {
            SearchSettings searchSettings = args.getParcelable(SEARCH_SETTINGS_KEY);
            assert searchSettings != null;


            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f != null && ((ISearchResultView) f).getResultView().equals(searchSettings.getResultView())) {
                ((ISearchResultView) f).search(searchSettings.getSearchQuery());
                return;
            }

            Fragment newFragment;
            if (SearchSettings.RESULT_VIEW_TOPICS.equals(searchSettings.getResultView())) {
                newFragment = SearchTopicsResultsFragment.newFragment(searchSettings.getSearchQuery());
            } else {
                newFragment = SearchPostsResultsFragment.newFragment(searchSettings.getSearchQuery());
            }


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (f != null)
                transaction.remove(f);
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.commit();
        } catch (URISyntaxException e) {
            Log.e(this, e);
        }
    }

    private MenuFragment mFragment1;

    private void createMenu() {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");

            ft.commit();
        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }


    @Override
    public void doSearchDialogPositiveClick(SearchSettings searchSettings) {
        args.putParcelable(SEARCH_SETTINGS_KEY, searchSettings);
        try {
            mFragment1.rebuildUrlMenu();
        } catch (Throwable ex) {
            Log.e(this, ex);
        }


        doSearch();
    }

    @Override
    public void doSearchDialogNegativeClick() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment != null && ((ISearchResultView) currentFragment).dispatchKeyEvent(event))
                return true;
        } catch (Throwable ex) {
            Log.e(this, ex);
        }
        return super.dispatchKeyEvent(event);
    }

    public void showSearchSettings() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = SearchSettingsDialogFragment.restartSearch((SearchSettings) args.getParcelable(SEARCH_SETTINGS_KEY));

        newFragment.show(ft, "dialog");
    }

    public String getQueryUrl() {
        try {
            SearchSettings searchSettings = args.getParcelable(SEARCH_SETTINGS_KEY);
            return searchSettings.getSearchQuery();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public static final class MenuFragment extends ProfileMenuFragment {

        private SubMenu m_SubMenu;

        public void rebuildUrlMenu() {
            if (m_SubMenu != null) {
                m_SubMenu.clear();
                ExtUrl.addUrlMenu(new Handler(), getActivity(), m_SubMenu,
                        ((SearchActivity) getActivity()).getQueryUrl(), null, null);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);


            MenuItem item;


            item = menu.add(R.string.Search)
                    .setIcon(R.drawable.ic_menu_search)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            ((SearchActivity) getActivity()).showSearchSettings();
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


            m_SubMenu = ExtUrl.addUrlSubMenu(new Handler(), getActivity(), menu,
                    ((SearchActivity) getActivity()).getQueryUrl(), null, null);

            item = menu.add(0, 0, 999, "Закрыть")
                    .setIcon(R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getActivity().finish();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }
}
