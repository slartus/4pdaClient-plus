package org.softeg.slartus.forpdaplus.core_api.utils

import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import androidx.core.text.HtmlCompat
import org.json.JSONObject
import java.lang.StringBuilder

fun coloredFromHtml(s: String?): Spanned? {
    return if (s == null) null else HtmlCompat.fromHtml(
        s,
        HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
    )
}

fun String?.spannedFromHtml(): Spanned? {
    return if (this == null) null else HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun String?.fromHtml(): String? {
    return this.spannedFromHtml()?.toString()
}

fun htmlEncode(s: String?): String? {
    return if (s == null) null else TextUtils.htmlEncode(s)
}

fun escapeNewLine(s: String): String? {
    val sb = StringBuilder()
    var c: Char
    val length = s.length
    for (i in 0 until length) {
        c = s[i]
        if (c == '\n') {
            sb.append("<br>")
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}

fun escapeQuotes(s: String?): String? {
    var escaped = JSONObject.quote(s)
    escaped = escaped.substring(1, escaped.length - 1)
    return escaped
}