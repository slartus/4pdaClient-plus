package org.softeg.slartus.forpdaplus.listfragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;

/*
 * Created by slinkin on 03.03.14.
 */
public class ListFragmentActivity extends BaseFragmentActivity {
    public static final String BRICK_NAME_KEY = "BRICK_NAME_KEY";
    public ListFragmentActivity() {

        super();
    }
    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        createActionMenu();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        setContentView(R.layout.list_fragment_activity);

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        assert extras != null;
        String brickName = extras.getString(BRICK_NAME_KEY);

        createFragment(brickName, extras);

    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment != null && ((IBrickFragment) currentFragment).dispatchKeyEvent(event))
                return true;
        } catch (Throwable ex) {
            AppLog.e(this, ex);
        }
        return super.dispatchKeyEvent(event);
    }

    private Boolean m_ExitWarned = false;
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment == null || !((IBrickFragment) currentFragment).onBackPressed()) {
            /*if (!m_ExitWarned) {
                Toast.makeText(this, "Нажмите кнопку НАЗАД снова, чтобы закрыть", Toast.LENGTH_SHORT).show();

                m_ExitWarned = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        m_ExitWarned = false;
                    }
                }, 3 * 1000);
            } else {
                super.onBackPressed();
            }*/
            super.onBackPressed();

        }else {
            m_ExitWarned = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        m_ExitWarned = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
           super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public static void showListFragment(Context activity, String brickName, Bundle extras) {
        Intent intent = new Intent(activity.getApplicationContext(), ListFragmentActivity.class);
        if (extras != null)
            intent.putExtras(extras);
        intent.putExtra(BRICK_NAME_KEY, brickName);

        activity.startActivity(intent);
    }

    private void createFragment(String brickName, Bundle args) {
        final BrickInfo listTemplate = ListCore.getRegisteredBrick(brickName);

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = listTemplate.createFragment();
        fragment.setArguments(args);
        setTitle(((IBrickFragment)fragment).getListTitle());
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment,"f2")
                .commit();

    }

    protected void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MenuFragment mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();

    }

    public static final class MenuFragment extends ProfileMenuFragment {
        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu,inflater);
            MenuItem item = menu.add(R.string.Search)
                    .setIcon(R.drawable.ic_magnify_white_24dp)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            SearchSettingsDialogFragment.showSearchSettingsDialog(getActivity(),
                                    SearchSettingsDialogFragment.createForumSearchSettings());
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
             item = menu.add(0, 0, 999,"Закрыть")
                    .setIcon(R.drawable.ic_close_white_24dp);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getActivity().finish();

                    return true;
                }
            });
        }
    }
}
