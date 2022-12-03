package org.softeg.slartus.forpdaplus.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core.QmsPreferences
import org.softeg.slartus.forpdaplus.core.entities.*
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.prefs.AppPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.ForumPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.QmsPreferencesImpl
import org.softeg.slartus.forpdaplus.qms.data.parsers.MentionsCountParser
import org.softeg.slartus.forpdaplus.qms.data.parsers.QmsCountParser
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
    fun provideParseFactoryImpl(
        qmsCountParser: QmsCountParser,
        qmsMentionsCountParser: MentionsCountParser
    ): ParseFactory =
        ParseFactoryImpl(setOf(qmsCountParser, qmsMentionsCountParser))
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