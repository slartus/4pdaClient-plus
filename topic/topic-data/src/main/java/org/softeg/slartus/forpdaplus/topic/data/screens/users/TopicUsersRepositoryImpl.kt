package org.softeg.slartus.forpdaplus.topic.data.screens.users

import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.TopicReadersParser
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.toTopicReaderOrNull
import ru.softeg.slartus.forum.api.TopicReaders
import ru.softeg.slartus.forum.api.TopicUsersRepository
import javax.inject.Inject

class TopicUsersRepositoryImpl @Inject constructor(
    private val remoteTopicUsersDataSource: RemoteTopicUsersDataSource,
    private val topicReadersParser: TopicReadersParser,
    private val parseFactory: ParseFactory
): TopicUsersRepository {
    override suspend fun getTopicReaders(topicId: String): TopicReaders {
        val page = remoteTopicUsersDataSource.loadTopicReaders(topicId)
        parseFactory.parse<Any>(
            url = "",
            body = page,
            resultParserId = null
        )
        return TopicReaders(topicReadersParser.parse(page).mapNotNull { it.toTopicReaderOrNull() })
    }
}