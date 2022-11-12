package org.softeg.slartus.forpdaplus.topic.data.screens.users

import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.TopicReadersParser
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.TopicWritersParser
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.toTopicReaderOrNull
import org.softeg.slartus.forpdaplus.topic.data.screens.users.parsers.toTopicWriterOrNull
import ru.softeg.slartus.forum.api.TopicReaders
import ru.softeg.slartus.forum.api.TopicUsersRepository
import ru.softeg.slartus.forum.api.TopicWriters
import javax.inject.Inject

class TopicUsersRepositoryImpl @Inject constructor(
    private val remoteTopicUsersDataSource: RemoteTopicUsersDataSource,
    private val topicReadersParser: TopicReadersParser,
    private val topicWritersParser: TopicWritersParser,
    private val parseFactory: ParseFactory
): TopicUsersRepository {
    override suspend fun getTopicReaders(topicId: String): TopicReaders {
        val page = remoteTopicUsersDataSource.loadTopicReaders(topicId)
        parseFactory.parseAsync(body = page)
        return TopicReaders(topicReadersParser.parse(page).mapNotNull { it.toTopicReaderOrNull() })
    }

    override suspend fun getTopicWriters(topicId: String): TopicWriters {
        val page = remoteTopicUsersDataSource.loadTopicWriters(topicId)
        parseFactory.parseAsync(body = page)
        return TopicWriters(topicWritersParser.parse(page).mapNotNull { it.toTopicWriterOrNull() })
    }
}