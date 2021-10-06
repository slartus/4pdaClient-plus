package org.softeg.slartus.forpdaplus.feature_preferences.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.di.AppThemePreferences
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    abstract fun bindCoreUiPreferences(appThemingPreferences: AppThemingPreferencesImpl): AppThemePreferences
}


class AppThemingPreferencesImpl @Inject constructor(): AppThemePreferences {
    override val mainAccentColor: String = Preferences.Common.Overall.mainAccentColor
    override val accentColor: Int = Preferences.Common.Overall.accentColor
    override val accentColorPressed: Int = Preferences.Common.Overall.accentColorPressed
    override val webViewFontName: String = Preferences.Common.Overall.webViewFontName
    override val appstyle: String = Preferences.Common.Overall.appStyle
}