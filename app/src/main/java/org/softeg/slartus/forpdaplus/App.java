package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdanotifyservice.MainService;
import org.softeg.slartus.forpdanotifyservice.favorites.FavoritesNotifier;
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: slinkin
 * Date: 05.08.11
 * Time: 8:03
 */
@ReportsCrashes(
        mailTo = "ololosh10050@gmail.com,slartus@gmail.com",
        mode = ReportingInteractionMode.DIALOG,
        customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT},
        resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
        resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.)
)
public class App extends android.app.Application {
    public static final int THEME_WHITE = 0;
    public static final int THEME_BLACK = 1;

    public static final int THEME_WHITE_HD = 13;
    public static final int THEME_BLACK_HD = 14;

    public static final int THEME_WHITE_MATERIAL_CYAN = 15;
    public static final int THEME_WHITE_MATERIAL_LB = 16;
    public static final int THEME_WHITE_MATERIAL_GRAY= 17;
    public static final int THEME_BLACK_MATERIAL_DARK = 18;

    public static final int THEME_WHITE_REMIE = 2;


    public static final int THEME_WHITE_TRABLONE = 4;
    public static final int THEME_WHITER_REMIE = 5;
    public static final int THEME_WHITE_VETALORLOV = 7;

    public static final int THEME_WHITE_OLD = 12;
    public static final int THEME_CUSTOM_CSS = 99;

    private final Integer[] WHITE_THEMES = {THEME_WHITE_TRABLONE, THEME_WHITE_VETALORLOV, THEME_WHITE_REMIE,
            THEME_WHITER_REMIE, THEME_WHITE, THEME_WHITE_OLD, THEME_WHITE_HD, THEME_WHITE_MATERIAL_CYAN, THEME_WHITE_MATERIAL_LB, THEME_WHITE_MATERIAL_GRAY};

    private static boolean m_IsDebugModeLoaded = false;
    private static boolean m_IsDebugMode = false;

    public static boolean getIsDebugMode() {

        if (!m_IsDebugModeLoaded) {
            m_IsDebugMode = PreferenceManager
                    .getDefaultSharedPreferences(INSTANCE).getBoolean("DebugMode", false);
            m_IsDebugModeLoaded = true;
        }
        return m_IsDebugMode;
    }

    public static void showMainActivityWithoutBack(Activity activity) {
//        if(activity.getIntent()!=null
//                &&activity.getIntent().getExtras()!=null
//                &&activity.getIntent().getExtras().containsKey(BaseFragmentActivity.SENDER_ACTIVITY)){
//            if(MainActivity.class.toString().equals(activity.getIntent().getExtras().getString(BaseFragmentActivity.SENDER_ACTIVITY))){
//                activity.onBackPressed();
//                return;
//            }
//        }
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private AtomicInteger m_AtomicInteger=new AtomicInteger();
    public int getUniqueIntValue(){
        return m_AtomicInteger.incrementAndGet();
    }

    public int getWebViewFont() {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("webViewFont",0);
    }
    public int getColorAccent(String type) {
        int color = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch(type) {
            case "Accent":
                color = prefs.getInt("accentColor", Color.rgb(233, 30, 99));
                break;
            case "Pressed":
                color = prefs.getInt("accentColorPressed", Color.rgb(233, 30, 99));
                break;
        }
        return color;
    }
    public int getMainAccentColor() {
        int color = R.color.accentPink;
        switch (PreferenceManager.getDefaultSharedPreferences(this).getString("mainAccentColor", "pink")) {
            case "pink":
                color  = R.color.accentPink;
                break;
            case "blue":
                color = R.color.accentBlue;
                break;
            case "gray":
                color = R.color.accentGray;
                break;
        }
        return color;
    }
    public int getThemeStyleResID() {
        int theme = R.style.Theme_White;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String color = prefs.getString("mainAccentColor","pink");
        if (isWhiteTheme()){
            switch (color) {
                case "pink":
                    theme = R.style.MainPinkWH;
                    break;
                case "blue":
                    theme = R.style.MainBlueWH;
                    break;
                case "gray":
                    theme = R.style.MainGrayWH;
                    break;
            }
        }else{
            switch (color) {
                case "pink":
                    theme = R.style.MainPinkBL;
                    break;
                case "blue":
                    theme = R.style.MainBlueBL;
                    break;
                case "gray":
                    theme = R.style.MainGrayBL;
                    break;
            }
        }
        return theme;
    }
    public int getPrefsThemeStyleResID() {
        int theme = R.style.Theme_Prefs_WhitePink;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String color = prefs.getString("mainAccentColor","pink");
        if (isWhiteTheme()){
            switch (color) {
                case "pink":
                    theme = R.style.Theme_Prefs_WhitePink;
                    break;
                case "blue":
                    theme = R.style.Theme_Prefs_WhiteBlue;
                    break;
                case "gray":
                    theme = R.style.Theme_Prefs_WhiteGray;
                    break;
            }
        }else{
            switch (color) {
                case "pink":
                    theme = R.style.Theme_Prefs_BlackPink;
                    break;
                case "blue":
                    theme = R.style.Theme_Prefs_BlackBlue;
                    break;
                case "gray":
                    theme = R.style.Theme_Prefs_BlackGray;
                    break;
            }
        }
        return theme;
    }

    public int getTransluentThemeStyleResID() {
        int theme = R.style.Theme_Transluent_WhitePink;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String color = prefs.getString("mainAccentColor","pink");
        if (isWhiteTheme()){
            switch (color) {
                case "pink":
                    theme = R.style.Theme_Transluent_WhitePink;
                    break;
                case "blue":
                    theme = R.style.Theme_Transluent_WhiteBlue;
                    break;
                case "gray":
                    theme = R.style.Theme_Transluent_WhiteGray;
                    break;
            }
        }else{
            switch (color) {
                case "pink":
                    theme = R.style.Theme_Transluent_BlackPink;
                    break;
                case "blue":
                    theme = R.style.Theme_Transluent_BlackBlue;
                    break;
                case "gray":
                    theme = R.style.Theme_Transluent_BlackGray;
                    break;
            }
        }
        return theme;
    }

    public int getThemeBackgroundColorRes() {
        return isWhiteTheme() ? R.color.app_background_wh : R.color.app_background_bl;
    }

    public boolean isWhiteTheme() {
        String themeStr = getCurrentTheme();
        int theme = themeStr.length() < 3 ? Integer.parseInt(themeStr) : -1;

        return ArrayUtils.indexOf(theme, WHITE_THEMES) != -1 || themeStr.contains("/white/");
    }

    public int getThemeStyleWebViewBackground() {
        return isWhiteTheme() ? Color.parseColor("#eeeeee") : Color.parseColor("#212121");
    }

    public String getCurrentTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString("appstyle", Integer.toString(THEME_WHITE));
    }

