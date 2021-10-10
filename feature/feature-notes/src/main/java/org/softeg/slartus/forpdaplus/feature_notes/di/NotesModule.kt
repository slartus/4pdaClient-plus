package org.softeg.slartus.forpdaplus.feature_notes.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.feature_notes.network.NotesService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        .baseUrl(BASE_URL)
        .build()

    @Provides
    @Singleton
    fun provideNotesService(retrofit: Retrofit): NotesService =
        retrofit.create(NotesService::class.java)
}
