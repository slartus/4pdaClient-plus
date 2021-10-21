package org.softeg.slartus.forpdaplus.core_api.netwotk

import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListCategoryItem
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NewsListService {
    @GET("page/{page}")
    suspend fun all(@Path("page") page: Int = 1): Response<List<ApiNewsListItem>>

    @GET("{path}/page/{page}")
    suspend fun categorized(
        @Path("path") path: String,
        @Path("page") page: Int = 1
    ): Response<List<ApiNewsListItem>>

    @GET("/")
    suspend fun categories(): Response<List<ApiNewsListCategoryItem>>
}