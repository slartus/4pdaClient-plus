package org.softeg.slartus.forpdaplus.core_di.implementations

import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListItem
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListItem
import org.softeg.slartus.forpdaplus.feature_news.di.NewsListDependencies
import org.softeg.slartus.forpdaplus.feature_news.di.NewsListService
import org.softeg.slartus.forpdaplus.core_api.http.NewsListService as ApiService
import javax.inject.Inject

class NewsListDependenciesImpl @Inject constructor(
    override val newsListService: NewsListService
) : NewsListDependencies

class NewsListServiceImpl @Inject constructor(private val apiService: ApiService) :
    NewsListService {
    override suspend fun getNewsList(page: Int) =
        apiService.getNewsList(page).body()?.items
            ?.map {
                it.map()
            }
            ?: emptyList()
}

fun ApiNewsListItem.map(): NewsListItem =
    NewsListItem(
        this.id,
        this.url,
        this.title,
        this.description,
        this.authorId,
        this.author,
        this.date,
        this.imgUrl,
        this.commentsCount
    )