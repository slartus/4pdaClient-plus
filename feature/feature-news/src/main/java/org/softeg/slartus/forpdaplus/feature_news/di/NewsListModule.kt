package org.softeg.slartus.forpdaplus.feature_news.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListRepository
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NewsListModule {
    @Provides
    @Singleton
    fun provideRepository(newsListDependencies: NewsListDependencies): NewsListRepository =
        NewsListRepositoryImpl(newsListDependencies.newsListService)
}