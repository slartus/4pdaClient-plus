package org.softeg.slartus.forpdacommon

import android.net.Uri

fun String.toUriOrNull(): Uri? = kotlin.runCatching { Uri.parse(this) }
    .onFailure {
        it.printStackTrace()
    }
    .getOrNull()

fun Uri.getQueryParameterOrNull(key:String): String? = kotlin.runCatching { this.getQueryParameter(key) }
    .onFailure {
        it.printStackTrace()
    }
    .getOrNull()