package org.softeg.slartus.forpdaplus.core_ui

import android.content.Context

import java.io.File
import java.util.*

object CssStyles {
    @JvmStatic
    fun getStylesList(
        context: Context,
        systemDir: String?,
        newStyleNames: ArrayList<CharSequence>,
        newStyleValues: ArrayList<CharSequence>
    ) {
        var xmlPath: String
        var cssStyle: CssStyle
        val styleNames = context.resources.getStringArray(R.array.appthemesArray)
        val styleValues = context.resources.getStringArray(R.array.appthemesValues)
        for (i in styleNames.indices) {
            var styleName: CharSequence = styleNames[i]
            val styleValue: CharSequence = styleValues[i]
            xmlPath = AppTheme.getThemeCssFileName(styleValue.toString()).replace(".css", ".xml")
                .replace("/android_asset/", "")
            cssStyle = CssStyle.parseStyleFromAssets(context, xmlPath)
            if (cssStyle.ExistsInfo) styleName = cssStyle.Title
            newStyleNames.add(styleName)
            newStyleValues.add(styleValue)
        }
        val file = File(systemDir + "styles/")
        getStylesList(newStyleNames, newStyleValues, file)
    }

    private fun getStylesList(
        newStyleNames: ArrayList<CharSequence>,
        newStyleValues: ArrayList<CharSequence>, file: File
    ) {
        var cssPath: String
        var xmlPath: String
        var cssStyle: CssStyle
        if (file.exists()) {
            file.listFiles()
                ?.forEach { cssFile ->
                    if (cssFile.isDirectory) {
                        getStylesList(newStyleNames, newStyleValues, cssFile)
                    } else {
                        cssPath = cssFile.path
                        if (cssPath.lowercase(Locale.getDefault()).endsWith(".css")) {
                            xmlPath = cssPath.replace(".css", ".xml")
                            cssStyle = CssStyle.parseStyleFromFile(xmlPath)
                            val title = cssStyle.Title
                            newStyleNames.add(title)
                            newStyleValues.add(cssPath)
                        }
                    }
                }
        }
    }
}