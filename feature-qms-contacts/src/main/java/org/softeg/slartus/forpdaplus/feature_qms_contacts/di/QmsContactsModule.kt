package org.softeg.slartus.forpdaplus.feature_qms_contacts.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.feature_qms_contacts.repository.QmsContactsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface QmsContactsModule {
    @Binds
    @Singleton
    fun provideQmsContactsRepositoryImpl(qmsContactsRepositoryImpl: QmsContactsRepositoryImpl): QmsContactsRepository
}