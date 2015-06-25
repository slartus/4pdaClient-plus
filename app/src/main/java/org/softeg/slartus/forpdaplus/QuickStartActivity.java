package org.softeg.slartus.forpdaplus;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.tabs.BaseTab;
import org.softeg.slartus.forpdaplus.tabs.ITabParent;
import org.softeg.slartus.forpdaplus.tabs.Tabs;
import org.softeg.slartus.forpdaplus.tabs.ThemesTab;

/**
 * User: slinkin
 * Date: 14.11.11
 * Time: 11:48
 */
public class QuickStartActivity extends BaseFragmentActivity implements ITabParent {
    private BaseTab themesTab;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createActionMenu();

        setContentView(R.layout.empty_activity);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        assert extras != null;
        String template = extras.getString("template");
        themesTab = Tabs.create(this, template, "QuickTab");
        setContentView(themesTab);
        registerForContextMenu(themesTab.getListView());
        setTitle(themesTab.getTitle());
        themesTab.refresh(extras);
        themesTab.setOnTabTitleChangedListener(new ThemesTab.OnTabTitleChangedListener() {
            public void onTabTitleChanged(String title) {
                setTitle(title);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Toast.makeText(this,Integer.toString(item.getItemId()),Toast.LENGTH_LONG).show();
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return true;
    }
    public static void showTab(Activity activity, String tabTemplate) {
        showTab(activity, tabTemplate, null);
    }

    public static void showTab(Activity activity, String tabTemplate, Bundle extras) {
        Intent intent = new Intent(activity.getApplicationContext(), QuickStartActivity.class);
        if (extras != null)
            intent.putExtras(extras);
        intent.putExtra("template", tabTemplate);

        activity.startActivity(intent);
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

    public BaseTab getTab() {
        return themesTab;
    }

    private Boolean m_ExitWarned = false;

    @Override
    public void onBackPressed() {

        if (!themesTab.onParentBackPressed()) {
            if (!m_ExitWarned) {
                Toast.makeText(getApplicationContext(), "Нажмите кнопку НАЗАД снова, чтобы закрыть", Toast.LENGTH_SHORT).show();
                m_ExitWarned = true;
            } else {
                finish();
            }

        } else {
            m_ExitWarned = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        themesTab.onActivityResult(requestCode, resultCode, data);
    }

    public void refresh() {

    }

    @Override
    public void onResume() {
        super.onResume();
        m_ExitWarned = false;
    }


    public static final class MenuFragment extends Fragment {
        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        private QuickStartActivity getInterface() {
            return (QuickStartActivity) getActivity();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            getInterface().getTab().onCreateOptionsMenu(menu, inflater);

            MenuItem item;
            if (getInterface().getTab().refreshable()) {
                item = menu.add("Обновить").setIcon(R.drawable.ic_refresh_white_24dp);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        getInterface().themesTab.refresh();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        themesTab.onCreateContextMenu(menu, v, menuInfo, mHandler);

    }


}
