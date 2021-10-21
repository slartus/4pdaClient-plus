package org.softeg.slartus.forpdaplus.core_di.implementations

import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListItem
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListItem
import org.softeg.slartus.forpdaplus.feature_news.di.NewsListDependencies
import org.softeg.slartus.forpdaplus.feature_news.di.NewsListService
import retrofit2.Response
import org.softeg.slartus.forpdaplus.core_api.netwotk.NewsListService as ApiService
import javax.inject.Inject

class NewsListDependenciesImpl @Inject constructor(
    override val newsListService: NewsListService
) : NewsListDependencies

class NewsListServiceImpl @Inject constructor(private val apiService: ApiService) :
    NewsListService {
    override suspend fun all(page: Int): List<NewsListItem> {

        val categories = apiService.categories()
        return map(apiService.all(page))
    }

    override suspend fun categorized(category: String, page: Int): List<NewsListItem> =
        map(apiService.categorized(category, page))

    private fun map(response: Response<List<ApiNewsListItem>>): List<NewsListItem> {
        if (response.isSuccessful) {
            return response.body()
                ?.map {
                    it.map()
                }
                ?: emptyList()
        } else {
            // TODO: обработка response.code
            throw NotReportException(
                response.errorBody()?.string() ?: "Неизвестная ошибка получения новостей"
            )
        }
    }
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