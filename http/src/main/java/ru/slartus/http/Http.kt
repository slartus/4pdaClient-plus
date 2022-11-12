package ru.slartus.http


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import androidx.core.util.Pair
import okhttp3.*
import okio.Buffer
import org.softeg.slartus.hosthelper.HostHelper
import java.io.*
import java.net.CookieManager
import java.net.CookiePolicy.ACCEPT_ALL
import java.net.HttpCookie
import java.net.URI
import java.nio.charset.Charset
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Suppress("unused")
/*
 * Created by slartus on 25.01.2015.
 */
class Http private constructor(context: Context, appName: String, appVersion: String) {

    companion object {
        const val TAG = "Http"
        private var INSTANCE: Http? = null
        private const val FULL_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36"

        fun init(context: Context, appName: String, appVersion: String) {
            INSTANCE = Http(context, appName, appVersion)
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

        @JvmStatic
        fun newClientBuiler(context: Context): OkHttpClient.Builder {
            val trustManager = object : X509TrustManager {

                override fun getAcceptedIssuers() =
                    loadSSLCertificates(context).toTypedArray()

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }
            }
            val trustAllCerts = arrayOf<TrustManager>(trustManager)

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .supportsTlsExtensions(true)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
                )
                .build()
            val builder = OkHttpClient.Builder()
                .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(UnzippingInterceptor())
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier { _, _ -> true }
            return builder
        }

        private fun loadSSLCertificates(context: Context): List<X509Certificate> {
            val certificates = ArrayList<X509Certificate>()
            try {

                val certificateFactory = CertificateFactory.getInstance("X.509")
                for (rawId in intArrayOf(R.raw.slartus_ru, R.raw.forpda_to, R.raw.github)) {
                    val inputStream = context.resources.openRawResource(rawId)
                    val certificateInput: InputStream = BufferedInputStream(inputStream)
                    try {
                        val certificate = certificateFactory.generateCertificate(certificateInput)
                        if (certificate is X509Certificate) {
                            certificates.add(certificate)
                        } else {
                            Log.w(TAG, "Wrong Certificate format: $rawId")
                        }
                    } catch (exception: CertificateException) {
                        Log.w(TAG, "Cannot read certificate: $rawId")
                    } finally {
                        try {
                            certificateInput.close()
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: CertificateException) {
                e.printStackTrace()
            }
            return certificates.toList()
        }
    }

    private var client: OkHttpClient
    var cookieStore: PersistentCookieStore = PersistentCookieStore.getInstance(context)

    init {
        val cookieHandler = CookieManager(cookieStore, ACCEPT_ALL)

        val builder = newClientBuiler(context)
            .cookieJar(JavaNetCookieJar(cookieHandler))

        if (BuildConfig.DEBUG)
            builder.addInterceptor(DebugLoggingInterceptor())
        else
            builder.addInterceptor(LoggingInterceptor())

        client = builder
            .build()    // socket timeout
    }

    private val userAgent by lazy {
        String.format(
            Locale.getDefault(),
            "%s/%s (Android %s; %s; %s %s; %s)",
            appName,
            appVersion,
            Build.VERSION.RELEASE,
            Build.MODEL,
            Build.BRAND,
            Build.DEVICE,
            Locale.getDefault().language
        )
    }


    private fun prepareUrl(url: String): String {
        var res = url.replace(
            Regex("^//4pda\\.(?:ru|to)", RegexOption.IGNORE_CASE),
            "https://${HostHelper.host}"
        )
        url.replace(Regex("https?://4pda\\.(?:ru|to)"), HostHelper.host)
        res = res.replace(Regex("^//([^/]*)/"), "https://$1/")
        if (!res.startsWith("http"))
            res = "https://$res"
        return res
    }

    private fun buildRequestHeaders(userAgent: String = this.userAgent): Headers {
        val headersBuilder = Headers.Builder()
        headersBuilder.add(
            "Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
        )
        // headersBuilder.add("Accept-Encoding", "gzip, deflate")
        headersBuilder.add(
            "Accept-Language",
            "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7,vi;q=0.6,bg;q=0.5"
        )
        headersBuilder.add("User-Agent", userAgent)
        headersBuilder.add("Accept-Encoding", "gzip, deflate, br")

        headersBuilder.add(
            "Connection",
            "close"
        )// https://stackoverflow.com/questions/52726909/java-io-ioexception-unexpected-end-of-stream-on-connection
        return headersBuilder.build()
    }

    fun response(url: String) = response(url, false)
    private fun response(url: String, desktopVersion: Boolean = false): Response {

        try {
            if (desktopVersion)
                setCookieDeskVer(true)

            val request = Request.Builder()
                .headers(buildRequestHeaders(if (desktopVersion) FULL_USER_AGENT else userAgent))
                .url(prepareUrl(url))
                .build()

            return client.newCall(request).execute()
        } finally {
            if (desktopVersion)
                setCookieDeskVer(false)
        }
    }

    private fun setCookieDeskVer(deskVer: Boolean) {
        val uri = URI.create("https://${HostHelper.host}/")
        cookieStore.cookies.filter { it.name == "deskver" }.forEach {
            cookieStore.remove(uri, it)
        }
        cookieStore.add(uri, HttpCookie("deskver", if (deskVer) "1" else "0"))
    }

    fun performGet(url: String): AppResponse {
        val response = response(url)
        val body = response.body?.string()
        return AppResponse(url, response.request.url.toString(), body ?: "")
    }

    fun performGetRedirectUrlElseRequestUrl(url: String): String {
        val response = response(url)
        val redirectUrl = response.request.url.toString()
        return redirectUrl.ifEmpty { url }
    }

    fun performGetFull(url: String): AppResponse {
        val response = response(url, true)
        val body = response.body?.string()
        return AppResponse(url, response.request.url.toString(), body ?: "")
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
            .headers(buildRequestHeaders(userAgent))
            .url(prepareUrl(url))
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body ?: "")
        } catch (ex: IOException) {
            throw HttpException(ex)
        }
    }


