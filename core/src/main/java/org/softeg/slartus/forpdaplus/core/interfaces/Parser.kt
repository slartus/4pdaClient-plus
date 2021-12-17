package org.softeg.slartus.forpdaplus.core.interfaces

interface Parser<T> {
    fun parse(page: String): T
}