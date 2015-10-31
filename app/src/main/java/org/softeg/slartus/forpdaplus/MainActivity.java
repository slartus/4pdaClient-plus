package org.softeg.slartus.forpdaplus;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.mainnotifiers.DonateNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager;
import org.softeg.slartus.forpdaplus.mainnotifiers.TopicAttentionNotifier;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.tabs.TabItem;
import org.softeg.slartus.forpdaplus.tabs.Tabs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends FragmentActivity implements BricksListDialogFragment.IBricksListDialogCaller,
        MainDrawerMenu.SelectItemListener, TabDrawerMenu.SelectItemListener {

    private final static String tabPrefix = "tab";
    private Handler mHandler = new Handler();

    MenuFragment mFragment1;

    private MainDrawerMenu mMainDrawerMenu;
    private static TabDrawerMenu mTabDraweMenu;
    private RelativeLayout leftDrawer,topInform;
    public Toolbar toolbar;
    boolean top;
    int lastTheme;
    private static TabItem tabOnIntent = null;
    private static boolean activityPaused = true;

    @Override
    protected void afterCreate() {

    }
    public MainDrawerMenu getmMainDrawerMenu(){
        return mMainDrawerMenu;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        lastTheme = App.getInstance().getThemeStyleResID();
        try {
            if (checkIntent()&saveInstance!=null) return;
            setContentView(R.layout.main);
            App.getInstance().getTabIterator();
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            if(Build.VERSION.SDK_INT>20) {
                findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
                toolbar.setElevation(4);
            }
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
            }
            if(PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("statusbarTransparent", false)) {
                if (android.os.Build.VERSION.SDK_INT >= 21)
                    getWindow().setStatusBarColor(Color.TRANSPARENT);
            }else {
                if (android.os.Build.VERSION.SDK_INT > 18) {
                    LinearLayout statusBar = (LinearLayout) findViewById(R.id.statusBar);
                    statusBar.setMinimumHeight(getStatusBarHeight());

                    if (App.getInstance().getCurrentThemeName().equals("white")) {
                        statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_wh));
                    } else if (App.getInstance().getCurrentThemeName().equals("black")) {
                        statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_bl));
                    }
                    statusBarShowed = true;
                }
            }


            createMenu();
