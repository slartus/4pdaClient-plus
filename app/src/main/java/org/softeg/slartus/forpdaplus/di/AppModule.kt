package org.softeg.slartus.forpdaplus.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.*
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsContacts
import org.softeg.slartus.forpdaplus.core.entities.QmsThreads
import org.softeg.slartus.forpdaplus.core.entities.UserProfile
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.domain_qms.di.QmsCountParserInt
import org.softeg.slartus.forpdaplus.domain_qms.di.QmsNewThreadParserString
import org.softeg.slartus.forpdaplus.prefs.AppPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.ForumPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.ListPreferencesImpl
import org.softeg.slartus.forpdaplus.prefs.QmsPreferencesImpl
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
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideParseFactoryImpl(
        qmsContactsParser: Parser<QmsContacts>,
        @QmsCountParserInt qmsCountParser: Parser<Int>,
        qmsContactParser: Parser<QmsContact>,
        qmsThreadsParser: Parser<QmsThreads>,
        profileParser: Parser<UserProfile>,
        @QmsNewThreadParserString qmsChatParser: Parser<String>,
    ): ParseFactory =
        ParseFactoryImpl.Builder()
            .add(profileParser)
            .add(qmsChatParser)
            .add(qmsContactsParser)
            .add(qmsContactParser)
            .add(qmsCountParser)
            .add(qmsThreadsParser)
            .build()
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
    fun bindAppHttpClientImpl(appHttpClientImpl: AppHttpClientImpl): AppHttpClient
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface PreferencesModule {

    @Binds
    @Singleton
    fun bindAppPreferencesImpl(appPreferencesImpl: AppPreferencesImpl): AppPreferences

    @Binds
    @Singleton
    fun bindForumPreferencesImpl(forumPreferencesImpl: ForumPreferencesImpl): ForumPreferences

    @Binds
    @Singleton
    fun bindListPreferencesImpl(listPreferencesImpl: ListPreferencesImpl): ListPreferences

    @Binds
    @Singleton
    fun bindQmsPreferencesImpl(qmsPreferencesImpl: QmsPreferencesImpl): QmsPreferences
}