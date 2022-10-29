package org.softeg.slartus.forpdaplus.domain_qms.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.MentionsCount
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import java.util.regex.Pattern
import javax.inject.Inject

class MentionsCountParser @Inject constructor() : Parser<MentionsCount> {
    override val id: String
        get() = MentionsCountParser::class.java.simpleName

    private val _data = MutableStateFlow(MentionsCount())
    override val data
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return true
    }

    override suspend fun parse(page: String, args: Bundle?): MentionsCount {
        val matcher = pattern.matcher(page)
        if (matcher.find())
            return MentionsCount(matcher.group(1)?.toString()?.toIntOrNull())
        return MentionsCount()
    }

    companion object {
        private val pattern by lazy {
            Pattern.compile(
                """\Wact=mentions[^"]*"[^"]*\sdata-count="(\d+)"""",
                Pattern.CASE_INSENSITIVE
            )
        }
    }
}