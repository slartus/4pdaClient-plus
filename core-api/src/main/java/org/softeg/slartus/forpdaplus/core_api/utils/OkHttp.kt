package org.softeg.slartus.forpdaplus.core_api.utils

import java.io.Closeable
import java.io.IOException

fun Closeable?.closeQuietly() {
    if (this == null) return
    try {
        this.close()
    } catch (ignored: IOException) {
    }
}