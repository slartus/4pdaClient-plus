package ru.slartus.http

import okhttp3.*
import okhttp3.internal.http.RealResponseBody
import okio.BufferedSink
import okio.GzipSink
import okio.GzipSource
import okio.Okio
import java.io.IOException


class UnzippingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        return unzip(response)
    }

    // copied from okhttp3.internal.http.HttpEngine (because is private)
    @Throws(IOException::class)
    private fun unzip(response: Response): Response {
        val body = response.body() ?: return response

        val contentEncoding = response.headers().get("Content-Encoding") ?: return response
        if (contentEncoding != "gzip") return response

        val contentLength = body.contentLength()

        val responseBody = GzipSource(body.source())
        val strippedHeaders = response.headers().newBuilder().build()
        return response.newBuilder().headers(strippedHeaders)
            .body(
                RealResponseBody(
                    body.contentType()?.toString(),
                    contentLength,
                    Okio.buffer(responseBody)
                )
            )
            .build()
    }
}
