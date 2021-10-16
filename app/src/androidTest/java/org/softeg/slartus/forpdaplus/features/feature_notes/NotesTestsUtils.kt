package org.softeg.slartus.forpdaplus.features.feature_notes

import org.softeg.slartus.forpdaplus.feature_notes.Note
import java.util.*

fun notesList(): List<Note> {
    val random = Random()
    return listOf(
        Note(
            title = "title ${random.nextInt()}",
            body = "body ${random.nextInt()}",
            url = "url ${random.nextInt()}",
            topicId = "topicId ${random.nextInt()}",
            topicTitle = "topicTitle ${random.nextInt()}",
            userId = "userId ${random.nextInt()}",
            userName = "userName ${random.nextInt()}"
        ),
        Note(
            title = "title ${random.nextInt()}",
        ),
        Note(
            body = "body ${random.nextInt()}",
        ),
        Note(
            url = "url ${random.nextInt()}",
        ),
        Note(
            topicId = "topicId ${random.nextInt()}",
            topicTitle = "topicTitle ${random.nextInt()}",
        ),
        Note(
            userId = "userId ${random.nextInt()}",
            userName = "userName ${random.nextInt()}"
        ),
        Note(
            title = "title ${random.nextInt()}",
            body = "body ${random.nextInt()}",
        ),
        Note(
            body = "body ${random.nextInt()}",
            url = "url ${random.nextInt()}",
        ),
        Note(
            url = "url ${random.nextInt()}",
            topicId = "topicId ${random.nextInt()}",
            topicTitle = "topicTitle ${random.nextInt()}",
        ),
        Note(
            topicId = "topicId ${random.nextInt()}",
            topicTitle = "topicTitle ${random.nextInt()}",
            userId = "userId ${random.nextInt()}",
            userName = "userName ${random.nextInt()}"
        ),
        Note(
            title = "title ${random.nextInt()}",
            topicId = "topicId ${random.nextInt()}",
            topicTitle = "topicTitle ${random.nextInt()}",
        ),
        Note(
            body = "body ${random.nextInt()}",
            topicId = "topicId ${random.nextInt()}",
            topicTitle = "topicTitle ${random.nextInt()}",
        ),
        Note(
            body = "body ${random.nextInt()}",
            userId = "userId ${random.nextInt()}",
            userName = "userName ${random.nextInt()}"
        ),
        Note(
            title = "title ${random.nextInt()}",
            userId = "userId ${random.nextInt()}",
            userName = "userName ${random.nextInt()}"
        ),
        Note(
            url = "url ${random.nextInt()}",
            userId = "userId ${random.nextInt()}",
            userName = "userName ${random.nextInt()}"
        ),
        Note(
            url = "url ${random.nextInt()}",
            title = "title ${random.nextInt()}",
        ),
    )
}