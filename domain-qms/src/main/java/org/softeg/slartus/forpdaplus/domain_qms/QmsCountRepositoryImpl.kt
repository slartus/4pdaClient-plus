package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.QmsCountRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.core_lib.coroutines.AppIOScope
import javax.inject.Inject

class QmsCountRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val qmsCountParser: Parser<Int>
) :
    QmsCountRepository {

    private val _count = MutableStateFlow(0)
    override val count: Flow<Int>
        get() = _count.asStateFlow()

    init {
        AppIOScope().launch {
            qmsCountParser.data
                .drop(1)
                .distinctUntilChanged()
                .collect { count ->
                    setCount(count)
                }
        }
    }

    override suspend fun load() {
        qmsService.getQmsCount(qmsCountParser.id)
    }

    override suspend fun setCount(count: Int) {
        _count.emit(count)
    }

}