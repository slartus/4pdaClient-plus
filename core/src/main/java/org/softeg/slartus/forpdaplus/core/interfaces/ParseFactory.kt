package org.softeg.slartus.forpdaplus.core.interfaces

import android.os.Bundle

interface ParseFactory {
    suspend fun <T : Any?> parse(
        url: String,
        body: String,
        resultParserId: String? = null,
        args: Bundle? = null
    ): T?
}