package org.softeg.slartus.forpdaplus.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppNavigator
import org.softeg.slartus.forpdaplus.navigation.AppNavigatorImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {
    @Binds
    abstract fun bindAppNavigator(appNavigatorImpl: AppNavigatorImpl): AppNavigator

}


