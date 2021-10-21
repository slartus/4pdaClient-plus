package org.softeg.slartus.forpdaplus.core_api.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.softeg.slartus.forpdaplus.core_api.BuildConfig
import retrofit2.Converter
import retrofit2.Retrofit

fun buildOkHttpClient(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG)
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    else
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
    return OkHttpClient.Builder().addInterceptor(interceptor).build()
}

fun buildRetrofit(client: OkHttpClient, factory: Converter.Factory): Retrofit {
    return Retrofit.Builder()
        .addConverterFactory(factory)
        .client(client)
        .baseUrl(ForPdaService.endPoint)
        .build()
}