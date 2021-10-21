package org.softeg.slartus.forpdaplus.feature_news.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface NewsListRepository {
    fun getNewsList(page: Int? = null): Flow<PagingData<NewsListItem>>
}