package org.softeg.slartus.forpdaplus.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.feature_notes.di.UrlManager
import org.softeg.slartus.forpdaplus.navigation.AppRouterImpl
import org.softeg.slartus.forpdaplus.navigation.ExtendedRouter
import org.softeg.slartus.forpdaplus.navigation.MainActivityNavigator
import java.lang.ref.WeakReference
import javax.inject.Singleton

// read later https://www.coder.work/article/3510927
@Module
@InstallIn(SingletonComponent::class)
class MainModule {
    @Singleton
    @Provides
    fun provideCicerone(): Cicerone<Router> = Cicerone.create(ExtendedRouter())

    @Singleton
    @Provides
    fun provideAppNavigator(cicerone: Cicerone<Router>): AppRouter =
        AppRouterImpl(cicerone.router as ExtendedRouter)

    @Singleton
    @Provides
    fun provideNavigationHolder(cicerone: Cicerone<Router>): NavigatorHolder =
        cicerone.getNavigatorHolder()
}

@Module
@InstallIn(SingletonComponent::class)
class UrlManagerModule {
    @Singleton
    @Provides
    fun provideUrlManager(@ApplicationContext appContext: Context): UrlManager {
        return UrlManagerImpl(appContext)
    }
}

@Module
@InstallIn(ActivityComponent::class)
class MainActivityModule {

    @Provides
    fun provideMainActivityNavigator(@ActivityContext appContext: FragmentActivity): AppNavigator {
        return MainActivityNavigator(appContext)
    }
}

class UrlManagerImpl(context: Context) : UrlManager {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    override fun openUrl(url: String) {
        IntentActivity.tryShowUrl(
            contextRef.get(),
            null,
            url,
            true,
            false,
            null
        )
    }
}


