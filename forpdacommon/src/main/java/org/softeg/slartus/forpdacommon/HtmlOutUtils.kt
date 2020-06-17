package org.softeg.slartus.forpdacommon

import android.net.Uri

object HtmlOutUtils {
    @JvmStatic
    fun getHtmlout(methodName: String, val1: String?, val2: String?): String? {
        return getHtmlout( methodName, arrayOf(val1, val2))
    }

    @JvmStatic
    private fun getHtmlout(methodName: String, val1: String): String? {
        return getHtmlout( methodName, arrayOf<String?>(val1))
    }

    @JvmStatic
    fun getHtmlout(methodName: String): String? {
        return getHtmlout( methodName, arrayOfNulls(0))
    }

    @JvmStatic
    private fun getHtmlout(methodName: String, paramValues: Array<String?>): String? {
        return getHtmlout(methodName, paramValues, true)
    }

    @JvmStatic
    fun getHtmlout(methodName: String?, paramValues: Array<String?>, modifyParams: Boolean): String? {
        var sb = StringBuilder()
        if (!Functions.isWebviewAllowJavascriptInterface()) {
            sb.append("href=\"https://www.HTMLOUT.ru/")
            sb.append(methodName).append("?")
            var i = 0
            for (paramName in paramValues) {
                sb.append("val").append(i).append("=").append(if (modifyParams) Uri.encode(paramName) else paramName).append("&")
                i++
            }
            sb = sb.delete(sb.length - 1, sb.length)
            sb.append("\"")
        } else {
            sb.append(" onclick=\"window.HTMLOUT.").append(methodName).append("(")
            for (paramName in paramValues) {
                sb.append("'").append(paramName).append("',")
            }
            if (paramValues.size > 0) sb.delete(sb.length - 1, sb.length)
            sb.append(")\"")
        }
        return sb.toString()
    }

}