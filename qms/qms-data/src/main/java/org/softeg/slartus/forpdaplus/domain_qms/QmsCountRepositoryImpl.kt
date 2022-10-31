package org.softeg.slartus.forpdaplus.domain_qms

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.softeg.slartus.qms.api.models.QmsCount
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import ru.softeg.slartus.qms.api.repositories.QmsCountRepository
import org.softeg.slartus.forpdaplus.core.services.QmsService
import org.softeg.slartus.forpdaplus.core_lib.coroutines.AppIOScope
import javax.inject.Inject

class QmsCountRepositoryImpl @Inject constructor(
    private val qmsService: QmsService,
    private val qmsCountParser: Parser<QmsCount>
) :
    QmsCountRepository {

    private val _countFlow = MutableStateFlow(0)
    override val countFlow: Flow<Int>
        get() = _countFlow.asStateFlow()

    init {
        AppIOScope().launch {
            qmsCountParser.data
                .drop(1)
                .distinctUntilChanged()
                .collect { item ->
                    setCount(item.count ?: 0)
                }
        }
    }

    override suspend fun load() {
        qmsService.getQmsCount(qmsCountParser.id)
    }

    override suspend fun setCount(count: Int) {
        _countFlow.emit(count)
    }

}