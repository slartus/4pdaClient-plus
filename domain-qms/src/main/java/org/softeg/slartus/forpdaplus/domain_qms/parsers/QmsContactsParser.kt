package org.softeg.slartus.forpdaplus.domain_qms.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsContacts
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.domain_qms.entities.QmsContactImpl
import java.util.regex.Pattern
import javax.inject.Inject

class QmsContactsParser @Inject constructor() : Parser<QmsContacts> {
    override val id: String
        get() = QmsContactsParser::class.java.simpleName

    private val _data = MutableStateFlow(QmsContacts(emptyList()))
    override val data
        get() = _data.asStateFlow()

    override suspend fun parse(page: String, args: Bundle?): QmsContacts {
        val res = mutableListOf<QmsContact>()
        val m = qmsContactsPattern.matcher(page)

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
        val result = QmsContacts(res)
        _data.emit(result)
        return result
    }

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return urlActPattern.matcher(url).find() && urlActionPattern.matcher(url).find()
    }

    companion object {
        private val qmsContactsPattern by lazy {
            Pattern.compile(
                "<a class=\"list-group-item[^>]*=(\\d*)\">[^<]*<div class=\"bage\">([^<]*)[\\s\\S]*?src=\"([^\"]*)\" title=\"([^\"]*)\"",
                Pattern.CASE_INSENSITIVE
            )
        }

        private val urlActPattern by lazy {
            Pattern.compile("act=qms-xhr", Pattern.CASE_INSENSITIVE)
        }

        private val urlActionPattern by lazy {
            Pattern.compile("action=userlist", Pattern.CASE_INSENSITIVE)
        }
    }

}