package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.mainnotifiers.DonateNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager;
import org.softeg.slartus.forpdaplus.mainnotifiers.TopicAttentionNotifier;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.tabs.Tabs;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends BrowserViewsFragmentActivity implements BricksListDialogFragment.IBricksListDialogCaller,
        MainDrawerMenu.SelectItemListener {
    private Handler mHandler = new Handler();

    MenuFragment mFragment1;

    private MainDrawerMenu mMainDrawerMenu;

    @Override
    protected void afterCreate() {

    }


    @Override
    public void onDestroy() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
        if (currentFragment != null) {
            //fragmentManager.beginTransaction().remove(currentFragment).commit();
            currentFragment.onDestroy();

        }
        super.onDestroy();
    }

    @Override
    public String Prefix() {
        return null;
    }

    @Override
    public WebView getWebView() {
        return null;
    }

    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
            if (checkIntent())
                return;
            setContentView(R.layout.main);

            createMenu();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.decor, null); // "null" is important.
            ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            View child = decor.getChildAt(0);
            decor.removeView(child);
            FrameLayout container = (FrameLayout) drawer.findViewById(R.id.ab_cont); // This is the container we defined just now.
            container.addView(child,0);
            decor.addView(drawer);

            FrameLayout contentFrame = (FrameLayout) drawer.findViewById(R.id.content_frame);
            RelativeLayout leftDrawer = (RelativeLayout) drawer.findViewById(R.id.left_drawer);
            final float scale = getResources().getDisplayMetrics().density;
            int paddingTop = (int) (80.5 * scale + 0.5f);
            int paddingTopTab = (int) (88.5 * scale + 0.5f);
            int paddingBottom = 0;

            boolean fullScreen = (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
            if(fullScreen){
                paddingTop = (int) (56 * scale + 0.5f);
                paddingTopTab = (int) (64 * scale + 0.5f);
            }
            if(Integer.valueOf(android.os.Build.VERSION.SDK) > 19){
                if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
                    if(needBottom()) {
                        paddingBottom = (int) (48 * scale + 0.5f);
                    }
                }
                contentFrame.setPadding(0,paddingTop,0,paddingBottom);
                leftDrawer.setPadding(0,0,0,paddingBottom);
                if (isTablet()) {
                    contentFrame.setPadding(0,paddingTopTab,0,paddingBottom);
                }
            }else {
                contentFrame.setPadding(0, paddingTop, 0, 0);
                if (isTablet()) {
                    contentFrame.setPadding(0,paddingTopTab,0,0);
                }
            }

            mMainDrawerMenu = new MainDrawerMenu(this, this);




            NotifiersManager notifiersManager = new NotifiersManager(this);
            new DonateNotifier(notifiersManager).start(this);
            new TopicAttentionNotifier(notifiersManager).start(this);
            new ForPdaVersionNotifier(notifiersManager, 1).start(this);
        } catch (Throwable ex) {
            AppLog.e(getApplicationContext(), ex);
        }
    }
    public boolean isTablet(){
        int lil = getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return ((lil == 4) || (lil == Configuration.SCREENLAYOUT_SIZE_LARGE));
    }
    public boolean needBottom(){
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("paddingBottomMain", true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mMainDrawerMenu.toggleOpenState();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mMainDrawerMenu != null) {
            mMainDrawerMenu.syncState();
        }
        new ShortUserInfo(this);

    }

    private void createMenu() {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
            if (mFragment1 == null) {
                mFragment1 = new MenuFragment();
                ft.add(mFragment1, "f1");
            }
            ft.commit();
        } catch (Exception ex) {
            AppLog.e(this, ex);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    private boolean checkIntent() {
        return checkIntent(getIntent());
    }

    private boolean checkIntent(final Intent intent) {
        if (IntentActivity.checkSendAction(this, intent))
            return false;
        //intent.setData(Uri.parse("http://4pda.ru/forum/lofiversion/index.php?t365142-1650.html"));
        if (intent.getData() != null) {
            final String url = intent.getData().toString();
            if (IntentActivity.tryShowUrl(this, mHandler, url, false, true)) {
                return true;
            }
            startNextMatchingActivity(intent);

            Toast.makeText(this, "Не умею обрабатывать ссылки такого типа\n" + url, Toast.LENGTH_LONG).show();
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onBricksListDialogResult(DialogInterface dialog, String dialogId,
                                         BrickInfo brickInfo, Bundle args) {
        dialog.dismiss();
        ListFragmentActivity.showListFragment(this, brickInfo.getName(), args);
    }

    /**
     * Swaps fragments in the main content view
     */
    public void selectItem(final BrickInfo listTemplate) {
        //final BrickInfo listTemplate = ((MenuBrickAdapter) mAdapter).getItem(position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);

        if (currentFragment != null) {
            if (((IBrickFragment) currentFragment)
                    .getListName().equals(listTemplate.getName())) {
                if (mMainDrawerMenu != null)
                    mMainDrawerMenu.close();
                return;
            }

            currentFragment.onDestroy();
        }
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);// новости выставляют выпадающий список
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setSubtitle(null);

        Fragment fragment = listTemplate.createFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        setTitle(listTemplate.getTitle());
        if (mMainDrawerMenu != null)
            mMainDrawerMenu.close();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Preferences.Lists.setLastSelectedList(listTemplate.getName());
//                Preferences.Lists.addLastAction(listTemplate.getName());
//            }
//        }).start();

    }

    private Boolean m_ExitWarned = false;

    private void appExit() {

        App.getInstance().exit();
    }

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

    @Override
    public void onBackPressed() {
        try {
            if (mMainDrawerMenu.isOpen()) {
                mMainDrawerMenu.close();
                return;
            }
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment == null || !((IBrickFragment) currentFragment).onBackPressed()) {
                if (!m_ExitWarned) {
                    Toast.makeText(this, "Нажмите кнопку НАЗАД снова, чтобы выйти из программы", Toast.LENGTH_SHORT).show();
                    m_ExitWarned = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            m_ExitWarned = false;
                        }
                    }, 3 * 1000);
                } else {
                    appExit();
                }

            } else {
                m_ExitWarned = false;
            }
        } catch (Throwable ignored) {
            appExit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        m_ExitWarned = false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        try {
            if (v.getTag() != null) {
                Object o = v.getTag();
                if (TagPair.class.isInstance(o)) {
                    TagPair tagPair = TagPair.class.cast(o);
                    if (tagPair.first.equals("Tab")) {

                        final String tabId = tagPair.second;
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

                        String defaulttabId = prefs.getString("tabs.defaulttab", "Tab1");
                        try {
                            menu.setHeaderTitle(Tabs.getTabName(prefs, tabId));
                        } catch (NotReportException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        menu.add("По умолчанию").setCheckable(true).setChecked(tabId.equals(defaulttabId))
                                .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("tabs.defaulttab", tabId);
                                        editor.commit();
                                        menuItem.setChecked(true);
                                        return true;
                                    }
                                });
                        android.view.Menu defaultActionMenu = menu.addSubMenu("Действие по умолчанию..");
                        String[] actionsArray = getResources().getStringArray(R.array.ThemeActionsArray);
                        final String[] actionsValues = getResources().getStringArray(R.array.ThemeActionsValues);
                        final String actionPrefName = "tabs." + tabId + ".Action";
                        String defaultAction = prefs.getString(actionPrefName, "getfirstpost");
                        for (int i = 0; i < actionsValues.length; i++) {
                            final int finalI = i;
                            defaultActionMenu.add(actionsArray[i])
                                    .setCheckable(true).setChecked(defaultAction.equals(actionsValues[i]))
                                    .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(actionPrefName, actionsValues[finalI]);
                                            editor.commit();
                                            menuItem.setChecked(true);
                                            return true;
                                        }
                                    });
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }
    }

    public Handler getHandler() {
        return mHandler;
    }


    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public static final class MenuFragment extends ProfileMenuFragment {

        public MenuFragment() {
            super();
        }


        @Override
        public void onCreate(Bundle saveInstance) {
            super.onCreate(saveInstance);
            setHasOptionsMenu(true);
        }

        private Menu m_miOther;


        public void setOtherMenu() {
            MenuItem miQuickStart = m_miOther.add("Быстрый доступ");
            miQuickStart.setIcon(R.drawable.ic_menu_quickrun);
            miQuickStart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    BricksListDialogFragment.showDialog((BricksListDialogFragment.IBricksListDialogCaller) getActivity(),
                            BricksListDialogFragment.QUICK_LIST_ID,
                            ListCore.getBricksNames(ListCore.getQuickBricks()), null);

                    return true;
                }
            });
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);


            MenuItem item;

            m_miOther = menu;
            item = menu.add(R.string.Search)
                    .setIcon(R.drawable.ic_menu_search)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            SearchSettingsDialogFragment.showSearchSettingsDialog(getActivity(),
                                    SearchSettingsDialogFragment.createForumSearchSettings());
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            setOtherMenu();


            item = menu.add(0, 0, 999, R.string.CloseApp)
                    .setIcon(R.drawable.ic_menu_close_clear_cancel)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {


                            getActivity().finish();
                            System.exit(0);
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        }
    }

    public class TagPair extends Pair<String, String> {

        public TagPair(String first, String second) {
            super(first, second);
        }
    }

}