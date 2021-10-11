package org.softeg.slartus.forpdaplus.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppNavigator
import org.softeg.slartus.forpdaplus.feature_notes.di.UrlManager
import org.softeg.slartus.forpdaplus.navigation.AppNavigatorImpl
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {
    @Binds
    abstract fun bindAppNavigator(appNavigatorImpl: AppNavigatorImpl): AppNavigator


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

class UrlManagerImpl (context: Context) : UrlManager {
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


