package org.softeg.slartus.forpdaplus.topic.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.TopicAttachmentsParser
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.TopicAttachmentsRepositoryImpl
import org.softeg.slartus.forpdaplus.topic.data.screens.attachments.models.TopicAttachmentsResponse
import org.softeg.slartus.forpdaplus.topic.data.screens.users.TopicUsersRepositoryImpl
import ru.softeg.slartus.forum.api.TopicAttachmentsRepository
import ru.softeg.slartus.forum.api.TopicUsersRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface TopicModule {
    @Binds
    @Singleton
    fun provideTopicAttachmentsRepository(forumRepositoryImpl: TopicAttachmentsRepositoryImpl): TopicAttachmentsRepository

    @Binds
    @Singleton
    fun provideTopicUsersRepository(topicUsersRepositoryImpl: TopicUsersRepositoryImpl): TopicUsersRepository

    @Binds
    @Singleton
    fun provideAttachmentsParser(parser: TopicAttachmentsParser): Parser<TopicAttachmentsResponse>
}