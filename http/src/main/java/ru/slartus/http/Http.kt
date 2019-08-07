package ru.slartus.http


import android.content.Context
import android.net.ConnectivityManager
import android.support.v4.util.Pair
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import okhttp3.*
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Buffer
import java.io.File
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy.ACCEPT_ALL
import java.util.*
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.asRequestBody

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

        private fun getMimeType(url: String): String? {
            var type: String? = null
            val extension = getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
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

//        if (BuildConfig.DEBUG)
//            builder.addInterceptor(DebugLoggingInterceptor())
//        else
//            builder.addInterceptor(LoggingInterceptor())

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
                   formDataParts: List<Pair<String, String>> = emptyList(),
                   progressListener: CountingFileRequestBody.ProgressListener? = null): AppResponse {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)


        val file = File(filePath)
        val totalSize = file.length()

        val mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtensionFromUrl(file.toString()))?.toMediaTypeOrNull()


        if (progressListener != null) {
            builder.addPart(headersOf("Content-Disposition", "form-data; name=\"$fileFormDataName\"; filename=\"$fileName\""),
                    CountingFileRequestBody(file, mediaType) { num ->
                        val progress = num.toFloat() / totalSize.toFloat() * 100.0
                        progressListener.transferred(progress.toLong())
                    })
        } else {
            builder.addFormDataPart(fileFormDataName, fileName, file.asRequestBody(mediaType))
        }

        formDataParts.filter { it.first != null && it.second != null }.forEach {
            builder.addFormDataPart(it.first!!, it.second!!)
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
