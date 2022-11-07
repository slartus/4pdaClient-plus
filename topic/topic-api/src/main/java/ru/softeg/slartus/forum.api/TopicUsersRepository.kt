package ru.softeg.slartus.forum.api

interface TopicUsersRepository {
    suspend fun getTopicReaders(topicId: String): TopicReaders
    suspend fun getTopicWriters(topicId: String): TopicWriters
}