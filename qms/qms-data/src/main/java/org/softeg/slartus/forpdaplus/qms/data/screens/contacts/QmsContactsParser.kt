package org.softeg.slartus.forpdaplus.qms.data.screens.contacts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdacommon.fromHtml
import ru.softeg.slartus.qms.api.models.QmsContact
import ru.softeg.slartus.qms.api.models.QmsContacts
import java.util.regex.Pattern
import javax.inject.Inject

class QmsContactsParser @Inject constructor() {

    suspend fun parse(page: String): QmsContacts = withContext(Dispatchers.Default) {
        val res = mutableListOf<QmsContact>()
        val m = QmsContactsPattern.matcher(page)

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

            val qmsUser = QmsContact(id, nick, avatarUrl.orEmpty(), newMessagesCount ?: 0)


            res.add(qmsUser)
        }
        return@withContext QmsContacts(res)
    }

    companion object {
        private val QmsContactsPattern by lazy {
            Pattern.compile(
                "<a class=\"list-group-item[^>]*=(\\d*)\">[^<]*<div class=\"bage\">([^<]*)[\\s\\S]*?src=\"([^\"]*)\" title=\"([^\"]*)\"",
                Pattern.CASE_INSENSITIVE
            )
        }
    }
}