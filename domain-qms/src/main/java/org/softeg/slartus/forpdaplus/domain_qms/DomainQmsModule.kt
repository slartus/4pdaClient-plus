package org.softeg.slartus.forpdaplus.domain_qms

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.repositories.QmsCountRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
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
}