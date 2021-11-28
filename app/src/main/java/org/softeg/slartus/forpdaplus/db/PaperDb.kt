package org.softeg.slartus.forpdaplus.db

import io.paperdb.Paper

object PaperDb {
    fun <T> write(key: String, value: T) {
        if (value == null)
            Paper.book().delete(key)
        else
            Paper.book().write(key, value)
    }

    fun <T> read(key: String, defaultValue: T): T {
        return Paper.book().read(key, defaultValue) ?: defaultValue
    }

    fun clear() {
        Paper.book().allKeys.forEach {
            Paper.book().delete(it)
        }
    }

}