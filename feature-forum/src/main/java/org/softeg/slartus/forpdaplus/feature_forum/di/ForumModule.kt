package org.softeg.slartus.forpdaplus.feature_forum.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.feature_forum.repository.ForumRepository
import org.softeg.slartus.forpdaplus.feature_forum.repository.ForumRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ForumModule {
    @Provides
    @Singleton
    fun provideForumRepository(
        forumService: ForumService,
        forumDb: ForumDb
    ): ForumRepository =
        ForumRepositoryImpl(forumService, forumDb)
}