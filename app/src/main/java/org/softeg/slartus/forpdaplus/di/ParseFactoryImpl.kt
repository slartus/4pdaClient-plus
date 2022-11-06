package org.softeg.slartus.forpdaplus.di

import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core_lib.coroutines.AppDefaultScope

class ParseFactoryImpl(private val parsers: Set<Parser<*>>) :
    ParseFactory {
    override fun parseAsync(
        body: String,
        exclude: Parser<*>?
    ) {
        AppDefaultScope().launch {
            parsers
                .filter { it != exclude }
                .forEach { parser ->
                    launch {
                        parser.parse(body)
                    }
                }
        }
    }
}