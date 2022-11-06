package org.softeg.slartus.forpdaplus.qms.data.parsers

import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.entities.MentionsCount
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import java.util.regex.Pattern
import javax.inject.Inject

class MentionsCountParser @Inject constructor() : Parser<MentionsCount> {
    private val _data = MutableStateFlow(MentionsCount())
    override val data
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return true
    }

    override suspend fun parse(page: String, args: Bundle?): MentionsCount =
        withContext(Dispatchers.Default) {
            val matcher = pattern.matcher(page)
            if (matcher.find())
                return@withContext MentionsCount(matcher.group(1)?.toString()?.toIntOrNull())
            else
                return@withContext MentionsCount()
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