    @JvmOverloads
    fun performPost(url: String, values: List<Pair<String, String>> = ArrayList()): AppResponse {
        val formBuilder = FormBody.Builder(Charset.forName("windows-1251"))
        values
            .filter { it.second != null }
            .forEach { formBuilder.add(it.first!!, it.second!!) }

        val formBody = formBuilder.build()

        Log.i(TAG, "post: $url")
        val request = Request.Builder()
            .headers(buildRequestHeaders(userAgent))
            .url(prepareUrl(url))
            .cacheControl(CacheControl.FORCE_NETWORK)
            .post(formBody)
            .build()


        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body ?: "")
        } catch (ex: IOException) {
            throw HttpException(ex)
        }

    }

    @JvmOverloads
    fun performPost(url: String, json: String): AppResponse {
        Log.i(TAG, "post: $url json: $json")
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json) // new

        val request = Request.Builder()
            .headers(buildRequestHeaders(userAgent))
            .url(prepareUrl(url))
            .cacheControl(CacheControl.FORCE_NETWORK)
            .post(body)
            .build()


        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body2 = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body2 ?: "")
        } catch (ex: IOException) {
            throw HttpException(ex)
        }

    }

    fun uploadFile(
        url: String,
        fileNameO: String,
        filePath: String,
        fileForm: FileForm = FileForm.FileUpload,
        formDataParts: List<Pair<String, String>> = emptyList(),
        progressListener: CountingFileRequestBody.ProgressListener? = null
    ): AppResponse {


        val fileName = Translit.translit(fileNameO).replace(' ', '_')
        val file = File(filePath)
        val totalSize = file.length()
        if (totalSize == 0L)
            throw HttpException("File is empty")
        val mediaType = "text/plain".toMediaTypeOrNull()

        var progress = 0L
        val fileFormDataName = fileForm.formName

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            if (progressListener != null) {
                addPart(
                    Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"$fileFormDataName\"; filename=\"$fileName\""
                    ),
                    CountingFileRequestBody(file, mediaType) { num ->
                        val newProgress = (num.toFloat() / totalSize.toFloat() * 100.0).toLong()
                        if (progress != newProgress) {
                            progress = newProgress
                            progressListener.transferred(progress)
                        }
                    })
            } else {
                addFormDataPart(fileFormDataName, fileName, file.asRequestBody(mediaType))
            }
            if (!formDataParts.any { it.first.equals("size", ignoreCase = true) })
                addFormDataPart("size", file.length().toString())
            if (!formDataParts.any { it.first.equals("md5", ignoreCase = true) })
                addFormDataPart("md5", FileUtils.calculateMD5(file))
            formDataParts.filter { it.first != null && it.second != null }.forEach {
                addFormDataPart(it.first!!, it.second!!)
            }
        }.build()

        val request = Request.Builder()
            .headers(buildRequestHeaders(userAgent))
            .url(prepareUrl(url))
            .post(requestBody)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw HttpException("Unexpected code $response")

            val body = response.body?.string()
            return AppResponse(url, response.request.url.toString(), body ?: "")
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
            Log.d(
                "OkHttp", String.format(
                    "Sending response %s on %s%n%s body: %s",
                    request.url, chain.connection(), request.headers, requestBody
                )
            )

            val response = chain.proceed(request)

            val t2 = System.nanoTime()



            Log.d(
                "OkHttp", String.format(
                    "Received response for %s in %.1fms%n%s",
                    response.request.url, (t2 - t1) / 1e6, response.headers
                )
            )

            return response
        }
    }
}

enum class FileForm(val formName: String) {
    FileUpload("FILE_UPLOAD")
}
