package org.softeg.slartus.forpdaplus.common.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.common.data.repositories.emotics.EmoticsRepositoryImpl
import ru.softeg.slartus.common.api.AppTheme
import ru.softeg.slartus.common.api.Settings
import ru.softeg.slartus.common.api.repositories.EmoticsRepository
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object CommonModuleProvides {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface CommonModuleBinds {
    @Binds
    @Singleton
    fun provideSettings(settings: SettingsImpl): Settings

    @Binds
    @Singleton
    fun provideEmoticsRepository(emoticsRepositoryImpl: EmoticsRepositoryImpl): EmoticsRepository

    @Binds
    @Singleton
    fun provideAppTheme(appThemeImpl: AppThemeImpl): AppTheme
}