    public String getCurrentThemeName() {
        return isWhiteTheme() ? "white" : "black";
    }

    public String getCurrentBackgroundColorHtml() {
        return isWhiteTheme() ? "#eeeeee" : "#212121";
    }

    private String checkThemeFile(String themePath) {
        try {
            if (!new File(themePath).exists()) {
                // Toast.makeText(INSTANCE,"не найден файл темы: "+themePath,Toast.LENGTH_LONG).show();
                return defaultCssTheme();
            }
            return themePath;
        } catch (Throwable ex) {
            return defaultCssTheme();
        }
    }

    private String defaultCssTheme() {
        return "/android_asset/forum/css/coba_white_blue.css";
    }

    public String getThemeCssFileName() {
        String themeStr = App.getInstance().getCurrentTheme();
        return getThemeCssFileName(themeStr);
    }

    public String getThemeCssFileName(String themeStr) {
        if (themeStr.length() > 3)
            return checkThemeFile(themeStr);

        String path = "/android_asset/forum/css/";
        String cssFile = "coba_white_blue.css";
        int theme = Integer.parseInt(themeStr);
        if (theme == -1)
            return themeStr;
        String color = PreferenceManager.getDefaultSharedPreferences(this).getString("mainAccentColor", "pink");
        switch (theme) {
            case THEME_WHITE:
                switch (color) {
                    case "pink":
                        cssFile = "coba_white_blue.css";
                        break;
                    case "blue":
                        cssFile = "coba_white_blue_blue.css";
                        break;
                    case "gray":
                        cssFile = "coba_white_blue_gray.css";
                        break;
                }
                break;
            /*case THEME_WHITE_OLD:
                cssFile = "coba_white_blue.css";
                break;*/
            case THEME_BLACK:
                switch (color) {
                    case "pink":
                        cssFile = "coba_dark_blue.css";
                        break;
                    case "blue":
                        cssFile = "coba_dark_blue_blue.css";
                        break;
                    case "gray":
                        cssFile = "coba_dark_blue_gray.css";
                        break;
                }
                break;
            case THEME_WHITE_MATERIAL_CYAN:
                cssFile = "material_cyan.css";
                break;
            case THEME_WHITE_MATERIAL_LB:
                cssFile = "material_light-blue.css";
                break;
            case THEME_WHITE_MATERIAL_GRAY:
                cssFile = "material_gray.css";
                break;
            case THEME_BLACK_MATERIAL_DARK:
                cssFile = "material_dark.css";
                break;

            /*case THEME_WHITE_HD:
                cssFile = "white_hd.css";
                break;
            case THEME_BLACK_HD:
                cssFile = "black_hd.css";
                break;*/
            case THEME_CUSTOM_CSS:
                return "/mnt/sdcard/style.css";
        }
        return path + cssFile;
    }

