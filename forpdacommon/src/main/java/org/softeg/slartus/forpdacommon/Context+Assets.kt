package org.softeg.slartus.forpdacommon

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun Context.loadAssetsText(assetName: String, charsetName: String = "UTF-8"): String {
    InputStreamReader(
        this.assets.open(assetName),
        charsetName
    ).use { assetsReader ->
        BufferedReader(assetsReader).use { reader ->

            var line: String?
            val sb = StringBuffer()
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            return sb.toString()
        }
    }
}
