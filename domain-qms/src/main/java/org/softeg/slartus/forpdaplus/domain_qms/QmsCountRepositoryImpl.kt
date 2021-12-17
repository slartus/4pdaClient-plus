package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.repositories.QmsContactsRepository
import org.softeg.slartus.forpdaplus.core.repositories.QmsCountRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.core_lib.coroutines.AppIOScope
import javax.inject.Inject

class QmsCountRepositoryImpl @Inject constructor(
    private val qmsContactsRepository: QmsContactsRepository,
    private val qmsService: QmsService
) :
    QmsCountRepository {

    private val _count = MutableStateFlow(0)
    override val count: Flow<Int>
        get() = _count.asStateFlow()

    init {
        AppIOScope().launch {
            qmsContactsRepository.contacts
                .drop(1)
                .distinctUntilChanged()
                .collect { contacts ->
                    setCount(contacts.sumOf { it.newMessagesCount ?: 0 })
                }
        }
    }

    override suspend fun load() {
        setCount(qmsService.getQmsCount())
    }

    override suspend fun setCount(count: Int) {
        _count.emit(count)
    }

}