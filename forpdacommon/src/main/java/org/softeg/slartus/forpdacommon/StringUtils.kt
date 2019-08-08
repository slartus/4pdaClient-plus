package org.softeg.slartus.forpdacommon

/*
 * Created by slinkin on 16.12.2015.
 */


import android.os.Build
import android.text.Html
import android.text.Spanned

fun String.toHtml(): Spanned =StringUtils.fromHtml(this)
fun String.fromHtml(): Spanned =StringUtils.fromHtml(this)

@Suppress("DEPRECATION")
object StringUtils {

    fun fromHtml(html: String): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(html);
        }
    }

}
