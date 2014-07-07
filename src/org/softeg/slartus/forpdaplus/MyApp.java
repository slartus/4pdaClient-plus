package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
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

/**
 * User: slinkin
 * Date: 05.08.11
 * Time: 8:03
 */
public class MyApp extends android.app.Application {
    public static final int THEME_WHITE = 0;
    public static final int THEME_BLACK = 1;

    public static final int THEME_WHITE_HD = 13;
    public static final int THEME_BLACK_HD = 14;


    public static final int THEME_WHITE_REMIE = 2;


    public static final int THEME_WHITE_TRABLONE = 4;
    public static final int THEME_WHITER_REMIE = 5;
    public static final int THEME_WHITE_VETALORLOV = 7;

    public static final int THEME_WHITE_OLD = 12;
    public static final int THEME_CUSTOM_CSS = 99;

    private final Integer[] WHITE_THEMES = {THEME_WHITE_TRABLONE, THEME_WHITE_VETALORLOV, THEME_WHITE_REMIE,
            THEME_WHITER_REMIE, THEME_WHITE, THEME_WHITE_OLD, THEME_WHITE_HD};

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
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public int getThemeStyleResID() {
        return isWhiteTheme() ? R.style.Theme_White : R.style.Theme_Black;
    }

    public int getTransluentThemeStyleResID() {
        return isWhiteTheme() ? R.style.Theme_Transluent_White : R.style.Theme_Transluent_Black;
    }

    public int getThemeBackgroundColorRes() {
        return isWhiteTheme() ? R.color.pda__background_light : R.color.pda__background_dark;
    }

    public boolean isWhiteTheme() {
        String themeStr = getCurrentTheme();
        int theme = themeStr.length() < 3 ? Integer.parseInt(themeStr) : -1;

        return ArrayUtils.indexOf(theme, WHITE_THEMES) != -1 || themeStr.contains("/white/");
    }

    public int getThemeStyleWebViewBackground() {
        return isWhiteTheme() ? getResources().getColor(R.color.white_theme_webview_background) : Color.BLACK;
    }

    public String getCurrentTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString("appstyle", Integer.toString(THEME_WHITE_OLD));
    }

    public String getCurrentThemeName() {
        return isWhiteTheme() ? "white" : "black";
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
        return "/android_asset/forum/css/white.css";
    }

    public String getThemeCssFileName() {
        String themeStr = MyApp.getInstance().getCurrentTheme();
        return getThemeCssFileName(themeStr);
    }

    public String getThemeCssFileName(String themeStr) {
        if (themeStr.length() > 3)
            return checkThemeFile(themeStr);

        String path = "/android_asset/forum/css/";
        String cssFile = "white.css";
        int theme = Integer.parseInt(themeStr);
        if (theme == -1)
            return themeStr;
        switch (theme) {
            case THEME_WHITE:
                cssFile = "white.css";
                break;
            case THEME_BLACK:
                cssFile = "black.css";
                break;
            case THEME_WHITE_OLD:
                cssFile = "white_old.css";
                break;

            case THEME_WHITE_HD:
                cssFile = "white_hd.css";
                break;
            case THEME_BLACK_HD:
                cssFile = "black_hd.css";
                break;
            case THEME_CUSTOM_CSS:
                return "/mnt/sdcard/style.css";
        }
        return path + cssFile;
    }

    private static MyApp INSTANCE = null;

    public MyApp() {
        INSTANCE = this;


    }

    private MyActivityLifecycleCallbacks m_MyActivityLifecycleCallbacks;

    @Override
    public void onCreate() {
        super.onCreate();
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

    public static MyApp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MyApp();

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


    public String getAppExternalFolderPath() throws IOException {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/4pdaClient/";
        if (!FileUtils.hasStorage(path, true))
            throw new NotReportException("Нет доступа к папке программы: " + path);
        return path;
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