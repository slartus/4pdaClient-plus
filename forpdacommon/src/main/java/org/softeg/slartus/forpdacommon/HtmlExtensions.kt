package org.softeg.slartus.forpdacommon

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned


fun String?.fromHtml(): Spanned {
    return when {
        this == null -> // return an empty spannable if the html is null
            SpannableString("")
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        else -> @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
}