package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import androidx.multidex.MultiDexApplication;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;

import com.github.terrakok.cicerone.BaseRouter;
import com.github.terrakok.cicerone.Cicerone;
import com.github.terrakok.cicerone.NavigatorHolder;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdanotifyservice.MainService;
import org.softeg.slartus.forpdanotifyservice.favorites.FavoritesNotifier;
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;
import org.softeg.slartus.forpdaplus.acra.ACRAReportSenderFactory;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;
import org.softeg.slartus.forpdaplus.repositories.InternetConnection;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import io.paperdb.Paper;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.slartus.http.Http;

import static org.softeg.slartus.forpdaplus.prefs.PreferencesActivity.getPackageInfo;

/**
 * User: slinkin
 * Date: 05.08.11
 * Time: 8:03
 */

@ReportsCrashes(
        mode = ReportingInteractionMode.TOAST,
        customReportContent = {ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME, ReportField.USER_COMMENT, ReportField.IS_SILENT, ReportField.PACKAGE_NAME,
                ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.AVAILABLE_MEM_SIZE, ReportField.SHARED_PREFERENCES,
                ReportField.APPLICATION_LOG, ReportField.STACK_TRACE, ReportField.LOGCAT},
        resNotifTitle = R.string.crash_dialog_title,
        resNotifText =  R.string.crash_dialog_text,
        resNotifIcon = R.drawable.notify_icon,
        resToastText = R.string.crash_dialog_text,
        resDialogOkToast = R.string.crash_dialog_ok_toast,
        resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info,
        resDialogTitle = R.string.crash_dialog_title, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        reportSenderFactoryClasses = {ACRAReportSenderFactory.class})
//optional. default is a warning sign
public class App extends MultiDexApplication {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Cicerone cicerone = Cicerone.create();
    public BaseRouter getRouter()
    {
        return cicerone.getRouter();
    }

    public NavigatorHolder getNavigatorHolder()
    {
        return cicerone.getNavigatorHolder();
    }

    private Locale locale;
    private String lang;

    public ScreensController screensController = new ScreensController();
    private int tabIterator = 0;

    public int getTabIterator() {
        return tabIterator;
    }

    public void setTabIterator(int tabIterator) {
        this.tabIterator = tabIterator;
    }

    public void clearTabIterator() {
        tabIterator = 0;
    }

    public void plusTabIterator() {
        tabIterator++;
    }

    private final AtomicInteger m_AtomicInteger = new AtomicInteger();

    public int getUniqueIntValue() {
        return m_AtomicInteger.incrementAndGet();
    }

    private SharedPreferences preferences;

    public SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }


    private static App INSTANCE = null;

    public App() {
        INSTANCE = this;
    }

    private MyActivityLifecycleCallbacks m_MyActivityLifecycleCallbacks;
    @SuppressWarnings("FieldCanBeLocal")
    private static final boolean isNewYear = false;

    @Override
    public void onCreate() {
        super.onCreate();

        org.softeg.slartus.forpdacommon.FACTORY.init(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Configuration config = getResources().getConfiguration();
        lang = getPreferences().getString("lang", "default");
        if (lang.equals("default")) {
            lang = config.locale.getLanguage();
        }
        locale = new Locale(lang);
        Locale.setDefault(locale);
        config.locale = locale;
        getResources().updateConfiguration(config, null);


        initImageLoader(this);
        m_MyActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(m_MyActivityLifecycleCallbacks);
        setTheme(AppTheme.getThemeStyleResID());
        try {
            DbHelper.prepareBases(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Paper.init(this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        resStartNotifierServices();
        Http.Companion.init(this, getString(R.string.app_name), getPackageInfo().versionName);
        Client.getInstance().checkLoginByCookies();
        InternetConnection.getInstance().subscribeInternetState();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (!ACRA.isACRASenderServiceProcess()) {
            ACRA.init(this);

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Configuration config = getResources().getConfiguration();
        locale = new Locale(lang);
        Locale.setDefault(locale);
        config.locale = locale;
        getResources().updateConfiguration(config, null);
    }


    public boolean isNewYear() {
        return isNewYear;
    }

    public void exit() {
        m_MyActivityLifecycleCallbacks.finishActivities();
    }


    public static App getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new App();
        }
        return INSTANCE;
    }

    public static void resStartNotifierServices() {
        reStartQmsService();
        reStartFavoritesNotifierService();
    }

    public static void stopQmsService() {
        try {
            QmsNotifier.cancelAlarm(INSTANCE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void reStartQmsService() {
        reStartQmsService(false);
    }

    public static void reStartQmsService(Boolean adaptive) {
        stopQmsService();
        startQmsService(adaptive);
    }

    private static void startQmsService(Boolean adaptive) {

        try {
            if (!QmsNotifier.isUse(getContext()))
                return;
            Intent intent = new Intent(INSTANCE, MainService.class);
            intent.putExtra("CookiesPath", PreferencesActivity.getCookieFilePath());
            float timeout = Math.max(ExtPreferences.parseFloat(App.getInstance().getPreferences(),
                    QmsNotifier.TIME_OUT_KEY, 5), 1);
            intent.putExtra(QmsNotifier.TIME_OUT_KEY, timeout);

            if (adaptive)
                intent.putExtra(QmsNotifier.ADAPTIVE_TIME_OUT_KEY, 1.0f);

            QmsNotifier.restartTask(INSTANCE, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static final DisplayImageOptions.Builder options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.no_image)
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .handler(new Handler())
            .displayer(new FadeInBitmapDisplayer(500, true, true, false));

    public static DisplayImageOptions.Builder getDefaultOptionsUIL() {
        return options;
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new BaseImageDownloader(context) {
                    @Override
                    public InputStream getStream(String imageUri, Object extra) throws IOException {
                        if (imageUri.startsWith("//"))
                            imageUri = "https:".concat(imageUri);
                        return  super.getStream(imageUri, extra);
                    }

                    @Override
                    protected InputStream getStreamFromNetwork(String imageUri, Object extra) {
                        return Http.Companion.getInstance().response(imageUri).body().byteStream();
                    }
                })
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 2 Mb
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options.build())
                .writeDebugLogs()
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
        try {
            if (!FavoritesNotifier.isUse(getContext())) return;

            Intent intent = new Intent(INSTANCE, MainService.class);
            intent.putExtra("CookiesPath", PreferencesActivity.getCookieFilePath());
            intent.putExtra(FavoritesNotifier.TIME_OUT_KEY, Math.max(ExtPreferences.parseFloat(App.getInstance().getPreferences(),
                    FavoritesNotifier.TIME_OUT_KEY, 5), 1));

            FavoritesNotifier.restartTask(INSTANCE, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return getInstance();
    }

    public static SwipeRefreshLayout createSwipeRefreshLayout(View view,
                                                              final Runnable refreshAction) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.ptr_layout);
        swipeRefreshLayout.setOnRefreshListener(refreshAction::run);
        swipeRefreshLayout.setColorSchemeResources(AppTheme.getMainAccentColor());
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppTheme.getSwipeRefreshBackground());
        return swipeRefreshLayout;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        compositeDisposable.dispose();
    }

    public void addToDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }
}
