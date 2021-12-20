package org.softeg.slartus.forpdacommon

import android.net.Uri
import java.lang.StringBuilder
import java.net.URLEncoder
import kotlin.math.max

class URIUtils {
    companion object {

        fun escapeHTML(str: String?): String? {
            val s = str ?: return str
            val out = StringBuilder(max(16, s.length))
            for (element in s) {
                if (element.toInt() > 127 || element == '"' || element == '\'' || element == '<' || element == '>' || element == '&') {
                    out.append("&#")
                    out.append(element.toInt())
                    out.append(';')
                } else {
                    out.append(element)
                }
            }
            return out.toString()
        }

        @JvmStatic
        fun createURI(
            scheme: String,
            authority: String,
            path: String,
            params: MutableList<NameValuePair>,
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
            url += "?" + params.joinToString("&") {
                "${it.name}=${URLEncoder.encode(escapeHTML(it.value) ?: "", encoding)}"
            }
            return url
        }

    }
}