package org.softeg.slartus.forpdaplus.domain_qms.di

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
import org.softeg.slartus.forpdaplus.domain_qms.QmsContactsRepositoryImpl
import org.softeg.slartus.forpdaplus.domain_qms.QmsCountRepositoryImpl
import org.softeg.slartus.forpdaplus.domain_qms.QmsServiceImpl
import org.softeg.slartus.forpdaplus.domain_qms.QmsThreadsRepositoryImpl
import org.softeg.slartus.forpdaplus.domain_qms.parsers.*
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
    fun provideQmsContactParser(parser: QmsContactParser): Parser<QmsContact>

    @Binds
    @Singleton
    fun provideQmsThreadsParser(parser: QmsThreadsParser): Parser<QmsThreads>

    @QmsCountParserInt
    @Binds
    @Singleton
    fun provideQmsCountParser(parser: QmsCountParser): Parser<Int?>

    @QmsNewThreadParserString
    @Binds
    @Singleton
    fun provideQmsNewThreadParser(parser: QmsNewThreadParser): Parser<String?>
}

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
interface DomainQmsVieModelModule {

    @Binds
    fun provideQmsThreadsRepositoryImpl(qmsThreadsRepositoryImpl: QmsThreadsRepositoryImpl): QmsThreadsRepository
}