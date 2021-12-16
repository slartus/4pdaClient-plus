package org.softeg.slartus.forpdaplus.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core.QmsPreferences
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.prefs.AppPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.ForumPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.QmsPreferencesImpl
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideUserInfoRepositoryImpl(): UserInfoRepository = UserInfoRepositoryImpl.instance

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface ManagersModule {
    @Binds
    @Singleton
    fun bindLinkManagerImpl(appActionsImpl: AppActionsImpl): AppActions

    @Binds
    @Singleton
    fun bindAppPreferencesImpl(appPreferencesImpl: AppPreferencesImpl): AppPreferences

    @Binds
    @Singleton
    fun bindForumPreferencesImpl(forumPreferencesImpl: ForumPreferencesImpl): ForumPreferences

    @Binds
    @Singleton
    fun bindQmsPreferencesImpl(qmsPreferencesImpl: QmsPreferencesImpl): QmsPreferences

    @Binds
    @Singleton
    fun bindAppHttpClientImpl(appHttpClientImpl: AppHttpClientImpl): AppHttpClient
}