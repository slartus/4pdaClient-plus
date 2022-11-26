package org.softeg.slartus.forpdaplus.attachments.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.softeg.slartus.attachments.api.AttachmentsRepository
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AttachmentsModule {
    @Binds
    @Singleton
    fun provideAttachmentsRepository(attachmentsRepositoryImpl: AttachmentsRepositoryImpl): AttachmentsRepository
}