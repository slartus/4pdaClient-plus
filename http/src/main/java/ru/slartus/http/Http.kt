package ru.slartus.http


import android.content.Context
import android.net.ConnectivityManager
import android.support.v4.util.Pair
import android.util.Log
import okhttp3.*
import okio.Buffer
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("unused")
/*
 * Created by slartus on 25.01.2015.
 */
class Http private constructor() {
    private object Holder {
        val INSTANCE = Http()
    }

    companion object {
        const val TAG = "Http"
        val instance by lazy { Holder.INSTANCE }

        @Suppress("DEPRECATION")
        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    private var client: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG)
            builder.addInterceptor(DebugLoggingInterceptor())
        else
            builder.addInterceptor(LoggingInterceptor())

        client = builder
                .build()    // socket timeout
    }


    fun performGet(url: String): String {
        Log.d(TAG, "get: $url")
        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }


    /**
     * Validate cache, return stream. Return cache if no network.
     * @param context
     * @return
     */
    private fun getOnlineInterceptor(context: Context): Interceptor {

        return Interceptor { chain ->
            val response = chain.proceed(chain.request())

            val headers = response.header("Cache-Control")
            if (isOnline(context) && (headers == null || headers.contains("no-store")
                            || headers.contains("must-revalidate")
                            || headers.contains("no-cache") || headers.contains("max-age=0"))) {

                response.newBuilder()
                        .header("Cache-Control", "public, max-age=600")
                        .build()
            } else {
                response
            }
        }
    }

    /**
     * Get me cache.
     * @param context
     * @return
     */
    private fun getOfflineInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            request = if (!isOnline(context)) {
                request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached")
                        .build()
            } else {
                request.newBuilder()
                        .header("Cache-Control", "public, no-cache")
                        .build()
            }

            chain.proceed(request)
        }
    }

    @Throws(IOException::class)
    private fun performGetAsyncWithCache(context: Context, url: String, callback: Callback) {
        Log.d(TAG, "get: $url")
        val request = Request.Builder()
                .url(url)
                .build()

        val cacheSize = (20 * 1024 * 1024).toLong() // 10 MB
        val httpCacheDirectory = File(context.cacheDir, "responses")

        val cache = Cache(httpCacheDirectory, cacheSize)

        client
                .newBuilder()
                .addNetworkInterceptor(getOnlineInterceptor(context))
                .addInterceptor(getOfflineInterceptor(context))
                .cache(cache)
                .build().newCall(request)
                .enqueue(callback)
    }

    @Throws(Exception::class)
    fun postMultipart(url: String, values: List<Pair<String, String>>): String {
        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        val formBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
        for (nameValuePair in values) {
            formBuilder.addFormDataPart(nameValuePair.first!!, nameValuePair.second!!)
        }

        val requestBody = formBuilder.build()
        val request = Request.Builder()

                .url(url)
                .post(requestBody)
                .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")
            return response.body()!!.string()
        } catch (ex: IOException) {
            throw HttpException(ex)
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun performPost(url: String, values: List<Pair<String, String>> = ArrayList()): String {
        val formBuilder = FormBody.Builder()
        values
                .filter { it.second != null }
                .forEach { formBuilder.add(it.first!!, it.second!!) }

        val formBody = formBuilder.build()

        Log.d(TAG, "post: $url")
        val request = Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .post(formBody)
                .build()


        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")
            return response.body()!!.string()
        } catch (ex: IOException) {
            throw HttpException(ex)
        }

    }

    private class LoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            Log.i("OkHttp", request.url().toString())

            return chain.proceed(request)
        }
    }

    private class DebugLoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            val t1 = System.nanoTime()
            val buffer = Buffer()
            request.body()?.writeTo(buffer)
            val requestBody = buffer.readUtf8()
            Log.d("OkHttp", String.format("Sending request %s on %s%n%s body: %s",
                    request.url(), chain.connection(), request.headers(), requestBody))

            val response = chain.proceed(request)

            val t2 = System.nanoTime()



            Log.d("OkHttp", String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6, response.headers()))

            return response
        }
    }
}
