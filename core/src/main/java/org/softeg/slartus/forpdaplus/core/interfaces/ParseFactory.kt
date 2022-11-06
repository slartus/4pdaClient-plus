package org.softeg.slartus.forpdaplus.core.interfaces

interface ParseFactory {
    fun parseAsync(
        body: String,
        exclude: Parser<*>? = null
    )
}