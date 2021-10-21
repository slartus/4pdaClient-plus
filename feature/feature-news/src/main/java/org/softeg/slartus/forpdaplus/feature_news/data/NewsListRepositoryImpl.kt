package org.softeg.slartus.forpdaplus.feature_news.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.feature_news.di.NewsListService

class NewsListRepositoryImpl(private val newsListService: NewsListService) : NewsListRepository {
    override fun getNewsList(page: Int?): Flow<PagingData<NewsListItem>> {
        return Pager(PagingConfig(pageSize = 20)) {
            NewsListPagingSource(newsListService, page, "query")
        }.flow
    }
}