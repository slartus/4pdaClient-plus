package org.softeg.slartus.forpdaplus.feature_notes.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.softeg.slartus.forpdaplus.feature_notes.network.NotesService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.slartus.http.BuildConfig
import ru.slartus.http.UnzippingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NotesModule {
    @Provides
    fun providesBaseUrl(): String = "https://any.com/"

    @Provides
    @Singleton
    fun provideRetrofit(BASE_URL: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setDateFormat("yyyy.MM.dd HH:mm:ss")
                    .create()
            )
        )
        .client(
            OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(UnzippingInterceptor())
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        this.level =
                            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
                    }
                )
                .build()
        )
        .baseUrl(BASE_URL)
        .build()

    @Provides
    @Singleton
    fun provideNotesService(retrofit: Retrofit): NotesService =
        retrofit.create(NotesService::class.java)
}
