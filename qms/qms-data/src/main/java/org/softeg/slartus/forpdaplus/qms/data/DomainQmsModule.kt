package org.softeg.slartus.forpdaplus.qms.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.entities.*
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.qms.data.parsers.*
import org.softeg.slartus.forpdaplus.qms.data.screens.contacts.QmsContactsRepositoryImpl
import org.softeg.slartus.forpdaplus.qms.data.screens.contacts.RemoteQmsContactsDatasourceImpl
import org.softeg.slartus.forpdaplus.qms.data.screens.thread.QmsThreadRepositoryImpl
import org.softeg.slartus.forpdaplus.qms.data.screens.threads.QmsThreadsRepositoryImpl
import ru.softeg.slartus.qms.api.QmsService
import ru.softeg.slartus.qms.api.models.QmsCount
import ru.softeg.slartus.qms.api.repositories.QmsContactsRepository
import ru.softeg.slartus.qms.api.repositories.QmsCountRepository
import ru.softeg.slartus.qms.api.repositories.QmsThreadRepository
import ru.softeg.slartus.qms.api.repositories.QmsThreadsRepository
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DomainQmsModule {
    @Binds
    @Singleton
    fun provideQmsServiceImpl(qmsServiceImpl: RemoteQmsContactsDatasourceImpl): QmsService

    @Binds
    @Singleton
    fun provideQmsContactsRepositoryImpl(qmsContactsRepositoryImpl: QmsContactsRepositoryImpl): QmsContactsRepository

    @Binds
    @Singleton
    fun provideQmsCountRepositoryImpl(qmsCountRepositoryImpl: QmsCountRepositoryImpl): QmsCountRepository


    @Binds
    @Singleton
    fun provideQmsThreadRepositoryImpl(qmsThreadRepositoryImpl: QmsThreadRepositoryImpl): QmsThreadRepository

    @Binds
    @Singleton
    fun provideQmsCountParser(parser: QmsCountParser): Parser<QmsCount>

    @Binds
    @Singleton
    fun provideMentionsCountParser(parser: MentionsCountParser): Parser<MentionsCount>
}

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
interface DomainQmsVieModelModule {

    @Binds
    fun provideQmsThreadsRepositoryImpl(qmsThreadsRepositoryImpl: QmsThreadsRepositoryImpl): QmsThreadsRepository
}