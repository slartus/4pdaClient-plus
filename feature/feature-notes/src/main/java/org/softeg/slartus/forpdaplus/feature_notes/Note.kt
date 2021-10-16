package org.softeg.slartus.forpdaplus.feature_notes

import com.google.gson.annotations.SerializedName
import java.util.*

data class Note(
    @SerializedName("_id")
    val id: Int? = null,
    @SerializedName("Title")
    val title: String? = null,
    @SerializedName("Body")
    val body: String? = null,
    @SerializedName("Url")
    val url: String? = null,
    @SerializedName("TopicId")
    val topicId: String? = null,
    @SerializedName("Topic")
    val topicTitle: String? = null,
    @SerializedName("PostId")
    val postId: String? = null,
    @SerializedName("UserId")
    val userId: String? = null,
    @SerializedName("UserName")
    val userName: String? = null,
    @SerializedName("Date")
    val date: Date = Date()
)