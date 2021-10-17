package org.softeg.slartus.forpdaplus.feature_notes.data

import android.content.Context
import kotlin.Pair
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.hosthelper.HostHelper.Companion.getPostUrl
import org.softeg.slartus.hosthelper.HostHelper.Companion.getTopicUrl
import org.softeg.slartus.hosthelper.HostHelper.Companion.getUserUrl

val Note.topicUrl: String?
    get() = if (topicId != null) getTopicUrl(topicId) else null

val Note.userUrl: String?
    get() = if (userId != null) getUserUrl(userId) else null

val Note.postUrl: String?
    get() = if (topicId != null && postId != null) getPostUrl(topicId, postId) else null

fun Note.getUrls(context: Context): List<Pair<String, String>> {
    val links = mutableListOf<Pair<String, String>>()
    topicUrl?.let {
        links.add(Pair(topicTitle ?: "", it))
    }
    userUrl?.let {
        links.add(Pair(userName ?: "", it))
    }
    postUrl?.let {
        links.add(Pair(context.getString(R.string.link_to_post), it))
    }

    return links
}