    private static App INSTANCE = null;

    public App() {
        INSTANCE = this;


    }

    private MyActivityLifecycleCallbacks m_MyActivityLifecycleCallbacks;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        initImageLoader(this);
        m_MyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(m_MyActivityLifecycleCallbacks);
        setTheme(getThemeStyleResID());

        try {
            DbHelper.prepareBases(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        m_MyActivityLifecycleCallbacks.finishActivities();
    }


    private static Boolean m_QmsStarted = false;
    private static Boolean m_FavoritesNotifierStarted = false;

    public static App getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new App();

        }
        if (!m_QmsStarted) {
            reStartQmsService();
        }
        if (!m_FavoritesNotifierStarted) {
            reStartFavoritesNotifierService();
        }
        return INSTANCE;
    }

    public static void resStartNotifierServices() {
        reStartQmsService();
        reStartFavoritesNotifierService();
    }

    private static void stopQmsService() {
        try {
            QmsNotifier.cancelAlarm(INSTANCE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void reStartQmsService() {
        stopQmsService();
        startQmsService();
    }

    private static void startQmsService() {
        m_QmsStarted = true;
        try {
            if (!QmsNotifier.isUse(getContext()))
                return;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());


            Intent intent = new Intent(INSTANCE, MainService.class);
            intent.putExtra("CookiesPath", PreferencesActivity.getCookieFilePath(INSTANCE));
            intent.putExtra(QmsNotifier.TIME_OUT_KEY, Math.max(ExtPreferences.parseFloat(sharedPreferences,
                    QmsNotifier.TIME_OUT_KEY, 5), 1));

            QmsNotifier.restartTask(INSTANCE, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.no_image)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .handler(new Handler())
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 2 Mb
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }

    private static void stopFavoritesNotifierService() {
        try {
            FavoritesNotifier.cancelAlarm(INSTANCE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void reStartFavoritesNotifierService() {
        stopFavoritesNotifierService();
        startFavoritesNotifierService();
    }

    private static void startFavoritesNotifierService() {
        m_FavoritesNotifierStarted = true;
        try {
            if (!FavoritesNotifier.isUse(getContext())) return;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            Intent intent = new Intent(INSTANCE, MainService.class);
            intent.putExtra("CookiesPath", PreferencesActivity.getCookieFilePath(INSTANCE));
            intent.putExtra(FavoritesNotifier.TIME_OUT_KEY, Math.max(ExtPreferences.parseFloat(sharedPreferences,
                    FavoritesNotifier.TIME_OUT_KEY, 5), 1));

            FavoritesNotifier.restartTask(INSTANCE, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return getInstance();
    }

    public boolean isDebuggable() {
        return (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static SwipeRefreshLayout createSwipeRefreshLayout(Activity activity, View view,
                                                                final Runnable refreshAction) {
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.ptr_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAction.run();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(App.getInstance().getMainAccentColor());
        return swipeRefreshLayout;
    }


    private static final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        private HashMap<String, Activity> m_Activities = new HashMap<>();

        public void onActivityCreated(Activity activity, Bundle bundle) {
            m_Activities.put(activity.getLocalClassName(), activity);
        }

        public void onActivityDestroyed(Activity activity) {
            if (m_Activities.containsKey(activity.getLocalClassName()))
                m_Activities.remove(activity.getLocalClassName());
        }

        public void onActivityPaused(Activity activity) {

        }

        public void onActivityResumed(Activity activity) {

        }

        public void onActivitySaveInstanceState(Activity activity,
                                                Bundle outState) {

        }

        public void onActivityStarted(Activity activity) {

        }

        public void onActivityStopped(Activity activity) {

        }

        public void finishActivities() {
            for (Map.Entry<String, Activity> entry : m_Activities.entrySet()) {
                try {
                    Activity activity = entry.getValue();

                    if (activity == null)
                        continue;

                    if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())
                        continue;

                    if (activity.isFinishing())
                        continue;

                    entry.getValue().finish();
                } catch (Throwable ex) {
                    Log.e("", "finishActivities:" + ex.toString());
                }
            }
        }
    }


}