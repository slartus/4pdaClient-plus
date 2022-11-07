package ru.softeg.slartus.forum.api

data class TopicReaders(
    private val readers: List<TopicReader>
) : List<TopicReader> by readers

data class TopicReader(val id: String, val nick: String, val htmlColor: String)