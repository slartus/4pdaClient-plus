package org.softeg.slartus.forpdaplus.domain_qms

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.entities.*
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import ru.softeg.slartus.qms.api.repositories.QmsContactsRepository
import ru.softeg.slartus.qms.api.repositories.QmsCountRepository
import ru.softeg.slartus.qms.api.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.domain_qms.parsers.*
import ru.softeg.slartus.qms.api.models.QmsCount
import ru.softeg.slartus.qms.api.models.QmsContact
import ru.softeg.slartus.qms.api.models.QmsContacts
import ru.softeg.slartus.qms.api.models.QmsThreads
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DomainQmsModule {
    @Binds
    @Singleton
    fun provideQmsServiceImpl(qmsServiceImpl: QmsServiceImpl): QmsService

    @Binds
    @Singleton
    fun provideQmsContactsRepositoryImpl(qmsContactsRepositoryImpl: QmsContactsRepositoryImpl): QmsContactsRepository

    @Binds
    @Singleton
    fun provideQmsCountRepositoryImpl(qmsCountRepositoryImpl: QmsCountRepositoryImpl): QmsCountRepository

    @Binds
    @Singleton
    fun provideQmsContactsParser(parser: QmsContactsParser): Parser<QmsContacts>

    @Binds
    @Singleton
    fun provideQmsCountParser(parser: QmsCountParser): Parser<QmsCount>

    @Binds
    @Singleton
    fun provideMentionsCountParser(parser: MentionsCountParser): Parser<MentionsCount>

    @Binds
    @Singleton
    fun provideQmsContactParser(parser: QmsContactParser): Parser<QmsContact>

    @Binds
    @Singleton
    fun provideQmsThreadsParser(parser: QmsThreadsParser): Parser<QmsThreads>
}

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
interface DomainQmsVieModelModule {

    @Binds
    fun provideQmsThreadsRepositoryImpl(qmsThreadsRepositoryImpl: QmsThreadsRepositoryImpl): QmsThreadsRepository
}