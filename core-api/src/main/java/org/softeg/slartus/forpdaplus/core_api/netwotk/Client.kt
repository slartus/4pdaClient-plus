package org.softeg.slartus.forpdaplus.core_api.netwotk

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.softeg.slartus.forpdaplus.core_api.BuildConfig
import org.softeg.slartus.forpdaplus.core_api.converters.FactoriesList
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File

fun buildOkHttpClient(context: Context): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG)
        interceptor.level = HttpLoggingInterceptor.Level.BODY
    else
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
    return OkHttpClient
        .Builder()
        .cache(
            Cache(
                File(context.cacheDir, "http_cache"),
                // $0.05 worth of phone storage in 2020
                50L * 1024L * 1024L // 50 MiB
            )
        )
        .addInterceptor(interceptor)
        .build()
}

fun buildRetrofit(client: OkHttpClient, factory: Converter.Factory): Retrofit {
    return Retrofit.Builder()
        .addConverterFactory(factory)
        .client(client)
        .baseUrl(ForPdaService.endPoint)
        .build()
}