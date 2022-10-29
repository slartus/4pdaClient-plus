package org.softeg.slartus.forpdaplus.domain_qms.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.softeg.slartus.forpdaplus.core.entities.QmsContact
import org.softeg.slartus.forpdaplus.core.entities.QmsContacts
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.services.QmsService
import javax.inject.Inject

class QmsContactParser @Inject constructor(private val parser: Parser<QmsContacts>) :
    Parser<QmsContact?> {
    override val id: String
        get() = QmsContactParser::class.java.simpleName

    private val _data = MutableStateFlow<QmsContact?>(null)
    override val data
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return args?.containsKey(QmsService.ARG_CONTACT_ID) == true && parser.isOwn(url)
    }

    override suspend fun parse(page: String, args: Bundle?): QmsContact? {
        val result =
            parser.parse(page)?.firstOrNull { it.id == args?.getString(QmsService.ARG_CONTACT_ID) }
        _data.emit(result)
        return result
    }
}