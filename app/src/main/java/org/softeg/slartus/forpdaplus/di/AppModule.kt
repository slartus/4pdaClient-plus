package org.softeg.slartus.forpdaplus.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.LinkManager
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumPreferences
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideUserInfoRepositoryImpl(): UserInfoRepository = UserInfoRepositoryImpl.instance

}

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagersModule {
    @Binds
    abstract fun bindLinkManagerImpl(linkManagerImpl: LinkManagerImpl): LinkManager

}