package org.softeg.slartus.forpdaplus.core_api.http

import android.net.Uri
import org.softeg.slartus.forpdaplus.core_api.model.NewsList
import org.softeg.slartus.hosthelper.HostHelper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ForPdaService {
    companion object {
        val endPoint = Uri.Builder()
            .scheme(HostHelper.SCHEMA)
            .authority(HostHelper.AUTHORITY)
            .build().toString()
    }

}

interface NewsListService {
    @GET("page/{page}")
    suspend fun getNewsList(@Path("page") page: Int = 1): Response<NewsList>
}
