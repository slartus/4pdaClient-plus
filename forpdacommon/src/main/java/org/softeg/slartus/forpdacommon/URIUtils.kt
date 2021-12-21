package org.softeg.slartus.forpdacommon

import android.net.Uri
import java.net.URLEncoder
import kotlin.math.max

class URIUtils {
    companion object {
        @JvmStatic
        fun escapeHTML(str: String?): String? {
            val s = str ?: return str
            val out = StringBuilder(max(16, s.length))
            val ignoreCodes = (1040..1103) + 1025 + 8470// А-я + Ё + №
            val maxLatinCode = 126
            for (element in s) {
                val code = element.toInt()
                if (code > maxLatinCode && code !in ignoreCodes) {
                    out.append("&#")
                    out.append(code)
                    out.append(';')
                } else {
                    out.append(element)
                }
            }
            return out.toString()
        }

        @Deprecated("use params: Map<String, String> instead.")
        @JvmStatic
        fun createURI(
            scheme: String,
            authority: String,
            path: String,
            params: MutableList<NameValuePair>,
            encoding: String
        ): String {
            return createURI(
                scheme,
                authority,
                path,
                params.map { it.name to (it.value ?: "") }.toMap(),
                encoding
            )
        }

        @JvmStatic
        fun createURI(
            scheme: String,
            authority: String,
            path: String,
            params: Map<String, String>,
            encoding: String
        ): String {
            val builder =
                Uri.Builder()
                    .scheme(scheme)
                    .authority(authority)

            path.split("/").forEach {
                builder.appendPath(it)
            }

            var url = builder.build().toString()
            url += "?" + params
                .map { "${it.key}=${URLEncoder.encode(escapeHTML(it.value) ?: "", encoding)}" }
                .joinToString("&")
            return url
        }
    }
}