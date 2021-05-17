package ru.slartus.http

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.RealResponseBody
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
        if (response.body() == null) {
            return response
        }

        //check if we have gzip response
        val contentEncoding = response.headers().get("Content-Encoding")

        //this is used to decompress gzipped responses
        return if (contentEncoding != null && contentEncoding == "gzip") {
            val contentLength = response.body?.contentLength() ?: 0L
            val responseBody = GzipSource(response.body?.source())
            val strippedHeaders = response.headers().newBuilder().build()
            response.newBuilder().headers(strippedHeaders)
                .body(
                    RealResponseBody(
                        response.body?.contentType()?.toString(),
                        contentLength,
                        Okio.buffer(responseBody)
                    )
                )
                .build()
        } else {
            response
        }
    }
}