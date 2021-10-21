package org.softeg.slartus.forpdaplus.core_api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.softeg.slartus.forpdaplus.core_api.http.buildOkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun provideClient(): OkHttpClient = buildOkHttpClient()
}

