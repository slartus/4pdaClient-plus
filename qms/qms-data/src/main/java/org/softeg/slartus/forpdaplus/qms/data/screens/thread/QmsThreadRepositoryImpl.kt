package org.softeg.slartus.forpdaplus.qms.data.screens.thread

import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.qms.data.screens.thread.parsers.QmsThreadParser
import org.softeg.slartus.forpdaplus.qms.data.screens.thread.parsers.checkDeleteMessagesResponse
import ru.softeg.slartus.qms.api.models.QmsThreadPage
import ru.softeg.slartus.qms.api.repositories.QmsThreadRepository
import javax.inject.Inject

class QmsThreadRepositoryImpl @Inject constructor(
    private val remoteQmsThreadDataSource: RemoteQmsThreadDataSource,
    private val parseFactory: ParseFactory,
    private val qmsThreadParser: QmsThreadParser
) : QmsThreadRepository {
    override suspend fun getThread(
        userId: String,
        threadId: String
    ): QmsThreadPage {
        val page = remoteQmsThreadDataSource.getThread(userId, threadId)
        parseFactory.parseAsync(page)
        return qmsThreadParser.parse(page)
    }

    override suspend fun sendMessage(
        userId: String,
        threadId: String,
        message: String,
        attachIds: List<String>
    ) {
        val page = remoteQmsThreadDataSource.sendMessage(userId, threadId, message, attachIds)
        parseFactory.parseAsync(page)
    }

    override suspend fun deleteMessages(userId: String, threadId: String, postIds: List<String>) {
        val page = remoteQmsThreadDataSource.deleteMessages(userId, threadId, postIds)
        parseFactory.parseAsync(page)
        checkDeleteMessagesResponse(page)
    }

    override suspend fun deleteAttach(attachId: String) {
        remoteQmsThreadDataSource.deleteAttach(attachId)
    }
}