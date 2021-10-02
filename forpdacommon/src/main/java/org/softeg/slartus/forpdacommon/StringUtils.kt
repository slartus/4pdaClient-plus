package org.softeg.slartus.forpdacommon

/*
 * Created by slinkin on 16.12.2015.
 */


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


fun String.toHtml(): Spanned = StringUtils.fromHtml(this)
fun String.fromHtml(): Spanned = StringUtils.fromHtml(this)

@Suppress("DEPRECATION")
object StringUtils {

    fun fromHtml(html: String): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(html)
        }
    }

    @JvmStatic
    fun copyToClipboard(context: Context, link: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("url", link)
        clipboard.setPrimaryClip(clip)
    }
}

fun String.md5(): String? {
    try { // Create MD5 Hash
        val digest = MessageDigest.getInstance("MD5")
        digest.update(this.toByteArray())
        val messageDigest = digest.digest()
        // Create Hex String
        val hexString = StringBuffer()
        for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
        return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

fun String.simplifyNumber() =
        this.trimEnd { c -> c == '0' || c == ',' || c == '.' }
