package org.softeg.slartus.forpdaplus.feature_news.di

import org.softeg.slartus.forpdaplus.feature_news.data.NewsListItem

interface NewsListDependencies {
    val newsListService: NewsListService
}

interface NewsListService {
    suspend fun all(page: Int = 1): List<NewsListItem>
    suspend fun categorized(category: String, page: Int = 1): List<NewsListItem>
}