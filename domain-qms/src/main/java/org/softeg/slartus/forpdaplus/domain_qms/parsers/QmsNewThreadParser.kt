package org.softeg.slartus.forpdaplus.domain_qms.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import java.util.regex.Pattern
import javax.inject.Inject

class QmsNewThreadParser @Inject constructor() : Parser<String?> {
    override val id: String
        get() = QmsNewThreadParser::class.java.simpleName

    override val data: Flow<String?>
        get() = TODO("Not yet implemented")

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return urlActPattern.matcher(url).find() && urlXhrPattern.matcher(url).find()
    }

    override suspend fun parse(page: String, args: Bundle?): String? {
        val matcher = threadIdPattern.matcher(page)
        return if (matcher.find()) {
            matcher.group(1)?.toString()
        } else null
    }

    companion object {
        private val urlActPattern by lazy {
            Pattern.compile("act=qms", Pattern.CASE_INSENSITIVE)
        }

        private val urlXhrPattern by lazy {
            Pattern.compile("xhr=body", Pattern.CASE_INSENSITIVE)
        }

        private val threadIdPattern by lazy {
            Pattern.compile("""<input[^>]*?name="t"[^>]*?value="(\d+)"""", Pattern.CASE_INSENSITIVE)
        }
    }
}