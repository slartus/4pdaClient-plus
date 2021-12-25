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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.common.SearchSettingsMapperKt;
import org.softeg.slartus.forpdaplus.core.AppPreferences;
import org.softeg.slartus.forpdaplus.core.ListPreferences;
import org.softeg.slartus.forpdaplus.core.interfaces.SearchSettingsListener;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.fragments.UserInfoMenuFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchPostFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchTopicsFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.mainnotifiers.DonateNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends BaseActivity implements BricksListDialogFragment.IBricksListDialogCaller,
        MainDrawerMenu.SelectItemListener, TabDrawerMenu.SelectItemListener {

    @Inject
    AppPreferences appPreferences;
    @Inject
    ListPreferences listPreferences;


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

    private final TimberTree timberTree = new TimberTree(new WeakReference<>(this));

    @Override
    public void onCreate(Bundle saveInstance) {
        setTheme(AppTheme.getThemeStyleResID());
        super.onCreate(saveInstance);
        Timber.plant(timberTree);

        setRequestedOrientation(appPreferences.getScreenOrientation());

        if (shortUserInfo != null)
            shortUserInfo.setMActivity(new WeakReference<>(this));
        if (saveInstance != null) {
            TabsManager.getInstance().setTabIterator(saveInstance.getInt("tabIterator"));
            TabsManager.getInstance().setCurrentFragmentTag(saveInstance.getString("currentTag"));
        }

        restoreTabsByFragments();
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
            if (appPreferences.getTitleMarquee()) {
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

            mTabDrawerMenu = new TabDrawerMenu(this, this);
            mMainDrawerMenu = new MainDrawerMenu(this, this);

            searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings();

            NotifiersManager notifiersManager = new NotifiersManager();
            new DonateNotifier(notifiersManager).start(this);
            //new TopicAttentionNotifier(notifiersManager).start(this);
            new ForPdaVersionNotifier(notifiersManager, 1, false).start(this);
            activityPaused = false;
            if (TabsManager.getInstance().getCurrentFragmentTag() != null)
                if (TabsManager.getInstance().getTabByTag(TabsManager.getInstance().getCurrentFragmentTag()) != null) {
                    selectTab(TabsManager.getInstance().getTabByTag(TabsManager.getInstance().getCurrentFragmentTag()));
                }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_WRITE_STORAGE);
        } catch (Throwable ex) {
            AppLog.e(getApplicationContext(), ex);
        }
        addUserInfoFragment();
    }

    @Override
    protected void onDestroy() {
        Timber.uproot(timberTree);
        super.onDestroy();
    }

    private void addUserInfoFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment mFragment1 = fm.findFragmentByTag(UserInfoMenuFragment.TAG);
        if (mFragment1 == null) {
            mFragment1 = new UserInfoMenuFragment();
            ft.add(mFragment1, UserInfoMenuFragment.TAG);
        }
        ft.commit();
    }

    private void restoreTabsByFragments() {
        if (TabsManager.getInstance().getTabItems().size() == 0) {
            GeneralFragment frag;
            TabItem item;
            final List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragmentList) {
                try {
                    if (fragment instanceof GeneralFragment) {
                        frag = (GeneralFragment) fragment;
                        item = new TabItem(frag.getGeneralTitle(), frag.getGeneralUrl(), frag.getTag(), frag.getGeneralParentTag(), frag);
                        frag.setThisTab(item);
                        TabsManager.getInstance().getTabItems().add(item);
                    }
                } catch (ClassCastException ex) {
                    AppLog.e(ex);
                }
            }
        }
    }


    public void hidePopupWindows() {
        InputMethodManager service = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        assert service != null;
        View currentFocus = this.getCurrentFocus();
        if (currentFocus == null)
            currentFocus = new View(this);
        service.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TabsManager.getInstance().getCurrentFragmentTag());
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

        if (mTabDrawerMenu == null)
            mTabDrawerMenu = new TabDrawerMenu(this, this);
        mTabDrawerMenu.close();

        if (!top)
            shortUserInfo = new ShortUserInfo(this, mMainDrawerMenu.getNavigationView().getHeaderView(0));
        else
            mMainDrawerMenu.getNavigationView().getHeaderView(0).setVisibility(View.GONE);

        Client.INSTANCE.checkLoginByCookies();
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
        // intent.setData(Uri.parse("https://4pda.to/forum/index.php?showtopic=271502&st=42420#entry111243233"));
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
        if (mTabDrawerMenu != null) {
            mTabDrawerMenu.close();
            notifyTabAdapter();
        }
        if (mMainDrawerMenu != null) {
            mMainDrawerMenu.close();
            mMainDrawerMenu.setItemCheckable(title);
        }

        String currentFragmentTag = String.valueOf(TabsManager.getInstance().getCurrentFragmentTag());

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
                    if (listPreferences.getRefreshListOnTab())
                        handler.postDelayed(() -> ((IBrickFragment) getSupportFragmentManager().findFragmentByTag(tag)).loadData(true), 300);
                }
            }
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction, boolean withAnimation) {
        if (withAnimation)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        else
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        for (Fragment fr : getSupportFragmentManager().getFragments()) {
            if (fr != null && !(fr instanceof UserInfoMenuFragment)) {
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
            if (TabsManager.getInstance().getTabByTag(tag) != null) {
                TabItem tabItem = TabsManager.getInstance().getTabByTag(tag);
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

    private void endActionFragment(String title, String tag) {
        TabsManager.getInstance().setCurrentFragmentTag(tag);
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
        outState.putInt("tabIterator", TabsManager.getInstance().getTabIterator());
        outState.putString("currentTag", TabsManager.getInstance().getCurrentFragmentTag());
        super.onSaveInstanceState(outState);
        if (hack) {
            onStop();
            onStart();
        }
        hack = false;
    }


    public void notifyTabAdapter() {
        if (mTabDrawerMenu != null)
            if (TabDrawerMenu.adapter != null)
                mTabDrawerMenu.notifyDataSetChanged();
    }

    private String lang = null;

    @Override
    public void onResume() {
        super.onResume();
        if (lang == null) {
            lang = appPreferences.getLanguage();
        }
        if (!appPreferences.getLanguage().equals(lang)) {
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
        activityPaused = false;
        if (TabsManager.getInstance().getCurrentFragmentTag() == null) {
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

        if (!(String.valueOf(TabsManager.getInstance().getCurrentFragmentTag())).equals("null")) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TabsManager.getInstance().getCurrentFragmentTag());
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
        if (!(String.valueOf(TabsManager.getInstance().getCurrentFragmentTag())).equals("null")) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TabsManager.getInstance().getCurrentFragmentTag());
            if (fragment != null) {
                fragment.onPause();
            }
        }
    }


    public void tryRemoveTab(String tag) {
        tryRemoveTab(tag, false);
    }

    public void tryRemoveTab(String tag, boolean tryClose) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && TabsManager.getInstance().getTabByTag(tag) != null)
            if (tryClose) {
                if (!((GeneralFragment) fragment).closeTab())
                    removeTab(tag);
            } else {
                removeTab(tag);
            }
    }

    private void removeTab(String tag) {
        if (activityPaused | mTabDrawerMenu == null) {
            tabTagForRemove = tag;
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideFragments(transaction, false);
            transaction.remove(getSupportFragmentManager().findFragmentByTag(tag));
            transaction.commitAllowingStateLoss();
            mTabDrawerMenu.removeTab(tag);
        }
    }

    public void removeTabs(List<TabItem> items) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, false);
        for (TabItem item : items) {
            transaction.remove(item.getFragment());
            TabsManager.getInstance().getTabItems().remove(item);
        }
        transaction.commitAllowingStateLoss();
    }


    private Boolean m_ExitWarned = false;

    public void appExit() {
        TabsManager.getInstance().setCurrentFragmentTag(null);
        TabsManager.getInstance().getTabItems().clear();
        TabsManager.getInstance().clearTabIterator();
        App.getInstance().exit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TabsManager.getInstance().getCurrentFragmentTag());
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
            if (mTabDrawerMenu.isOpen()) {
                mTabDrawerMenu.close();
                return;
            }
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TabsManager.getInstance().getCurrentFragmentTag());
            if (currentFragment == null || !((IBrickFragment) currentFragment).onBackPressed()) {
                if (TabsManager.getInstance().getTabItems().size() <= 1) {
                    if (!m_ExitWarned) {
                        Toast.makeText(this, R.string.close_program_toasr, Toast.LENGTH_SHORT).show();
                        m_ExitWarned = true;
                        new Handler().postDelayed(() -> m_ExitWarned = false, 3 * 1000);
                    } else {
                        appExit();
                    }
                } else {
                    tryRemoveTab(TabsManager.getInstance().getCurrentFragmentTag(), true);
                }

            } else {
                m_ExitWarned = false;
            }
        } catch (Throwable ignored) {
            appExit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.tabs_item:
                mTabDrawerMenu.toggleOpenState();
                return true;
            case R.id.search_item:
                SearchSettingsDialogFragment.showSearchSettingsDialog(MainActivity.this, getSearchSettings());
                return true;
            case R.id.exit_item:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);


        menu.findItem(R.id.tabs_item).setVisible(getPreferences().getBoolean("openTabDrawerButton", false));
        menu.findItem(R.id.exit_item).setVisible(getPreferences().getBoolean("showExitButton", false));
        return super.onPrepareOptionsMenu(menu);
    }

    private SearchSettings getSearchSettings() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof SearchSettingsListener) {
            org.softeg.slartus.forpdaplus.core.entities.SearchSettings searchSettings = ((SearchSettingsListener) currentFragment).getSearchSettings();
            if (searchSettings != null) {
                return SearchSettingsMapperKt.map(searchSettings);
            }
        }
        return MainActivity.searchSettings;
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
        String currentFragmentTag = TabsManager.getInstance().getCurrentFragmentTag();
        if (currentFragmentTag != null && !"null".equals(currentFragmentTag)) {
            return getSupportFragmentManager().findFragmentByTag(TabsManager.getInstance().getCurrentFragmentTag());
        }
        return null;
    }

    public static final int REQUEST_WRITE_STORAGE = 112;
    private static TabDrawerMenu mTabDrawerMenu;
    private static TabItem tabOnIntent = null;
    private static String tabTagForRemove = null;
    private static boolean activityPaused = false;
    public static SearchSettings searchSettings;
    private static final int MSG_RECREATE = 1337;

    public static SharedPreferences getPreferences() {
        return App.getInstance().getPreferences();
    }

    public static void addTab(String url, Fragment fragment) {
        addTab("ForPDA", url, fragment);
    }

    public static void addTab(String title, String url, Fragment fragment) {
        if (activityPaused | mTabDrawerMenu == null) {
            tabOnIntent = new TabItem(title, url, tabPrefix + TabsManager.getInstance().getTabIterator(), TabsManager.getInstance().getCurrentFragmentTag(), fragment);
        } else {
            addTabToList(title, url, tabPrefix + TabsManager.getInstance().getTabIterator(), fragment, true);
        }
        if (!TabsManager.getInstance().isContainsByUrl(url)) {
            String newTag = tabPrefix + (TabsManager.getInstance().getTabIterator() - 1);
            TabsManager.getInstance().setCurrentFragmentTag(newTag);
        }
    }

    private static void addTabToList(String name, String url, String tag, Fragment fragment, boolean select) {
        TabItem item = null;
        if (TabsManager.getInstance().isContainsByUrl(url)) {
            if (select) item = TabsManager.getInstance().getTabByUrl(url);
        } else if (!TabsManager.getInstance().isContainsByTag(tag)) {
            item = new TabItem(name, url, tag, TabsManager.getInstance().getCurrentFragmentTag(), fragment);
            ((GeneralFragment) fragment).setThisTab(item);
            TabsManager.getInstance().getTabItems().add(item);
            TabsManager.getInstance().plusTabIterator();
            mTabDrawerMenu.refreshAdapter();
        } else {
            if (select) item = TabsManager.getInstance().getTabByTag(tag);
        }

        if (select) mTabDrawerMenu.selectTab(item);
    }

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
}