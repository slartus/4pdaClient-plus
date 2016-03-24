package org.softeg.slartus.forpdaplus;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.DownloadFragment;
import org.softeg.slartus.forpdaplus.fragments.ForumRulesFragment;
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

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends AppCompatActivity implements BricksListDialogFragment.IBricksListDialogCaller,
        MainDrawerMenu.SelectItemListener, TabDrawerMenu.SelectItemListener {
// test commit to beta
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
    public Toolbar toolbar;
    boolean top;
    int lastTheme;
    private static TabItem tabOnIntent = null;
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
            appBarLayout.getLocationOnScreen(ints);
            statusBarHeight = ints[1];

            if(statusBar!=null)
                statusBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, statusBarHeight));

            if(getPreferences().getBoolean("statusbarFake", false)&Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT){
                if(fakeStatusBar!=null){
                    fakeStatusBar.setVisibility(View.VISIBLE);
                    fakeStatusBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, statusBarHeight));
                }
            }
        }
    };


    public static SearchSettings searchSettings;

    private static List<String> users = new ArrayList<>();
    private static List<String> blockedUsers = new ArrayList<>();

    private static final int MSG_RECREATE = 1337;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("kek", "HANDLE MESSAGE LEAK MEMORY| ALARMA TUT UTECHKA!!!!");
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
        log("hack chnge to true");
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        setTheme(App.getInstance().getThemeStyleResID());
        super.onCreate(saveInstance);


        loadPreferences(App.getInstance().getPreferences());
        if(shortUserInfo!=null)
            shortUserInfo.mActivity = this;
        if(saveInstance!=null) {
            App.getInstance().setTabIterator(saveInstance.getInt("tabIterator"));
            App.getInstance().setCurrentFragmentTag(saveInstance.getString("currentTag"));
        }

        final List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        if (fragmentList != null&App.getInstance().getTabItems().size()==0) {
            GeneralFragment frag;
            TabItem item;
            for (Fragment fragment : fragmentList) {
                frag=(GeneralFragment)fragment;
                if(frag==null) continue;

                item = new TabItem(frag.getGeneralTitle(), frag.getGeneralUrl(), frag.getTag(), frag.getGeneralParentTag(), frag);
                frag.setThisTab(item);
                App.getInstance().getTabItems().add(item);
                Log.e("kek", "RESTORE TAB " + frag + " : " + frag.getThisTab());
            }
        }
        try {
            if (checkIntent()&saveInstance!=null) return;
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
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            appBarLayout = (AppBarLayout) findViewById(R.id.appbarlayout);
            toolbarShadow = findViewById(R.id.toolbar_shadow);
            if(Build.VERSION.SDK_INT>20) {
                toolbarShadow.setVisibility(View.GONE);
                toolbar.setElevation(6);
                appBarLayout.setElevation(6);
            }

            setSupportActionBar(toolbar);
            if(App.getInstance().getPreferences().getBoolean("titleMarquee", false)){
                Field field = Toolbar.class.getDeclaredField("mTitleTextView");
                field.setAccessible(true);
                Object value = field.get(toolbar);
                if(value!=null){
                    TextView textView = (TextView)value;
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
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24dp);
            }
            statusBar = (RelativeLayout) findViewById(R.id.status_bar);
            fakeStatusBar = (RelativeLayout) findViewById(R.id.fakeSB);

            switch (App.getInstance().getThemeType()){
                case App.THEME_TYPE_LIGHT:
                    statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_light));
                    break;
                case App.THEME_TYPE_DARK:
                    statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_dark));
                    break;
                default:
                    statusBar.setBackgroundColor(getResources().getColor(R.color.statusBar_black));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d("kekosa", "call post");
                getWindow().getDecorView().post(setStatusBarHeight);
            }

            NavigationView leftDrawer = (NavigationView) findViewById(R.id.left_drawer);
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

            searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings();

            NotifiersManager notifiersManager = new NotifiersManager(this);
            new DonateNotifier(notifiersManager).start(this);
            //new TopicAttentionNotifier(notifiersManager).start(this);
            new ForPdaVersionNotifier(notifiersManager, 1).start(this, false, true);
            activityPaused = false;
            if(App.getInstance().getCurrentFragmentTag()!=null)
                if(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag())!=null)
                    selectTab(App.getInstance().getTabByTag(App.getInstance().getCurrentFragmentTag()));

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_WRITE_STORAGE);

            users.add("2527270");//Dj_GraFlY
            users.add("4415042");//alex_shadow2014
            users.add("2848060");//pirog-
            users.add("691140");//74dimon
            users.add("959551");//den26448
            users.add("1882226");//Berkut_Angarsk
            users.add("1111194");//ангел мститель
            users.add("1122011");//Matuhan
            users.add("2760915");//DumF0rGaming
            users.add("104142");//maxxwell
            users.add("2696673");//another side
            users.add("4324432");//Snow Volf
            users.add("2586315");//l1r_svg
            users.add("1750050");//pavelpc
            users.add("96664");//Морфий
            users.add("2556269");//Radiation15
            users.add("1726458");//iSanechek
            users.add("236113");//slartus
            blockedUsers.add("Radiation15");

        } catch (Throwable ex) {
            AppLog.e(getApplicationContext(), ex);
        }
    }
    public static void checkToster(Context context){
        if(true) return;
        boolean toster = false;
        if(Client.getInstance().UserId.equals("0")) {
            LoginDialog.showDialog(context, null);
            return;
        }
        for(String user:users)
            if(user.equals(Client.getInstance().UserId))
                toster = true;
        if(!toster) android.os.Process.killProcess(android.os.Process.myPid());
    }
    public static void checkUsers(Context context){
        if (true) return;
        boolean toster = false;
        Log.e("kek", "id = " +Client.getInstance().UserId);
        for(String user:blockedUsers)
            if(user.equals(Client.getInstance().getUser()))
                toster = true;
        if(toster){
            String[] mes = new String[]{"Не в этот раз",
                    "Не сегодня", "Как нибудь в следующий раз",
                    "Нет",
                    "У меня голова болит, давай не сегодня",
                    "Ты кто такой? -Давай досвидания!"};
            Toast.makeText(context, mes[(int)(Math.random()*mes.length)], Toast.LENGTH_LONG).show();
            App.getInstance().exit();
        }
    }
    public void hidePopupWindows(){
        ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
        if(fragment!=null)
            ((GeneralFragment)fragment).hidePopupWindows();
    }
    public View getToolbarShadow() {
        return toolbarShadow;
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public void setArrow(final boolean b, final View.OnClickListener listener){
        if(mMainDrawerMenu==null) return;
        mMainDrawerMenu.getDrawerToggle().setDrawerIndicatorEnabled(!b);
        mMainDrawerMenu.getDrawerToggle().setToolbarNavigationClickListener(listener);
    }
    private boolean lastHamburgerArrow = true;
    private ValueAnimator anim;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator();

    private View.OnClickListener toggleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mMainDrawerMenu!=null)
                mMainDrawerMenu.toggleOpenState();
        }
    };
    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {}

        @Override
        public void onDrawerOpened(View drawerView) {
            hidePopupWindows();
        }

        @Override
        public void onDrawerClosed(View drawerView) {}

        @Override
        public void onDrawerStateChanged(int newState) {}
    };
    public void animateHamburger(final boolean isArrow, final View.OnClickListener listener){
        if(toolbar==null) return;
        if(isArrow){
            toolbar.setNavigationOnClickListener(toggleListener);
            getmMainDrawerMenu().getDrawerLayout().setDrawerListener(getmMainDrawerMenu().getDrawerToggle());
        }else{
            if(listener!=null) toolbar.setNavigationOnClickListener(listener);
            getmMainDrawerMenu().getDrawerLayout().setDrawerListener(drawerListener);
        }
        if(isArrow==lastHamburgerArrow) return;

        anim = ValueAnimator.ofFloat(isArrow ? 1.0f : 0.0f, isArrow ? 0.0f : 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                getmMainDrawerMenu().getDrawerToggle().onDrawerSlide(getmMainDrawerMenu().getDrawerLayout(), (Float) valueAnimator.getAnimatedValue());
            }
        });
        anim.setInterpolator(interpolator);
        anim.setDuration(250);
        anim.start();
        lastHamburgerArrow = isArrow;
    }
    private ShortUserInfo shortUserInfo;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mMainDrawerMenu != null)
            mMainDrawerMenu.close();

        if(!top)
            shortUserInfo = new ShortUserInfo(this, mMainDrawerMenu.getNavigationView().getHeaderView(0));
        else
            mMainDrawerMenu.getNavigationView().getHeaderView(0).setVisibility(View.GONE);

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
        checkToster(this);
        checkUsers(this);
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
        log("intent: " + intent);
        /*if (IntentActivity.checkSendAction(this, intent))
            return false;*/
        if(intent.getAction().equals(Intent.ACTION_SEND)|intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
            Toast.makeText(getContext(), "Данное действие временно не поддерживается", Toast.LENGTH_SHORT).show();
            return false;
        }
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
        Fragment fragment = listTemplate.createFragment();
        selectFragment(listTemplate.getTitle(), "", listTemplate.getName(), fragment);
        addTabToList(listTemplate.getTitle(), listTemplate.getName(), listTemplate.getName(), fragment, false);
    }

    public void selectTab(TabItem tabItem) {
        selectFragment(tabItem.getTitle(), tabItem.getUrl(), tabItem.getTag(), tabItem.getFragment());
    }

    private void selectFragment(final String title, final String url, final String tag, final Fragment fragment){
        Log.e("kek", "selectfragment start");
        if(mTabDraweMenu!=null){
            mTabDraweMenu.close();
            notifyTabAdapter();
        }
        if(mMainDrawerMenu!=null){
            mMainDrawerMenu.close();
            mMainDrawerMenu.setItemCheckable(title);
        }

        String currentFragmentTag = String.valueOf(App.getInstance().getCurrentFragmentTag());

        endActionFragment(title, tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, true);
        if (tag.equals(currentFragmentTag)) {
            if(getSupportFragmentManager().findFragmentByTag(currentFragmentTag) == null) {
                addFragment(transaction, fragment, tag);
            }else {
                showFragment(transaction, tag);
            }
        }else{
            if (currentFragmentTag.equals("null")) {
                addFragment(transaction, fragment, tag);
            }else {
                if(getSupportFragmentManager().findFragmentByTag(tag)==null){
                    addFragment(transaction, fragment, tag);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
                }else {
                    showFragment(transaction, tag);
                    if(Preferences.Lists.isRefreshOnTab())
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((IBrickFragment)getSupportFragmentManager().findFragmentByTag(tag)).loadData(true);
                            }
                        }, 300);
                }
            }
        }
        transaction.commit();
    }

    public void hideFragments(FragmentTransaction transaction, boolean withAnimation){
        if(getSupportFragmentManager().getFragments()==null) return;
        if(withAnimation)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        for (Fragment fr:getSupportFragmentManager().getFragments()) {
            if (fr != null) {
                if(fr.isVisible())
                    fr.onPause();
                transaction.hide(fr);
            }
        }
    }

    private void showFragment(FragmentTransaction transaction, String tag){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        fragment.onResume();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
        Log.e("kek", "showfragment by tag end");
    }
    public void showFragment(String tag, boolean onresume){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment==null){
            if(App.getInstance().getTabByTag(tag)!=null){
                TabItem tabItem = App.getInstance().getTabByTag(tag);
                addTab(tabItem.getTitle(), tabItem.getUrl(), tabItem.getFragment());
                return;
            }
            transaction.commit();
            return;
        }
        if(onresume) fragment.onResume();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).show(fragment);
        transaction.commit();
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
    public void endActionFragment(String title){
        if (getSupportActionBar() != null) {
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);// новости выставляют выпадающий список
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            //getSupportActionBar().setSubtitle(null);
        }
        setTitle(title);
    }

    public static void log(String s) {
        Log.e("My log", s + "       ///////// INFO CURRENT TAG: " + App.getInstance().getCurrentFragmentTag());
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        outState.putInt("tabIterator", App.getInstance().getTabIterator());
        outState.putString("currentTag", App.getInstance().getCurrentFragmentTag());
        super.onSaveInstanceState(outState);
        if(hack){
            onStop();
            onStart();
        }
        hack= false;
        log("onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle outState) {
        super.onRestoreInstanceState(outState);
        log("onRestoreInstanceState");
    }

    public static SharedPreferences getPreferences() {
        return App.getInstance().getPreferences();
    }

    public void notifyTabAdapter(){
        mTabDraweMenu.notifyDataSetChanged();
    }
    /**
     * Управление вкладками начало
     */
    @Override
    protected void onResumeFragments() {
        //super.onResumeFragments();
        log("onResumeFragments");
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
        log("onResume " + System.currentTimeMillis() / 1000);
        onStart();
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
        if(!(String.valueOf(App.getInstance().getCurrentFragmentTag())).equals("null")){
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
            if(fragment!=null)
                fragment.onResume();
        }
        int[] ints = new int[2];
        appBarLayout.getLocationOnScreen(ints);
        if(statusBarHeight!=ints[1])
            setStatusBarHeight.run();
        log("onPostResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
        if(!(String.valueOf(App.getInstance().getCurrentFragmentTag())).equals("null")){
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(App.getInstance().getCurrentFragmentTag());
            if(fragment!=null)
            fragment.onPause();
        }

        log("onPause " + System.currentTimeMillis() / 1000);
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
        App.getInstance().setCurrentFragmentTag(tabPrefix + (App.getInstance().getTabIterator() - 1));
    }

    public static void addTabToList(String name, String url, String tag, Fragment fragment, boolean select){
        log("addtabtolist select: "+select);
        log("start addtabtolist");
        TabItem item = null;
        if(App.getInstance().isContainsByUrl(url)){
            if(select) item = App.getInstance().getTabByUrl(url);
            log("addtabtolist 1");

        }else if(!App.getInstance().isContainsByTag(tag)) {
            item = new TabItem(name, url, tag, App.getInstance().getCurrentFragmentTag(), fragment);
            ((GeneralFragment)fragment).setThisTab(item);
            Log.e("kek", "add item "+item);
            App.getInstance().getTabItems().add(item);
            App.getInstance().plusTabIterator();

            mTabDraweMenu.refreshAdapter();
            log("addtabtolist 2");
        }else {
            if(select) item = App.getInstance().getTabByTag(tag);
            log("addtabtolist 3");
        }
        log("addtabtolist 4");

        if(select) mTabDraweMenu.selectTab(item);
    }

    public static void selectTabByTag(String tag){
        log("selectTabByTag");
        mTabDraweMenu.selectTab(App.getInstance().getTabByTag(tag));;
    }

    public void tryRemoveTab(String tag){
        log("tryRemoveTab");
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment!=null)
            if(!((GeneralFragment)fragment).closeTab())
                removeTab(tag);
    }
    public void removeTab(String tag){
        TabItem tab = App.getInstance().getTabByTag(tag);
        log("remove tab" + (tab != null ? tab.getTitle() : "tab ne sushestvuet((("));
        log("remove " + tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, false);
        transaction.remove(getSupportFragmentManager().findFragmentByTag(tag));
        transaction.commit();
        mTabDraweMenu.removeTab(tag);
    }
    public void removeTabs(List<TabItem> items){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction, false);
        for(TabItem item:items) {
            transaction.remove(item.getFragment());
            App.getInstance().getTabItems().remove(item);
        }
        transaction.commit();
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
        if(menu!=null)
            menu.clear();
        else
            menu = new MenuBuilder(this);

        createUserMenu(menu);
        if(getPreferences().getBoolean("openTabDrawerButton", false)){
            menu.add("Вкладки")
                    .setIcon(R.drawable.ic_checkbox_multiple_blank_outline_white_24dp)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            mTabDraweMenu.toggleOpenState();
                            return true;
                        }
                    })
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        menu.add(R.string.Search)
                .setIcon(R.drawable.ic_magnify_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        SearchSettingsDialogFragment.showSearchSettingsDialog(MainActivity.this, searchSettings);
                        return true;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, 0, 997, "Правила форума").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                ForumRulesFragment.showRules();
                return true;
            }
        });
        menu.add(0, 0, 998, "Помощь (FAQ)").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                IntentActivity.showTopic("http://4pda.ru/forum/index.php?s=&showtopic=271502&view=findpost&p=45570566");
                return true;
            }
        });
        boolean showed = getPreferences().getBoolean("showedExitButton", false);
        if(getPreferences().getBoolean("showExitButton",false)) {
            if(!showed) {
                Answers.getInstance().logCustom(new CustomEvent("Button Exit Enable"));
                getPreferences().edit().putBoolean("showedExitButton", true).apply();
            }

                menu.add(0, 0, 999, R.string.CloseApp)
                    .setIcon(R.drawable.ic_close_white_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(MenuItem item) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                                return true;
                        }
                    });
//            if (!Surprise.isBlocked()) {
//            }
        }

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

    @SuppressWarnings("ResourceType")
    protected void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }
}