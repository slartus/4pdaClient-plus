package org.softeg.slartus.forpdaplus.core_api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.softeg.slartus.forpdaplus.core_api.converters.NewsListConverterFactory
import org.softeg.slartus.forpdaplus.core_api.netwotk.NewsListService
import org.softeg.slartus.forpdaplus.core_api.netwotk.buildRetrofit
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NewsListModule {
    @Provides
    @Singleton
    @NewsListApi
    fun provideNewsListConverterFactory(): Converter.Factory = NewsListConverterFactory.create()

    @Provides
    @Singleton
    @NewsListApi
    fun provideRetrofit(
        client: OkHttpClient,
        @NewsListApi converterFactory: Converter.Factory
    ): Retrofit = buildRetrofit(client, converterFactory)

    @Provides
    @Singleton
    fun provideNewsService(@NewsListApi retrofit: Retrofit): NewsListService =
        retrofit.create(NewsListService::class.java)
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NewsListApi