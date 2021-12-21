package org.softeg.slartus.forpdaplus.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDb
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDependencies
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumService

@Module
@InstallIn(SingletonComponent::class)
abstract class ForumFeatureModule {
    @Binds
    abstract fun bindForumDb(forumDbImpl: ForumDbImpl): ForumDb

    @Binds
    abstract fun bindForumServiceImpl(forumServiceImpl: ForumServiceImpl): ForumService

    @Binds
    abstract fun bindForumDependenciesImpl(forumDependenciesImpl: ForumDependenciesImpl): ForumDependencies
}