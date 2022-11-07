package ru.softeg.slartus.forum.api

interface TopicUsersRepository {
    suspend fun getTopicReaders(topicId: String): TopicReaders
}