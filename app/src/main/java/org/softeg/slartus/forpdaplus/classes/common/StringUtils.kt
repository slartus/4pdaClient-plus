package org.softeg.slartus.forpdaplus.classes.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils

object StringUtils {
    @JvmStatic
    fun join(values: List<String>, string: String): String {
        val sb = StringBuilder()
        var c = values.size
        for (`val` in values) {
            if (c-- > 1) sb.append(`val` + string) else sb.append(`val`)
        }
        return sb.toString()
    }
    @JvmStatic
    fun fromClipboard(context: Context): String? {
        val clipboardManager = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        if (clipboardManager.hasPrimaryClip()) {
            val data = clipboardManager.primaryClip
            if (data != null) for (i in 0 until data.itemCount) {
                var clipboardText = data.getItemAt(i).text
                if (clipboardText != null) if ("primaryClip".contentEquals(clipboardText) || "clipboardManager".contentEquals(clipboardText)) clipboardText = null
                if (clipboardText != null) clipboardText = clipboardText.toString().trim { it <= ' ' }
                if (!TextUtils.isEmpty(clipboardText)) return clipboardText.toString().trim { it <= ' ' }
            }
        }
        return null
    }

    @JvmStatic
    fun copyToClipboard(context: Context, link: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("url", link)
        clipboard.setPrimaryClip(clip)
    }
}