package org.softeg.slartus.forpdaplus.core_api.utils

import java.util.regex.Matcher

inline fun Matcher.findOnce(action: (Matcher) -> Unit): Matcher {
    if (this.find()) action(this)
    return this
}

inline fun Matcher.findAll(action: (Matcher) -> Unit): Matcher {
    while (this.find()) action(this)
    return this
}

inline fun <R> Matcher.map(crossinline transform: (Matcher) -> R): Sequence<R> {
    return sequence {
        findAll {
            yield(transform(this@map))
        }
    }
}

inline fun <R> Matcher.mapOnce(transform: (Matcher) -> R): R? {
    var data: R? = null
    findOnce {
        data = transform(this)
    }
    return data
}

fun elementRegex(element: String, tags: Map<String, String?>): String {
    val separator = "[^>]*"
    val tagsRegex = tags
        .map {
            if (it.value == null)
                it.key
            else
                """${it.key}="${it.value}""""
        }.joinToString(separator = separator)

    return """<\s*$element$separator$tagsRegex$separator>"""
}

const val MULTILINE_ANY_PATTERN = "[\\s\\S]*?"
fun elementWithBodyRegex(element: String, tags: Map<String, String?>): String {
    val separator = "[^>]*"
    val tagsRegex = tags
        .map {
            if (it.value == null)
                it.key
            else
                """${it.key}="${it.value}""""
        }.joinToString(separator = separator)

    return """<\s*$element$separator$tagsRegex$separator>($MULTILINE_ANY_PATTERN)</$element>"""
}
