package ru.softeg.slartus.forum.api

data class TopicAttachment(
    val id: String,
    val iconUrl: String,
    val url: String,
    val name: String,
    val date: String,
    val size: String,
    val postUrl: String,
    val count: String?
)