package org.softeg.slartus.forpdacommon

import android.net.Uri
import java.net.URLEncoder

class URIUtils {
    companion object {
        @JvmStatic
        fun createURI(scheme: String, authority: String, port: Int, path: String,params: MutableList<NameValuePair>, encoding: String): Uri {
            val builder =
                    Uri.Builder()
                            .scheme(scheme)
                            .authority(authority)
                            .appendPath(path)

            params.forEach {
                builder.appendQueryParameter(it.value,URLEncoder.encode(it.value, encoding))
            }
            return builder.build()
        }

    }
}

class URLEncodedUtils {
    companion object {
        @JvmStatic
        fun format(params: MutableList<NameValuePair>, encoding: String) =
                params.map {
                    "${it.name}=${URLEncoder.encode(it.value, encoding)}"
                }.joinToString(separator = "&")

    }
}