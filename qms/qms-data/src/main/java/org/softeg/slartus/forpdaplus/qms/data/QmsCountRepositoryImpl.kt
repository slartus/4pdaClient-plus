package org.softeg.slartus.forpdaplus.qms.data

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import ru.softeg.slartus.qms.api.models.QmsCount
import ru.softeg.slartus.qms.api.repositories.QmsCountRepository
import ru.softeg.slartus.qms.api.QmsService
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
        qmsService.getQmsCount()
    }

    override suspend fun setCount(count: Int) {
        _countFlow.emit(count)
    }

}