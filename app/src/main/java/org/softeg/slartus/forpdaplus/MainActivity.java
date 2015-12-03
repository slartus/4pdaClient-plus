package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.DownloadFragment;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchPostFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchTopicsFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.mainnotifiers.DonateNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager;
import org.softeg.slartus.forpdaplus.mainnotifiers.TopicAttentionNotifier;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends AppCompatActivity implements BricksListDialogFragment.IBricksListDialogCaller,
        MainDrawerMenu.SelectItemListener, TabDrawerMenu.SelectItemListener {

    public static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_LONG).show();
            }
        }

    }


    private final static String tabPrefix = "tab";
    private Handler mHandler = new Handler();

    private MainDrawerMenu mMainDrawerMenu;
    private static TabDrawerMenu mTabDraweMenu;
    private RelativeLayout topInform;
    public Toolbar toolbar;
    boolean top;
    int lastTheme;
    private static TabItem tabOnIntent = null;
    private static boolean activityPaused = false;

    public static SearchSettings searchSettings;

    private static final int MSG_RECREATE = 1337;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_RECREATE)
                recreate();
        }
    };

    public Handler getHandler() {
        return mHandler;
    }

    public MainDrawerMenu getmMainDrawerMenu(){
        return mMainDrawerMenu;
    }
    public boolean statusBarShowed = false;
    public boolean hack = false;
    public Context getContext() {
        return this;
    }
    @Override
    public void startActivityForResult(android.content.Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        hack = true;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        setTheme(App.getInstance().getTransluentThemeStyleResID());
        super.onCreate(saveInstance);

        try {
            if (checkIntent()&saveInstance!=null) return;
            //Фиксим intent
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            setIntent(intent);
            lastTheme = App.getInstance().getThemeStyleResID();
            /*if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            if (PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("coloredNavBar", true) &&
                    Build.VERSION.SDK_INT >= 21)
                getWindow().setNavigationBarColor(App.getInstance().getResources().getColor(getNavBarColor()));


            setContentView(R.layout.main);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            if(Build.VERSION.SDK_INT>20) {
                findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
                toolbar.setElevation(4);
            }
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24dp);
            }
            if(PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("statusbarTransparent", false)) {
                if (Build.VERSION.SDK_INT >= 21)
                    getWindow().setStatusBarColor(Color.TRANSPARENT);
            }else {
                if (Build.VERSION.SDK_INT > 18) {
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

            RelativeLayout leftDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
            topInform = (RelativeLayout) findViewById(R.id.topInform);
            int scale = (int) getResources().getDisplayMetrics().density;
            boolean bottom = getPreferences().getBoolean("isMarginBottomNav",false);
            top = !getPreferences().getBoolean("isShowShortUserInfo",true);
            if(bottom){
                leftDrawer.setPadding(0, 0, 0, (int) (48 * scale + 0.5f));
            }
            if(top){
                leftDrawer.setPadding(0, (int) (25 * scale + 0.5f), 0, 0);
            }
            if(top&bottom){
                leftDrawer.setPadding(0, (int) (25 * scale + 0.5f), 0, (int) (48 * scale + 0.5f));
            }

            mTabDraweMenu = new TabDrawerMenu(this, this);
            mMainDrawerMenu = new MainDrawerMenu(this, this);

            searchSettings = SearchSettingsDialogFragment.createForumSearchSettings();

            NotifiersManager notifiersManager = new NotifiersManager(this);
            new DonateNotifier(notifiersManager).start(this);
            new TopicAttentionNotifier(notifiersManager).start(this);
            new ForPdaVersionNotifier(notifiersManager, 1).start(this);
            activityPaused = false;
            if(App.getInstance().getCurrentFragmentTag()!=null)
                if(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag())!=null)
                    selectTab(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()));



        } catch (Throwable ex) {
            AppLog.e(getApplicationContext(), ex);
        }
    }
    public void setArrow(final boolean b, final View.OnClickListener listener){
        mMainDrawerMenu.getmDrawerToggle().setDrawerIndicatorEnabled(!b);
        mMainDrawerMenu.getmDrawerToggle().setToolbarNavigationClickListener(listener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mMainDrawerMenu != null)
            mMainDrawerMenu.close();

        if(!top)
            new ShortUserInfo(this);
        else
            topInform.setVisibility(View.GONE);

        Client.INSTANCE.checkLoginByCookies();
        Client.getInstance().addOnUserChangedListener(new Client.OnUserChangedListener() {
            @Override
            public void onUserChanged(String user, Boolean success) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUserMenu();
                    }
                });
            }
        });
        Client.getInstance().addOnMailListener(new Client.OnMailListener() {
            @Override
            public void onMail(int count) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUserMenu();
                    }
                });
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getStringExtra("template")!=null){
            if(intent.getStringExtra("template").equals(DownloadFragment.TEMPLATE)){
                DownloadFragment.newInstance();
                return;
            }
        }

        checkIntent(intent);
    }

    private boolean checkIntent() {
        return checkIntent(getIntent());
    }

    private boolean checkIntent(final Intent intent) {
        log("intent: "+intent);
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
        showListFragment(brickInfo.getName(), args);
    }

    /**
     * Swaps fragments in the main content view
     */


    public void selectItem(final BrickInfo listTemplate) {
        selectFragment(listTemplate.getTitle(), "", listTemplate.getName(), listTemplate.createFragment());
        addTabToList(listTemplate.getTitle(), listTemplate.getName(), listTemplate.getName(), listTemplate.createFragment(), false);
    }
    public void selectTab(TabItem tabItem){
        selectFragment(tabItem.getTitle(), tabItem.getUrl(), tabItem.getTag(), tabItem.getFragment());
    }

    private void selectFragment(final String title, final String url, final String tag, final Fragment fragment){
        if(mTabDraweMenu!=null){
            mTabDraweMenu.close();
            mTabDraweMenu.notifyDataSetChanged();
        }
        if(mMainDrawerMenu!=null){
            mMainDrawerMenu.close();
            mMainDrawerMenu.notifyDataSetChanged();
        }
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
                    transaction.show(fragment);
                }else {
                    showFragmentByTag(transaction, tag);
                    if(Preferences.Lists.isRefreshOnTab())
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
                if(fr.isVisible())
                    fr.onPause();
                transaction.hide(fr);
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

    private void addFragment(FragmentTransaction transaction, Fragment fragment, String tag) {
        if(fragment.isAdded()) return;
        transaction.add(R.id.content_frame, fragment, tag);
        transaction.show(fragment);
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

    public static void log(String s){
        Log.e("My log", s);
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        if(hack){
            onStop();
            onStart();
        }
        hack= false;
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle outState) {
        super.onRestoreInstanceState(outState);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public int getNavBarColor(){
        if(App.getInstance().isWhiteTheme())
            return R.color.actionbar_background_wh;
        else
            return R.color.actionbar_background_bl;
    }
    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /**
     * Управление вкладками начало
     */
    @Override
    protected void onResumeFragments() {
        //super.onResumeFragments();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(App.getInstance().getThemeStyleResID()!=lastTheme) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_RECREATE;
            handler.sendMessage(msg);
        }
        m_ExitWarned = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(App.getInstance().getCurrentFragmentTag()==null){
            BrickInfo brickInfo = ListCore.getRegisteredBrick(Preferences.Lists.getLastSelectedList());
            if (brickInfo == null)
                brickInfo = new NewsPagerBrickInfo();
            selectItem(brickInfo);
        } else if(tabOnIntent!=null) {
            addTabToList(tabOnIntent.getTitle(), tabOnIntent.getUrl(), tabOnIntent.getTag(), tabOnIntent.getFragment(), true);
        }
        tabOnIntent = null;
        activityPaused = false;
        if(!(App.getInstance().getCurrentFragmentTag()+"").equals("null")){
            if(getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag())!=null)
                getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag()).onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
        if(!(App.getInstance().getCurrentFragmentTag()+"").equals("null"))
            if(getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag())!=null)
                getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag()).onPause();
    }



    public static void addTab(String url, Fragment fragment){
        addTab("ForPDA", url, fragment);
    }
    public static void addTab(String title, String url, Fragment fragment) {


        if(activityPaused|mTabDraweMenu==null){
            tabOnIntent = new TabItem(title, url, tabPrefix + App.getInstance().getTabIterator(), App.getInstance().getCurrentFragmentTag(), fragment);
        }else {
            addTabToList(title, url, tabPrefix + App.getInstance().getTabIterator(), fragment, true);
        }
        App.getInstance().setCurrentFragmentTag(tabPrefix + (App.getInstance().getTabIterator()-1));
    }

    public static void addTabToList(String name, String url, String tag, Fragment fragment, boolean select){
        TabItem item = null;
        if(App.getInstance().isContainsByUrl(url)){
            if(select) item = App.getInstance().getTabByUrl(url);

        }else if(!App.getInstance().isContainsByTag(tag)) {
            log(tag +" : "+App.getInstance().getCurrentFragmentTag());
            item = new TabItem(name, url, tag, App.getInstance().getCurrentFragmentTag(), fragment);
            App.getInstance().getTabItems().add(item);
            App.getInstance().plusTabIterator();

            mTabDraweMenu.refreshAdapter();
        }else {
            if(select) item = App.getInstance().getTabByTag(tag);
        }
        if(select) mTabDraweMenu.selectTab(item);
    }

    public static void selectTabByTag(String tag){
        mTabDraweMenu.selectTab(App.getInstance().getTabByTag(tag));;
    }

    public void tryRemoveTab(String tag){
        if(!((GeneralFragment)getSupportFragmentManager().findFragmentByTag(tag)).closeTab()) removeTab(tag);
    }
    public void removeTab(String tag){
        log("remove "+tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        transaction.remove(getSupportFragmentManager().findFragmentByTag(tag));
        transaction.commit();
        mTabDraweMenu.removeTab(tag);
        mMainDrawerMenu.notifyDataSetChanged();

    }

    /**
     * Конец этого ужаса
     */


    public static void showListFragment(String brickName, Bundle extras){
        showListFragment("", brickName, extras);
    }
    public static void showListFragment(String prefix, String brickName, Bundle extras){
        final BrickInfo listTemplate = ListCore.getRegisteredBrick(brickName);
        Fragment fragment = listTemplate.createFragment();
        fragment.setArguments(extras);
        addTab(listTemplate.getTitle(), prefix + brickName, fragment);
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






    public static Menu mainMenu;
    private SubMenu mUserMenuItem;



    private int getUserIconRes() {
        Boolean logged = Client.getInstance().getLogined();
        if (logged) {

            if (Client.getInstance().getQmsCount() > 0) {
                return R.drawable.ic_chat_white_24dp;
            }
            return R.drawable.ic_account_white_24dp;
        } else {
            return R.drawable.ic_account_outline_white_24dp;
        }
    }

    public void setUserMenu() {
        if (mUserMenuItem == null) return;
        Boolean logged = Client.getInstance().getLogined();

        mUserMenuItem.getItem().setIcon(getUserIconRes());
        mUserMenuItem.getItem().setTitle(Client.getInstance().getUser());
        mUserMenuItem.clear();
        if (logged) {
            String text = Client.getInstance().getQmsCount() > 0 ? ("QMS (" + Client.getInstance().getQmsCount() + ")") : "QMS";
            mUserMenuItem.add(text)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            //ListFragmentActivity.showListFragment(MainActivity.this, QmsContactsBrickInfo.NAME, null);
                            QmsContactsBrickInfo brickInfo = new QmsContactsBrickInfo();
                            MainActivity.addTab(brickInfo.getTitle(), brickInfo.getName(), brickInfo.createFragment());
                            return true;
                        }
                    });

            mUserMenuItem.add(R.string.Profile)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            ProfileFragment.showProfile(Client.getInstance().UserId, Client.getInstance().getUser());
                            return true;
                        }
                    });


            mUserMenuItem.add(R.string.Reputation)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            UserReputationFragment.showActivity(MainActivity.this, Client.getInstance().UserId, false);
                            return true;
                        }
                    });

            mUserMenuItem.add(R.string.Logout)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            LoginDialog.logout(MainActivity.this);
                            return true;
                        }
                    });
        } else {
            mUserMenuItem.add(R.string.Login).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    LoginDialog.showDialog(MainActivity.this, null);
                    return true;
                }
            });

            mUserMenuItem.add(R.string.Registration).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    Intent marketIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://4pda.ru/forum/index.php?act=Reg&CODE=00"));
                    MainActivity.this.startActivity(marketIntent);
                    //
                    return true;
                }
            });
        }
    }

    private void createUserMenu(Menu menu) {
        mUserMenuItem = menu.addSubMenu(Client.getInstance().getUser());

        mUserMenuItem.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setUserMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.clear();

        createUserMenu(menu);

        menu.add(R.string.Search)
                .setIcon(R.drawable.ic_magnify_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        SearchSettingsDialogFragment.showSearchSettingsDialog(MainActivity.this, searchSettings);
                        return true;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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
                    AppLog.e(MainActivity.this, e);
                }
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Правила форума")
                        .content(Html.fromHtml(text.toString()))
                        .positiveText(android.R.string.ok)
                        .show();

                return true;
            }
        });

        menu.add(0, 0, 999, R.string.CloseApp)
                .setIcon(R.drawable.ic_close_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                        return true;
                    }
                });

        mainMenu = menu;
        return false;
    }

    public static void startForumSearch(SearchSettings searchSettings){

        String title = "Поиск";
        if(searchSettings.getQuery()!=null){
            if(!searchSettings.getQuery().equals(""))
                title = searchSettings.getQuery();
        }else if(searchSettings.getUserName()!=null){
            if(!searchSettings.getUserName().equals(""))
                title = "Поиск: "+searchSettings.getUserName();
        }
        try {
            if (SearchSettings.RESULT_VIEW_TOPICS.equals(searchSettings.getResultView()))
                MainActivity.addTab(title, searchSettings.getSearchQuery(), SearchTopicsFragment.newFragment(searchSettings.getSearchQuery()));
            else
                MainActivity.addTab(title, searchSettings.getSearchQuery(), SearchPostFragment.newFragment(searchSettings.getSearchQuery()));
        }catch (URISyntaxException e){
            e.printStackTrace();
        }

    }
}