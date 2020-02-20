package org.softeg.slartus.forpdaplus;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.activity.NewYear;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.DownloadFragment;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchPostFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchTopicsFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listfragments.mentions.MentionsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.mainnotifiers.DonateNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends BaseActivity implements BricksListDialogFragment.IBricksListDialogCaller,
        MainDrawerMenu.SelectItemListener, TabDrawerMenu.SelectItemListener {
    // test commit to beta
    public static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, R.string.permission_grented, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    private final static String tabPrefix = "tab";
    private Handler mHandler = new Handler();

    private MainDrawerMenu mMainDrawerMenu;
    private static TabDrawerMenu mTabDraweMenu;
    public Toolbar toolbar;
    boolean top;
    int lastTheme;
    private static TabItem tabOnIntent = null;
    private static String tabTagForRemove = null;
    private static boolean activityPaused = false;
    private View toolbarShadow;
    private AppBarLayout appBarLayout;
    private RelativeLayout statusBar;
    private RelativeLayout fakeStatusBar;
    private int statusBarHeight = -1;
    private Runnable setStatusBarHeight = new Runnable() {
        @Override
        public void run() {
            int[] ints = new int[2];
            appBarLayout.getLocationInWindow(ints);
            statusBarHeight = ints[1];

            if (statusBar != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) statusBar.getLayoutParams();
                params.height = statusBarHeight;
                statusBar.setLayoutParams(params);
            }

            if (getPreferences().getBoolean("statusbarFake", false) & Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                if (fakeStatusBar != null) {
                    fakeStatusBar.setVisibility(View.VISIBLE);
                    fakeStatusBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, statusBarHeight));
                }
            }
        }
    };


    public static SearchSettings searchSettings;


    private static final int MSG_RECREATE = 1337;
    Handler handler = new Handler(msg -> {
        if (msg.what == MSG_RECREATE)
            recreate();
        return true;
    });

    public Handler getHandler() {
        return mHandler;
    }

    public MainDrawerMenu getmMainDrawerMenu() {
        return mMainDrawerMenu;
    }

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
        setTheme(App.getInstance().getThemeStyleResID());
        super.onCreate(saveInstance);

        loadPreferences(App.getInstance().getPreferences());
        if (shortUserInfo != null)
            shortUserInfo.setMActivity(new WeakReference<>(this));
        if (saveInstance != null) {
            App.getInstance().setTabIterator(saveInstance.getInt("tabIterator"));
            App.getInstance().setCurrentFragmentTag(saveInstance.getString("currentTag"));
        }

        final List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        if (fragmentList != null & App.getInstance().getTabItems().size() == 0) {
            GeneralFragment frag;
            TabItem item;
            for (Fragment fragment : fragmentList) {
                try {
                    if (fragment instanceof GeneralFragment) {
                        frag = (GeneralFragment) fragment;
                        item = new TabItem(frag.getGeneralTitle(), frag.getGeneralUrl(), frag.getTag(), frag.getGeneralParentTag(), frag);
                        frag.setThisTab(item);
                        App.getInstance().getTabItems().add(item);
                    }
                } catch (ClassCastException ex) {
                    AppLog.e(ex);
                }
            }
        }
        try {
            if (!checkIntent()) {
                if (saveInstance == null)
                    finish();
                return;
            }
            //Фиксим intent
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            setIntent(intent);
            lastTheme = App.getInstance().getThemeStyleResID();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            if (getPreferences().getBoolean("coloredNavBar", true) && Build.VERSION.SDK_INT >= 21)
                getWindow().setNavigationBarColor(App.getInstance().getResources().getColor(App.getInstance().getNavBarColor()));


            setContentView(R.layout.main);
            toolbar = findViewById(R.id.toolbar);
            appBarLayout = findViewById(R.id.appbarlayout);
            toolbarShadow = findViewById(R.id.toolbar_shadow);
            if (Build.VERSION.SDK_INT > 20) {
                toolbarShadow.setVisibility(View.GONE);
                toolbar.setElevation(6);
                appBarLayout.setElevation(6);
            }

            setSupportActionBar(toolbar);
            if (App.getInstance().getPreferences().getBoolean("titleMarquee", false)) {
                Field field = Toolbar.class.getDeclaredField("mTitleTextView");
                field.setAccessible(true);
                Object value = field.get(toolbar);
                if (value != null) {
                    TextView textView = (TextView) value;
                    textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    textView.setHorizontallyScrolling(true);
                    textView.setMarqueeRepeatLimit(3);
                    textView.setSelected(true);
                }
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);
            }
            statusBar = findViewById(R.id.status_bar);
            fakeStatusBar = findViewById(R.id.fakeSB);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                switch (App.getInstance().getThemeType()) {
                    case App.THEME_TYPE_LIGHT:
                        statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_light));
                        break;
                    case App.THEME_TYPE_DARK:
                        statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_dark));
                        break;
                    default:
                        statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_black));
                }
                getWindow().getDecorView().post(setStatusBarHeight);
            }

            NewYear.check(this);
            NavigationView leftDrawer = findViewById(R.id.left_drawer);
            int scale = (int) getResources().getDisplayMetrics().density;
            boolean bottom = getPreferences().getBoolean("isMarginBottomNav", false);
            top = !getPreferences().getBoolean("isShowShortUserInfo", true);
            if (bottom) {
                leftDrawer.setPadding(0, 0, 0, (int) (48 * scale + 0.5f));
            }
            if (top) {
                leftDrawer.setPadding(0, (int) (25 * scale + 0.5f), 0, 0);
            }
            if (top & bottom) {
                leftDrawer.setPadding(0, (int) (25 * scale + 0.5f), 0, (int) (48 * scale + 0.5f));
            }

            mTabDraweMenu = new TabDrawerMenu(this, this);
            mMainDrawerMenu = new MainDrawerMenu(this, this);

            searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings();

            NotifiersManager notifiersManager = new NotifiersManager();
            new DonateNotifier(notifiersManager).start(this);
            //new TopicAttentionNotifier(notifiersManager).start(this);
            new ForPdaVersionNotifier(notifiersManager, 1, false).start(this);
            activityPaused = false;
            if (App.getInstance().getCurrentFragmentTag() != null)
                if (App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()) != null) {
                    selectTab(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()));
                }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_WRITE_STORAGE);
        } catch (Throwable ex) {
            AppLog.e(getApplicationContext(), ex);
        }
    }


    public void hidePopupWindows() {
        InputMethodManager service = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        assert service != null;
        View currentFocus = this.getCurrentFocus();
        assert currentFocus != null;
        service.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
        if (fragment != null)
            ((GeneralFragment) fragment).hidePopupWindows();
    }

    public View getToolbarShadow() {
        return toolbarShadow;
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public void setArrow(final boolean b, final View.OnClickListener listener) {
        if (mMainDrawerMenu == null) return;
        mMainDrawerMenu.getDrawerToggle().setDrawerIndicatorEnabled(!b);
        mMainDrawerMenu.getDrawerToggle().setToolbarNavigationClickListener(listener);
    }

    private boolean lastHamburgerArrow = true;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator();

    private View.OnClickListener toggleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMainDrawerMenu != null)
                mMainDrawerMenu.toggleOpenState();
        }
    };
    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            hidePopupWindows();
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };

    public void animateHamburger(final boolean isArrow, final View.OnClickListener listener) {
        if (toolbar == null) return;
        DrawerLayout drawerLayout = getmMainDrawerMenu().getDrawerLayout();
        if (isArrow) {
            toolbar.setNavigationOnClickListener(toggleListener);
            drawerLayout.setDrawerListener(getmMainDrawerMenu().getDrawerToggle());
        } else {
            if (listener != null) toolbar.setNavigationOnClickListener(listener);
            drawerLayout.setDrawerListener(drawerListener);
        }
        if (isArrow == lastHamburgerArrow) return;

        ValueAnimator anim = ValueAnimator.ofFloat(isArrow ? 1.0f : 0.0f, isArrow ? 0.0f : 1.0f);
        anim.addUpdateListener(valueAnimator -> getmMainDrawerMenu().getDrawerToggle().onDrawerSlide(getmMainDrawerMenu().getDrawerLayout(), (Float) valueAnimator.getAnimatedValue()));
        anim.setInterpolator(interpolator);
        anim.setDuration(250);
        anim.start();
        lastHamburgerArrow = isArrow;
    }

    private ShortUserInfo shortUserInfo;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mMainDrawerMenu == null) {
            mMainDrawerMenu = new MainDrawerMenu(this, this);
        }

        mMainDrawerMenu.close();

        if (mTabDraweMenu == null)
            mTabDraweMenu = new TabDrawerMenu(this, this);
        mTabDraweMenu.close();

        if (!top)
            shortUserInfo = new ShortUserInfo(this, mMainDrawerMenu.getNavigationView().getHeaderView(0));
        else
            mMainDrawerMenu.getNavigationView().getHeaderView(0).setVisibility(View.GONE);

        Client.INSTANCE.checkLoginByCookies();

        addToDisposable(UserInfoRepository
                .Companion.getInstance()
                .getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userInfo -> invalidateOptionsMenu()));
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (intent.getStringExtra("template") != null) {
            if (intent.getStringExtra("template").equals(DownloadFragment.TEMPLATE)) {
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
        /*if (IntentActivity.checkSendAction(this, intent))
            return false;*/
        if (intent.getAction() == null)
            intent.setAction(Intent.ACTION_MAIN);

        if (intent.getCategories() == null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

        if (intent.getAction().equals(Intent.ACTION_SEND) | intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
            Toast.makeText(getContext(), "Данное действие временно не поддерживается", Toast.LENGTH_SHORT).show();
            return false;
        }
        //intent.setData(Uri.parseCount("http://4pda.ru/forum/lofiversion/index.php?t365142-1650.html"));
        if (intent.getData() != null) {

            final String url = intent.getData().toString();

            if (IntentActivity.tryShowUrl(this, mHandler, url, false, true)) {
                return true;
            }
            //startNextMatchingActivity(intent);
            Toast.makeText(this, getString(R.string.links_not_supported) + ":\n" + url, Toast.LENGTH_LONG).show();
            //finish();
            return false;
        }
        return true;
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
        Fragment fragment = listTemplate.createFragment();
        selectFragment(listTemplate.getTitle(), listTemplate.getName(), fragment);
        addTabToList(listTemplate.getTitle(), listTemplate.getName(), listTemplate.getName(), fragment, false);
    }

    public void selectTab(TabItem tabItem) {
        selectFragment(tabItem.getTitle(), tabItem.getTag(), tabItem.getFragment());
    }

    private void selectFragment(final String title, final String tag, final Fragment fragment) {
        if (mTabDraweMenu != null) {
            mTabDraweMenu.close();
            notifyTabAdapter();
        }
        if (mMainDrawerMenu != null) {
            mMainDrawerMenu.close();
            mMainDrawerMenu.setItemCheckable(title);
        }

        String currentFragmentTag = String.valueOf(App.getInstance().getCurrentFragmentTag());

        endActionFragment(title, tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, true);
        if (tag.equals(currentFragmentTag)) {
            if (getSupportFragmentManager().findFragmentByTag(currentFragmentTag) == null) {
                addFragment(transaction, fragment, tag);
            } else {
                showFragment(transaction, tag);
            }
        } else {
            if (currentFragmentTag.equals("null")) {
                addFragment(transaction, fragment, tag);
            } else {
                if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
                    addFragment(transaction, fragment, tag);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
                } else {
                    showFragment(transaction, tag);
                    if (Preferences.Lists.isRefreshOnTab())
                        handler.postDelayed(() -> ((IBrickFragment) getSupportFragmentManager().findFragmentByTag(tag)).loadData(true), 300);
                }
            }
        }
        transaction.commitAllowingStateLoss();
    }

    public void hideFragments(FragmentTransaction transaction, boolean withAnimation) {
        if (getSupportFragmentManager().getFragments() == null) return;
        if (withAnimation)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        else
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        for (Fragment fr : getSupportFragmentManager().getFragments()) {
            if (fr != null) {
                if (fr.isVisible())
                    fr.onPause();
                transaction.hide(fr);
            }
        }
    }

    private void showFragment(FragmentTransaction transaction, String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        fragment.onResume();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
    }

    public void showFragment(String tag, boolean onresume) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            if (App.getInstance().getTabByTag(tag) != null) {
                TabItem tabItem = App.getInstance().getTabByTag(tag);
                addTab(tabItem.getTitle(), tabItem.getUrl(), tabItem.getFragment());
                return;
            }
            transaction.commitAllowingStateLoss();
            return;
        }
        if (onresume) fragment.onResume();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
        transaction.commitAllowingStateLoss();
    }

    private void addFragment(FragmentTransaction transaction, Fragment fragment, String tag) {
        if (fragment.isAdded()) return;
        transaction.add(R.id.content_frame, fragment, tag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
    }

    public void endActionFragment(String title, String tag) {
        App.getInstance().setCurrentFragmentTag(tag);
        endActionFragment(title);
    }

    public void endActionFragment(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);// новости выставляют выпадающий список
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            //getSupportActionBar().setSubtitle(null);
        }
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        outState.putInt("tabIterator", App.getInstance().getTabIterator());
        outState.putString("currentTag", App.getInstance().getCurrentFragmentTag());
        super.onSaveInstanceState(outState);
        if (hack) {
            onStop();
            onStart();
        }
        hack = false;
    }

    public static SharedPreferences getPreferences() {
        return App.getInstance().getPreferences();
    }

    public void notifyTabAdapter() {
        if (mTabDraweMenu != null)
            if (TabDrawerMenu.adapter != null)
                mTabDraweMenu.notifyDataSetChanged();
    }

    private String lang = null;

    @Override
    public void onResume() {
        super.onResume();
        if (lang == null) {
            lang = getPreferences().getString("lang", "default");
        }
        if (!getPreferences().getString("lang", "default").equals(lang)) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.lang_changed)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                        assert mgr != null;
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
        if (App.getInstance().getThemeStyleResID() != lastTheme) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_RECREATE;
            handler.sendMessage(msg);
        }
        m_ExitWarned = false;
        onStart();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        activityPaused = false;
        if (App.getInstance().getCurrentFragmentTag() == null) {
            BrickInfo brickInfo = ListCore.getRegisteredBrick(Preferences.Lists.getLastSelectedList());
            if (brickInfo == null)
                brickInfo = new NewsPagerBrickInfo();
            selectItem(brickInfo);
        }
        if (tabOnIntent != null) {
            addTabToList(tabOnIntent.getTitle(), tabOnIntent.getUrl(), tabOnIntent.getTag(), tabOnIntent.getFragment(), true);
        }
        if (tabTagForRemove != null) {
            tryRemoveTab(tabTagForRemove, true);
        }
        tabTagForRemove = null;
        tabOnIntent = null;

        if (!(String.valueOf(App.getInstance().getCurrentFragmentTag())).equals("null")) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
            if (fragment != null) {
                fragment.onResume();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int[] ints = new int[2];
            appBarLayout.getLocationOnScreen(ints);
            if (statusBarHeight != ints[1] && ints[1] != 0)
                setStatusBarHeight.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
        if (!(String.valueOf(App.getInstance().getCurrentFragmentTag())).equals("null")) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
            if (fragment != null) {
                fragment.onPause();
            }
        }
    }


    public static void addTab(String url, Fragment fragment) {
        addTab("ForPDA", url, fragment);
    }

    public static void addTab(String title, String url, Fragment fragment) {
        if (activityPaused | mTabDraweMenu == null) {
            tabOnIntent = new TabItem(title, url, tabPrefix + App.getInstance().getTabIterator(), App.getInstance().getCurrentFragmentTag(), fragment);
        } else {
            addTabToList(title, url, tabPrefix + App.getInstance().getTabIterator(), fragment, true);
        }
        if (!App.getInstance().isContainsByUrl(url)) {
            String newTag = tabPrefix + (App.getInstance().getTabIterator() - 1);
            App.getInstance().setCurrentFragmentTag(newTag);
        }
    }

    public static void addTabToList(String name, String url, String tag, Fragment fragment, boolean select) {
        TabItem item = null;
        if (App.getInstance().isContainsByUrl(url)) {
            if (select) item = App.getInstance().getTabByUrl(url);
        } else if (!App.getInstance().isContainsByTag(tag)) {
            item = new TabItem(name, url, tag, App.getInstance().getCurrentFragmentTag(), fragment);
            ((GeneralFragment) fragment).setThisTab(item);
            App.getInstance().getTabItems().add(item);
            App.getInstance().plusTabIterator();
            mTabDraweMenu.refreshAdapter();
        } else {
            if (select) item = App.getInstance().getTabByTag(tag);
        }

        if (select) mTabDraweMenu.selectTab(item);
    }

    public void tryRemoveTab(String tag) {
        tryRemoveTab(tag, false);
    }

    public void tryRemoveTab(String tag, boolean tryClose) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && App.getInstance().getTabByTag(tag) != null)
            if (tryClose) {
                if (!((GeneralFragment) fragment).closeTab())
                    removeTab(tag);
            } else {
                removeTab(tag);
            }
    }

    public void removeTab(String tag) {
        if (activityPaused | mTabDraweMenu == null) {
            tabTagForRemove = tag;
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideFragments(transaction, false);
            transaction.remove(getSupportFragmentManager().findFragmentByTag(tag));
            transaction.commitAllowingStateLoss();
            mTabDraweMenu.removeTab(tag);
        }
    }

    public void removeTabs(List<TabItem> items) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, false);
        for (TabItem item : items) {
            transaction.remove(item.getFragment());
            App.getInstance().getTabItems().remove(item);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * Конец этого ужаса
     */


    public static void showListFragment(String brickName, Bundle extras) {
        showListFragment("", brickName, extras);
    }

    public static void showListFragment(String prefix, String brickName, Bundle extras) {
        final BrickInfo listTemplate = ListCore.getRegisteredBrick(brickName);
        assert listTemplate != null;
        Fragment fragment = listTemplate.createFragment();
        fragment.setArguments(extras);
        addTab(listTemplate.getTitle(), prefix + brickName, fragment);
    }

    private Boolean m_ExitWarned = false;

    public void appExit() {
        App.getInstance().setCurrentFragmentTag(null);
        App.getInstance().getTabItems().clear();
        App.getInstance().clearTabIterator();
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
                if (App.getInstance().getTabItems().size() <= 1) {
                    if (!m_ExitWarned) {
                        Toast.makeText(this, R.string.close_program_toasr, Toast.LENGTH_SHORT).show();
                        m_ExitWarned = true;
                        new Handler().postDelayed(() -> m_ExitWarned = false, 3 * 1000);
                    } else {
                        appExit();
                    }
                } else {
                    tryRemoveTab(App.getInstance().getCurrentFragmentTag(), true);
                }

            } else {
                m_ExitWarned = false;
            }
        } catch (Throwable ignored) {
            appExit();
        }
    }

    private int getUserIconRes() {
        Boolean logged = Client.getInstance().getLogined();
        if (logged) {
            if (Client.getInstance().getQmsCount() > 0 || UserInfoRepository.Companion.getInstance().getUserInfo().getValue().mentionsCountOrDefault(0) > 0) {
                return R.drawable.message_text;
            }
            return R.drawable.account;
        } else {
            return R.drawable.account_outline;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.user, menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.tabs_item:
                mTabDraweMenu.toggleOpenState();
                return true;
            case R.id.search_item:
                SearchSettingsDialogFragment.showSearchSettingsDialog(MainActivity.this, searchSettings);
                return true;
            case R.id.exit_item:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return true;
            case R.id.qms_item:
                QmsContactsBrickInfo brickInfo = new QmsContactsBrickInfo();
                MainActivity.addTab(brickInfo.getTitle(), brickInfo.getName(), brickInfo.createFragment());
                return true;
            case R.id.mentions_item:
                MainActivity.addTab("Упоминания", "http://4pda.ru/forum/index.php?act=mentions",
                        MentionsListFragment.Companion.newFragment());
                return true;
            case R.id.profile_item:
                ProfileFragment.showProfile(UserInfoRepository.Companion.getInstance().getId(), Client.getInstance().getUser());
                return true;
            case R.id.reputation_item:
                UserReputationFragment.showActivity(UserInfoRepository.Companion.getInstance().getId(), false);
                return true;
            case R.id.logout_item:
                LoginDialog.logout(MainActivity.this);
                return true;
            case R.id.login_item:
                LoginDialog.showDialog(MainActivity.this);
                return true;
            case R.id.registration_item:
                Intent marketIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://4pda.ru/forum/index.php?act=Reg&CODE=00"));
                MainActivity.this.startActivity(marketIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        refreshUserMenu(menu);
        menu.findItem(R.id.tabs_item).setVisible(getPreferences().getBoolean("openTabDrawerButton", false));
        menu.findItem(R.id.exit_item).setVisible(getPreferences().getBoolean("showExitButton", false));
        return super.onPrepareOptionsMenu(menu);
    }

    private void refreshUserMenu(Menu menu){
        boolean logged = UserInfoRepository.Companion.getInstance().getLogined();
        menu.findItem(R.id.guest_item).setVisible(!logged);
        MenuItem userMenuItem = menu.findItem(R.id.user_item);
        userMenuItem.setVisible(logged);
        if(logged) {
            userMenuItem.setTitle(UserInfoRepository.Companion.getInstance().getName());
            userMenuItem.setIcon(getUserIconRes());
            String qmsTitle= Client.getInstance().getQmsCount() > 0 ? ("QMS (" + Client.getInstance().getQmsCount() + ")") : "QMS";
            menu.findItem(R.id.qms_item).setTitle(qmsTitle);
            int mentionsCount = UserInfoRepository.Companion.getInstance().getUserInfo()
                    .getValue().mentionsCountOrDefault(0);
            menu.findItem(R.id.mentions_item).setTitle("Упоминания " + (mentionsCount > 0 ? ("(" + mentionsCount + ")") : ""));
        }
    }

    public static void startForumSearch(SearchSettings searchSettings) {

        String title = App.getContext().getString(R.string.search);
        if (searchSettings.getQuery() != null) {
            if (!searchSettings.getQuery().equals(""))
                title = searchSettings.getQuery();
        } else if (searchSettings.getUserName() != null) {
            if (!searchSettings.getUserName().equals(""))
                title = App.getContext().getString(R.string.search) + ": " + searchSettings.getUserName();
        }

        if (SearchSettings.RESULT_VIEW_TOPICS.equals(searchSettings.getResultView()))
            MainActivity.addTab(title, searchSettings.getSearchQuery(), SearchTopicsFragment.newFragment(searchSettings.getSearchQuery()));
        else
            MainActivity.addTab(title, searchSettings.getSearchQuery(), SearchPostFragment.newFragment(searchSettings.getSearchQuery()));


    }

    @SuppressWarnings("ResourceType")
    protected void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }
}