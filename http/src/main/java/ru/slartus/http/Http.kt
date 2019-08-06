package ru.slartus.http


import android.content.Context
import android.net.ConnectivityManager
import android.os.FileUtils
import android.support.v4.util.Pair
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Buffer
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.CookieManager
import java.net.CookiePolicy.ACCEPT_ALL
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("unused")
/*
 * Created by slartus on 25.01.2015.
 */
class Http private constructor(context: Context) {

    companion object {
        const val TAG = "Http"
        private var INSTANCE: Http? = null
        private var USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36"
        fun init(context: Context) {
            INSTANCE = Http(context)
        }

        val instance by lazy { INSTANCE!! }

        @Suppress("DEPRECATION")
        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    private var client: OkHttpClient
    var cookieStore: PersistentCookieStore = PersistentCookieStore(context)

    init {
        val cookieHandler = CookieManager(cookieStore, ACCEPT_ALL)

        val builder = OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(cookieHandler))
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

    fun request(url: String): Response {
        Log.d(TAG, "get: $url")
        val request = Request.Builder()
                .addHeader("User-Agent", USER_AGENT)
                .url(url)
                .build()

        return client.newCall(request).execute()
    }

    fun performGet(url: String): AppResponse {
        val response = request(url)
        val body = response.body?.string()
        return AppResponse(url, response.request.url.toString(), body)
    }

    fun postMultipart(url: String, values: List<Pair<String, String>>): AppResponse {
        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        val formBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
        for (nameValuePair in values) {
            formBuilder.addFormDataPart(nameValuePair.first!!, nameValuePair.second!!)
        }

        val requestBody = formBuilder.build()
        val request = Request.Builder()
                .addHeader("User-Agent", USER_AGENT)
                .url(url)
                .post(requestBody)
                .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body)
        } catch (ex: IOException) {
            throw HttpException(ex)
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun performPost(url: String, values: List<Pair<String, String>> = ArrayList()): AppResponse {
        val formBuilder = FormBody.Builder()
        values
                .filter { it.second != null }
                .forEach { formBuilder.add(it.first!!, it.second!!) }

        val formBody = formBuilder.build()

        Log.d(TAG, "post: $url")
        val request = Request.Builder()
                .addHeader("User-Agent", USER_AGENT)
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .post(formBody)
                .build()


        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body)
        } catch (ex: IOException) {
            throw HttpException(ex)
        }

    }

    fun uploadFile(url: String, fileName: String, filePath: String, fileFormDataName: String,
                   formDataParts: List<Pair<String, String>> = ArrayList()): AppResponse {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val file = File(filePath)


        val MT = "image/png".toMediaTypeOrNull()
        builder.addFormDataPart(fileFormDataName, fileName, RequestBody.create(MT, file)) // <-------
        builder.addFormDataPart("Cache-Control", "max-age=0")
        builder.addFormDataPart("Upgrade-Insecure-Reaquest", "1")
        builder.addFormDataPart("Accept", "text-/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
        builder.addFormDataPart("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36 OPR/38.0.2220.31")
        builder.addFormDataPart("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
        builder.addFormDataPart("Referer", "https://savepice.ru/")
        builder.addFormDataPart("Origin", "https://savepice.ru")
        builder.addFormDataPart("X-Requested-With", "XMLHttpRequest")
        if (formDataParts.isNotEmpty()) {
            for (formDataPart in formDataParts.filter { it.first != null && it.second != null }) {
                builder.addFormDataPart(formDataPart.first!!, formDataPart.second!!)
            }
        }
        val requestBody = builder.build()
        val request = Request.Builder()
                .addHeader("User-Agent", USER_AGENT)
                .url(url)
                .post(requestBody)
                .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body)
        } catch (ex: IOException) {
            throw HttpException(ex)
        }
    }

    private class LoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            Log.i("OkHttp", request.url.toString())

            return chain.proceed(request)
        }
    }

    private class DebugLoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            val t1 = System.nanoTime()
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            val requestBody = buffer.readUtf8()
            Log.d("OkHttp", String.format("Sending request %s on %s%n%s body: %s",
                    request.url, chain.connection(), request.headers, requestBody))

            val response = chain.proceed(request)

            val t2 = System.nanoTime()



            Log.d("OkHttp", String.format("Received response for %s in %.1fms%n%s",
                    response.request.url, (t2 - t1) / 1e6, response.headers))

            return response
        }
    }
}
