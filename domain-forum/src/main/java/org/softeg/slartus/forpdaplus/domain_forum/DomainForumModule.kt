package org.softeg.slartus.forpdaplus.domain_forum

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository
import org.softeg.slartus.forpdaplus.core.services.ForumService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DomainForumModule {
    @Binds
    @Singleton
    fun provideForumRepositoryImpl(forumRepositoryImpl: ForumRepositoryImpl): ForumRepository

    @Binds
    @Singleton
    fun provideForumServiceImpl(forumServiceImpl: ForumServiceImpl): ForumService
}