/*
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.decor, null); // "null" is important.
            ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            View child = decor.getChildAt(0);
            decor.removeView(child);
            FrameLayout container = (FrameLayout) drawer.findViewById(R.id.ab_cont); // This is the container we defined just now.

            container.addView(child, 0);
            decor.addView(drawer);
*/
            leftDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
            topInform = (RelativeLayout) findViewById(R.id.topInform);
            int scale = (int) getResources().getDisplayMetrics().density;
            boolean bottom = getPreferences().getBoolean("isMarginBottomNav",false);
            top = !getPreferences().getBoolean("isShowShortUserInfo",true);
            if(bottom){
                leftDrawer.setPadding(0,0,0,(int) (48 * scale + 0.5f));
            }
            if(top){
                leftDrawer.setPadding(0,(int) (25 * scale + 0.5f),0,0);
            }
            if(top&bottom){
                leftDrawer.setPadding(0,(int) (25 * scale + 0.5f),0,(int) (48 * scale + 0.5f));
            }

            mTabDraweMenu = new TabDrawerMenu(this, this);
            mMainDrawerMenu = new MainDrawerMenu(this, this);

            NotifiersManager notifiersManager = new NotifiersManager(this);
            new DonateNotifier(notifiersManager).start(this);
            new TopicAttentionNotifier(notifiersManager).start(this);
            new ForPdaVersionNotifier(notifiersManager, 1).start(this);
        } catch (Throwable ex) {
            AppLog.e(getApplicationContext(), ex);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mMainDrawerMenu != null) {
            mMainDrawerMenu.syncState();
        }

        if(!top)
            new ShortUserInfo(this);
        else
            topInform.setVisibility(View.GONE);


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
        selectFragment(listTemplate.getTitle(), "", listTemplate.getName(), listTemplate.createFragment());
        mTabDraweMenu.addTab(listTemplate.getTitle(), listTemplate.getName(), listTemplate.getName(), listTemplate.createFragment(), false);

        mTabDraweMenu.notifyDataSetChanged();
    }
    public void selectTab(TabItem tabItem){
        selectFragment(tabItem.getTitle(), tabItem.getUrl(), tabItem.getTag(), tabItem.getFragment());

        mMainDrawerMenu.notifyDataSetChanged();
    }

    private void selectFragment(final String title, final String url, final String tag, final Fragment fragment){
        if(mTabDraweMenu!=null) mTabDraweMenu.close();
        if(mMainDrawerMenu!=null) mMainDrawerMenu.close();
        String currentFragmentTag = String.valueOf(App.getInstance().getCurrentFragmentTag());


        endActionFragment(title, tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        if (tag.equals(currentFragmentTag)) {
            if(getSupportFragmentManager().findFragmentByTag(currentFragmentTag) == null) {

                addFragment(transaction, fragment, tag);
            }else {
                showFragmentByTag(transaction, tag);
            }
        }else{
            if (currentFragmentTag.equals("null")) {
                addFragment(transaction, fragment, tag);
            }else {
                if(getSupportFragmentManager().findFragmentByTag(tag)==null){
                    addFragment(transaction, fragment, tag);
                }else {
                    showFragmentByTag(transaction, tag);
                    if(Preferences.Lists.isRefresh()&!tag.equals("News_Pages"))
                        ((IBrickFragment)getSupportFragmentManager().findFragmentByTag(tag)).loadData(true);
                }
            }
        }
        transaction.commit();

    }

    public void hideFragments(FragmentTransaction transaction){
        if(getSupportFragmentManager().getFragments()==null) return;
        for (Fragment fr:getSupportFragmentManager().getFragments()) {
            if (fr != null) {
                if (!fr.getTag().equals("f1")){
                    fr.onPause();
                    transaction.hide(fr);
                }
            }
        }
    }

    private void showFragmentByTag(FragmentTransaction transaction, String tag){
        getSupportFragmentManager().findFragmentByTag(tag).onResume();
        transaction.show(getSupportFragmentManager().findFragmentByTag(tag));
    }
    public void showFragmentByTag(String tag, boolean onresume){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(onresume) getSupportFragmentManager().findFragmentByTag(tag).onResume();
        transaction.show(getSupportFragmentManager().findFragmentByTag(tag));
        transaction.commit();
    }

    private void showFragment(FragmentTransaction transaction, Fragment fragment){
        transaction.show(fragment);
    }

    private void addFragment(FragmentTransaction transaction, Fragment fragment, String tag) {
        transaction.add(R.id.content_frame, fragment, tag);
    }

    public void showFragment(String title,String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.show(getSupportFragmentManager().findFragmentByTag(tag));
        transaction.commit();
    }

    public void endActionFragment(String title, String tag){
        App.getInstance().setCurrentFragmentTag(tag);
        endActionFragment(title);
    }
    public void endActionFragment(String title){
        if (getSupportActionBar() != null) {
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);// новости выставляют выпадающий список
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            //getSupportActionBar().setSubtitle(null);
        }
        setTitle(title);
    }

    public static void selectTabByTag(String tag){
        mTabDraweMenu.selectTab(App.getInstance().getTabByTag(tag));;
    }

    public void addTab(String title, String url, Fragment fragment){
        if(getSupportFragmentManager().findFragmentByTag(url)==null) {
            mTabDraweMenu.addTab(title, url, tabPrefix + App.getInstance().getTabIterator(), fragment, true);
        }else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideFragments(transaction);
            showFragmentByTag(transaction, url);
            transaction.commit();
            endActionFragment(title);
        }
    }

    public static void addTabByIntent(String url, Fragment fragment){
        addTabByIntent("ForPDA", url, fragment);
    }
    public static void addTabByIntent(String title, String url, Fragment fragment){
        App.getInstance().setCurrentFragmentTag(tabPrefix + App.getInstance().getTabIterator());

        //mTabDraweMenu.addTab(title, url, tabPrefix + App.getInstance().getTabIterator(), fragment, false);
        if(activityPaused){
            tabOnIntent = new TabItem(title, url, tabPrefix + App.getInstance().getTabIterator(), fragment);
        }else {
            mTabDraweMenu.addTab(title, url, tabPrefix + App.getInstance().getTabIterator(), fragment, true);
        }

    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        //if(isNewIntent)
        if(App.getInstance().getCurrentFragmentTag()==null){
            BrickInfo brickInfo = ListCore.getRegisteredBrick(Preferences.Lists.getLastSelectedList());
            if (brickInfo == null)
                brickInfo = new NewsPagerBrickInfo();
            selectItem(brickInfo);
        } else if(tabOnIntent!=null) {
            mTabDraweMenu.addTab(tabOnIntent.getTitle(), tabOnIntent.getUrl(), tabOnIntent.getTag(), tabOnIntent.getFragment(), true);
        }
        tabOnIntent = null;
        activityPaused = false;
    }

    public void tryRemoveTab(String tag){
        if(!((IBrickFragment)getSupportFragmentManager().findFragmentByTag(tag)).onBackPressed()) removeTab(tag);
    }
    public void removeTab(String tag){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        transaction.remove(getSupportFragmentManager().findFragmentByTag(tag));
        if(tag.equals("News_Pages"))
            transaction.remove(getSupportFragmentManager().findFragmentByTag("News_List"));
        transaction.commit();
        mTabDraweMenu.removeTab(tag);
        mMainDrawerMenu.notifyDataSetChanged();

    }

    @Override
    protected void onResumeFragments() {}

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }
    private Boolean m_ExitWarned = false;

    private void appExit() {

        App.getInstance().exit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
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
            if (mTabDraweMenu.isOpen()) {
                mTabDraweMenu.close();
                return;
            }
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
            if (currentFragment == null || !((IBrickFragment) currentFragment).onBackPressed()) {
                if(App.getInstance().getTabItems().size()<=1){
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
                        App.getInstance().setCurrentFragmentTag(null);
                        App.getInstance().getTabItems().clear();;
                        App.getInstance().clearTabIterator();
                        appExit();
                    }
                }else {
                    removeTab(App.getInstance().getCurrentFragmentTag());
                }

            } else {
                m_ExitWarned = false;
            }
        } catch (Throwable ignored) {
            appExit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
    }



    @Override
    public void onResume() {
        super.onResume();
        if(App.getInstance().getThemeStyleResID()!=lastTheme) reload();
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
                                        editor.apply();
                                        menuItem.setChecked(true);
                                        return true;
                                    }
                                });
                        android.view.Menu defaultActionMenu = menu.addSubMenu("Действие по умолчанию");
                        String[] actionsArray = getResources().getStringArray(R.array.ThemeActionsArray);
                        final String[] actionsValues = getResources().getStringArray(R.array.ThemeActionsValues);
                        final String actionPrefName = "tabs." + tabId + ".Action";
                        String defaultAction = prefs.getString(actionPrefName, "getfirstpost");
                        for (int i = 0; i < actionsValues.length; i++) {
                            final int finalI = i;
                            defaultActionMenu.add(actionsArray[i])
                                    .setCheckable(true).setChecked(defaultAction != null && defaultAction.equals(actionsValues[i]))
                                    .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(actionPrefName, actionsValues[finalI]);
                                            editor.apply();
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
                    .setIcon(R.drawable.ic_magnify_white_24dp)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            SearchSettingsDialogFragment.showSearchSettingsDialog(getActivity(),
                                    SearchSettingsDialogFragment.createForumSearchSettings());
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            //setOtherMenu();
            menu.add("Правила форума").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    StringBuilder text = new StringBuilder();
                    try {

                        BufferedReader br = new BufferedReader(new InputStreamReader(App.getInstance().getAssets().open("rules.txt"), "UTF-8"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line).append("\n");
                        }

                    } catch (IOException e) {
                        AppLog.e(getActivity(), e);
                    }
                    new MaterialDialog.Builder(getActivity())
                            .title("Правила форума")
                            .content(Html.fromHtml(text.toString()))
                            .positiveText(android.R.string.ok)
                            .show();

                    return true;
                }
            });

            item = menu.add(0, 0, 999, R.string.CloseApp)
                    .setIcon(R.drawable.ic_close_white_24dp)
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