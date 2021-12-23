package org.softeg.slartus.forpdaplus.domain_qms

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsContacts
import org.softeg.slartus.forpdaplus.core.entities.QmsThreads
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.repositories.QmsCountRepository
import org.softeg.slartus.forpdaplus.core.repositories.QmsThreadsRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.domain_qms.parsers.QmsContactParser
import org.softeg.slartus.forpdaplus.domain_qms.parsers.QmsContactsParser
import org.softeg.slartus.forpdaplus.domain_qms.parsers.QmsCountParser
import org.softeg.slartus.forpdaplus.domain_qms.parsers.QmsThreadsParser
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
    fun provideQmsCountParser(parser: QmsCountParser): Parser<Int>

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