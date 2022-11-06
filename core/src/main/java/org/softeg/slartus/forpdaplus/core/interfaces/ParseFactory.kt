package org.softeg.slartus.forpdaplus.core.interfaces

interface ParseFactory {
    fun parseAsync(
        url: String,
        body: String,
        exclude: Parser<*>? = null
    )
}