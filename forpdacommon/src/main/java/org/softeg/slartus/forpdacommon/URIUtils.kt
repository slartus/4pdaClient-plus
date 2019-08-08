package org.softeg.slartus.forpdacommon

import android.net.Uri
import java.net.URLEncoder

class URIUtils {
    companion object {
        @JvmStatic
        fun createURI(scheme: String, authority: String, path: String,params: MutableList<NameValuePair>, encoding: String): Uri {
            val builder =
                    Uri.Builder()
                            .scheme(scheme)
                            .authority(authority)

            path.split("/").forEach {
                builder.appendPath(it)
            }
            params.forEach {
                builder.appendQueryParameter(it.name,URLEncoder.encode(it.value, encoding))
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