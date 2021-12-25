package org.softeg.slartus.forpdaplus.di

import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import timber.log.Timber

class ParseFactoryImpl private constructor(private val parsers: List<Parser<*>>) : ParseFactory {
    class Builder {
        private val parsers = mutableListOf<Parser<*>>()
        fun add(parser: Parser<*>): Builder {
            parsers.add(parser)
            return this
        }

        fun build() = ParseFactoryImpl(parsers.toList())
    }

    override suspend fun <T> parse(
        url: String,
        body: String,
        resultParserId: String?,
        args: Bundle?
    ): T? {
        var result: T? = null
        withContext(Dispatchers.Default) {
            parsers
                .filter { it.isOwn(url, args) }
                .forEach { parser ->
                    launch {
                        val r = parser.parse(body, args)
                        Timber.d(parser.id)
                        if (parser.id == resultParserId)
                            result = r as? T?
                    }
                }
        }
        return result
    }
}