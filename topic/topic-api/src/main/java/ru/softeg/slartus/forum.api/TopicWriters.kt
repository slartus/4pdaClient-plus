package ru.softeg.slartus.forum.api

data class TopicWriters(
    private val readers: List<TopicWriter>
) : List<TopicWriter> by readers

data class TopicWriter(val id: String, val nick: String, val postsCount: Int)