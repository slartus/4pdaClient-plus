package org.softeg.slartus.forpdaplus.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideUserInfoRepositoryImpl(): UserInfoRepository = UserInfoRepositoryImpl.instance
}