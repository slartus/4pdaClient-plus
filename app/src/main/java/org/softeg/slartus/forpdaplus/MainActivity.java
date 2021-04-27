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

import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.activity.NewYear;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.TabDrawerMenu.TabDrawerMenu;
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
import org.softeg.slartus.forpdaplus.repositories.TabsRepository;
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
        MainDrawerMenu.SelectItemListener, TabDrawerMenu.SelectItemListener, OnShowScreenListener {
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
    private final Handler mHandler = new Handler();

    private MainDrawerMenu mMainDrawerMenu;
    private TabDrawerMenu mTabDraweMenu;
    public Toolbar toolbar;
    boolean top;
    int lastTheme;


    private View toolbarShadow;
    private AppBarLayout appBarLayout;
    private RelativeLayout statusBar;
    private RelativeLayout fakeStatusBar;
    private int statusBarHeight = -1;
    private final Runnable setStatusBarHeight = new Runnable() {
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
        setTheme(AppTheme.getThemeStyleResID());
        super.onCreate(saveInstance);
        App.getInstance().screensController.setOnShowScreenListener(this);
        loadPreferences(App.getInstance().getPreferences());
        if (shortUserInfo != null)
            shortUserInfo.setMActivity(new WeakReference<>(this));
        if (saveInstance != null) {
            App.getInstance().setTabIterator(saveInstance.getInt("tabIterator"));
            TabsRepository.getInstance().setCurrentFragmentTag(saveInstance.getString("currentTag"));
        }

        final List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        if (fragmentList != null || TabsRepository.getInstance().size() == 0) {
            GeneralFragment frag;
            TabItem item;
            for (Fragment fragment : fragmentList) {
                try {
                    if (fragment instanceof GeneralFragment) {
                        frag = (GeneralFragment) fragment;
                        item = new TabItem(frag.getGeneralTitle(), frag.getGeneralUrl(), frag.getTag(), frag.getGeneralParentTag());

                        TabsRepository.getInstance().add(item);
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
            lastTheme = AppTheme.getThemeStyleResID();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            if (getPreferences().getBoolean("coloredNavBar", true) && Build.VERSION.SDK_INT >= 21)
                getWindow().setNavigationBarColor(App.getInstance().getResources().getColor(AppTheme.getNavBarColor()));


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
                switch (AppTheme.getThemeType()) {
                    case AppTheme.THEME_TYPE_LIGHT:
                        statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_light));
                        break;
                    case AppTheme.THEME_TYPE_DARK:
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

            if (TabsRepository.getInstance().getCurrentFragmentTag() != null)
                if (TabsRepository.getInstance().getTabByTag(TabsRepository.getInstance().getCurrentFragmentTag()) != null) {
                    selectTab(TabsRepository.getInstance().getTabByTag(TabsRepository.getInstance().getCurrentFragmentTag()));
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
        if (currentFocus == null)
            currentFocus = new View(this);
        service.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TabsRepository.getInstance().getCurrentFragmentTag());
        if (fragment != null)
            ((GeneralFragment) fragment).hidePopupWindows();
    }

    public View getToolbarShadow() {
        return toolbarShadow;
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    private boolean lastHamburgerArrow = true;
    private final DecelerateInterpolator interpolator = new DecelerateInterpolator();

    private final View.OnClickListener toggleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMainDrawerMenu != null)
                mMainDrawerMenu.toggleOpenState();
        }
    };
    private final DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
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
                .subscribe(userInfo -> invalidateOptionsMenu(), throwable -> AppLog.e(this, throwable)));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
        //intent.setData(Uri.parseCount("https://4pda.ru/forum/lofiversion/index.php?t365142-1650.html"));
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
    }

    public void selectTab(TabItem tabItem) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tabItem.getTag());
        selectFragment(tabItem.getTitle(), tabItem.getTag(), fragment);
    }

    private void selectFragment(final String title, final String tag, final Fragment fragment) {
        if (mTabDraweMenu != null) {
            mTabDraweMenu.close();
        }
        if (mMainDrawerMenu != null) {
            mMainDrawerMenu.close();
            mMainDrawerMenu.setItemCheckable(title);
        }

        String currentFragmentTag = String.valueOf(TabsRepository.getInstance().getCurrentFragmentTag());

        endActionFragment(title, tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, true);

        if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
            addFragment(transaction, fragment, tag);
            if (!tag.equals(currentFragmentTag))
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
        } else {
            showFragment(transaction, tag);
            if (Preferences.Lists.isRefreshOnTab())
                handler.postDelayed(() -> ((IBrickFragment) getSupportFragmentManager().findFragmentByTag(tag)).loadData(true), 300);
        }

        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction, boolean withAnimation) {
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
            if (TabsRepository.getInstance().getTabByTag(tag) != null) {
                TabItem tabItem = TabsRepository.getInstance().getTabByTag(tag);
                addTab(tabItem.getTitle(), tabItem.getUrl(), null);
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

    private void endActionFragment(String title, String tag) {
        TabsRepository.getInstance().setCurrentFragmentTag(tag);
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
        outState.putString("currentTag", TabsRepository.getInstance().getCurrentFragmentTag());
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
        if (AppTheme.getThemeStyleResID() != lastTheme) {
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

        if (TabsRepository.getInstance().getCurrentFragmentTag() == null) {
            BrickInfo brickInfo = ListCore.getRegisteredBrick(Preferences.Lists.getLastSelectedList());
            if (brickInfo == null)
                brickInfo = new NewsPagerBrickInfo();
            selectItem(brickInfo);
        }


        if (!(String.valueOf(TabsRepository.getInstance().getCurrentFragmentTag())).equals("null")) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TabsRepository.getInstance().getCurrentFragmentTag());
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
        if (!(String.valueOf(TabsRepository.getInstance().getCurrentFragmentTag())).equals("null")) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TabsRepository.getInstance().getCurrentFragmentTag());
            if (fragment != null) {
                fragment.onPause();
            }
        }
    }


    private void addTabToList(String name, String url, String tag, Fragment fragment) {
        selectFragment(name, tag, fragment);
    }

    public void tryRemoveTab(String tag) {
        tryRemoveTab(tag, false);
    }

    public void tryRemoveTab(String tag, boolean tryClose) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && TabsRepository.getInstance().getTabByTag(tag) != null)
            if (tryClose) {
                if (!((GeneralFragment) fragment).closeTab())
                    removeTab(tag);
            } else {
                removeTab(tag);
            }
    }

    private void removeTab(String tag) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, false);
        transaction.remove(getSupportFragmentManager().findFragmentByTag(tag));
        transaction.commitAllowingStateLoss();
        mTabDraweMenu.removeTab(tag);

    }

    public void removeTabs(List<TabItem> items) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, false);
        for (TabItem item : items) {
            transaction.remove(getSupportFragmentManager().findFragmentByTag(item.getTag()));
            TabsRepository.getInstance().remove(item);
        }
        transaction.commitAllowingStateLoss();
    }

    public static void showListFragment(String brickName, Bundle extras) {
        showListFragment("", brickName, extras);
    }

    public static void showListFragment(String prefix, String brickName, Bundle extras) {
        final BrickInfo listTemplate = ListCore.getRegisteredBrick(brickName);
        assert listTemplate != null;
        Fragment fragment = listTemplate.createFragment();
        fragment.setArguments(extras);
        App.getInstance().screensController.
                addTab(listTemplate.getTitle(), prefix + brickName, fragment);
    }

    @Override
    public void addTab(@Nullable String url, @Nullable Fragment fragment) {
        addTab("ForPDA", url, fragment);
    }

    @Override
    public void addTab(@Nullable String title, @Nullable String url, @Nullable Fragment fragment) {
        String tag = tabPrefix + App.getInstance().getTabIterator();


        addTabToList(title, url, tag, fragment);
    }
    private Boolean m_ExitWarned = false;

    public void appExit() {
        TabsRepository.getInstance().setCurrentFragmentTag(null);
        TabsRepository.getInstance().clear();
        App.getInstance().clearTabIterator();
        App.getInstance().exit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TabsRepository.getInstance().getCurrentFragmentTag());
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
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TabsRepository.getInstance().getCurrentFragmentTag());
            if (currentFragment == null || !((IBrickFragment) currentFragment).onBackPressed()) {
                if (TabsRepository.getInstance().size() <= 1) {
                    if (!m_ExitWarned) {
                        Toast.makeText(this, R.string.close_program_toasr, Toast.LENGTH_SHORT).show();
                        m_ExitWarned = true;
                        new Handler().postDelayed(() -> m_ExitWarned = false, 3 * 1000);
                    } else {
                        appExit();
                    }
                } else {
                    tryRemoveTab(TabsRepository.getInstance().getCurrentFragmentTag(), true);
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
                addTab(brickInfo.getTitle(), brickInfo.getName(), brickInfo.createFragment());
                return true;
            case R.id.mentions_item:
                addTab("Упоминания", "https://4pda.ru/forum/index.php?act=mentions",
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
                        Uri.parse("https://4pda.ru/forum/index.php?act=Reg&CODE=00"));
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

    @Override
    public void onSupportActionModeStarted(@NonNull androidx.appcompat.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof GeneralFragment) {
            ((GeneralFragment) currentFragment).onSupportActionModeStarted(mode);
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof GeneralFragment) {
            ((GeneralFragment) currentFragment).onActionModeStarted(mode);
        }
    }

    private Fragment getCurrentFragment() {
        String currentFragmentTag = TabsRepository.getInstance().getCurrentFragmentTag();
        if (currentFragmentTag != null && !"null".equals(currentFragmentTag)) {
            return getSupportFragmentManager().findFragmentByTag(TabsRepository.getInstance().getCurrentFragmentTag());
        }
        return null;
    }

    private void refreshUserMenu(Menu menu) {
        boolean logged = UserInfoRepository.Companion.getInstance().getLogined();
        menu.findItem(R.id.guest_item).setVisible(!logged);
        MenuItem userMenuItem = menu.findItem(R.id.user_item);
        userMenuItem.setVisible(logged);
        if (logged) {
            userMenuItem.setTitle(UserInfoRepository.Companion.getInstance().getName());
            userMenuItem.setIcon(getUserIconRes());
            String qmsTitle = Client.getInstance().getQmsCount() > 0 ? ("QMS (" + Client.getInstance().getQmsCount() + ")") : "QMS";
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
            App.getInstance().screensController.addTab(title, searchSettings.getSearchQuery(), SearchTopicsFragment.newFragment(searchSettings.getSearchQuery()));
        else
            App.getInstance().screensController.addTab(title, searchSettings.getSearchQuery(), SearchPostFragment.newFragment(searchSettings.getSearchQuery()));


    }

    @SuppressWarnings("ResourceType")
    private void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }


}