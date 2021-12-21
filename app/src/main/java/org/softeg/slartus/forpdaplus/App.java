package org.softeg.slartus.forpdaplus;

import static org.softeg.slartus.forpdaplus.prefs.PreferencesActivity.getPackageInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.multidex.MultiDexApplication;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdanotifyservice.favorites.FavoritesNotifier;
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;
import org.softeg.slartus.forpdaplus.acra.AcraExtensionsKt;
import org.softeg.slartus.forpdaplus.core.AppPreferences;
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;
import org.softeg.slartus.forpdaplus.repositories.ForumsRepository;
import org.softeg.slartus.forpdaplus.repositories.InternetConnection;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.paperdb.Paper;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import ru.slartus.http.Http;
import timber.log.Timber;

@HiltAndroidApp
public class App extends MultiDexApplication {
    @Inject
    ForumRepository forumRepository;
    @Inject
    AppPreferences appPreferences;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    public static String Host = HostHelper.getHost();
    private Locale locale;
    private String lang;


    private final AtomicInteger m_AtomicInteger = new AtomicInteger();

    public int getUniqueIntValue() {
        return m_AtomicInteger.incrementAndGet();
    }

    private SharedPreferences preferences;

    @Deprecated
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

    @Override
    public void onCreate() {
        super.onCreate();
        initTimber();
        //TooLargeTool.startLogging(this);//логирование saveinstancestate
        org.softeg.slartus.forpdacommon.FACTORY.init(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        initLocale();

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
        ForumsRepository.getInstance().init(forumRepository);
    }

    private void initLocale() {
        Configuration config = getResources().getConfiguration();
        lang = appPreferences.getLanguage();
        if (lang.equals(AppPreferences.LANGUAGE_DEFAULT)) {
            lang = config.locale.getLanguage();
        }
        locale = new Locale(lang);
        Locale.setDefault(locale);
        config.locale = locale;
        getResources().updateConfiguration(config, null);
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        AcraExtensionsKt.configureAcra(this);
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
            float timeout = Math.max(ExtPreferences.parseFloat(App.getInstance().getPreferences(),
                    QmsNotifier.TIME_OUT_KEY, 5), 1);

            QmsNotifier.restartTask(INSTANCE, PreferencesActivity.getCookieFilePath(), timeout);
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
                        return super.getStream(imageUri, extra);
                    }

                    @Override
                    protected InputStream getStreamFromNetwork(String imageUri, Object extra) {
                        ResponseBody responseBody = Http.Companion.getInstance().response(imageUri).body();
                        return responseBody != null ? responseBody.byteStream() : null;
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
            float timeout = Math.max(ExtPreferences.parseFloat(App.getInstance().getPreferences(),
                    FavoritesNotifier.TIME_OUT_KEY, 5), 1);

            FavoritesNotifier.restartTask(INSTANCE, PreferencesActivity.getCookieFilePath(), timeout);
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
