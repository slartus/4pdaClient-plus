package org.softeg.slartus.forpdaplus.domain_qms.parsers

import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.domain_qms.QmsContactImpl
import java.util.regex.Pattern

object QmsContactsParser {
    private val QmsContactsPattern by lazy {
        Pattern.compile(
            "<a class=\"list-group-item[^>]*=(\\d*)\">[^<]*<div class=\"bage\">([^<]*)[\\s\\S]*?src=\"([^\"]*)\" title=\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE
        )
    }

    fun parse(pageBody: String): List<QmsContact> {
        val res = mutableListOf<QmsContact>()
        val m = QmsContactsPattern.matcher(pageBody)

        while (m.find()) {
            val id = m.group(1) ?: continue
            var avatarUrl = m.group(3)
            if (avatarUrl?.substring(0, 2) == "//") {
                avatarUrl = "https:$avatarUrl"
            }
            val nick = m.group(4)?.fromHtml()?.toString()?.trim { it <= ' ' } ?: "Unknown"

            var newMessagesCount: Int? = null
            val countString = m.group(2)?.trim { it <= ' ' } ?: ""
            if (countString != "")
                newMessagesCount = countString.filter { it.isDigit() }.toIntOrNull()

            val qmsUser = QmsContactImpl(id, nick, avatarUrl, newMessagesCount)


            res.add(qmsUser)
        }
        return res
    }
}