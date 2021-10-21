package org.softeg.slartus.hosthelper

fun String?.is4pdaHost() =
    this?.matches(Regex(HostHelper.hostPattern + ".*", RegexOption.IGNORE_CASE)) == true

class HostHelper {
    companion object {
        const val DEFAULT_CHARSET="windows-1251"

        const val SCHEMA = "https"
        const val AUTHORITY="4pda.to"

        @JvmStatic
        val host = AUTHORITY

        @JvmStatic
        val hostPattern = "(?:^|.*[^a-zA-Z0-9])4pda\\.(?:to|ru)"

        @JvmStatic
        fun getTopicUrl(topicId: String) = "https://$host/forum/index.php?showtopic=$topicId"

        @JvmStatic
        fun getPostUrl(topicId: String, postId: String) =
            "$SCHEMA://$host/forum/index.php?showtopic=$topicId&view=findpost&p=$postId"

        @JvmStatic
        fun getUserUrl(userId: String) = "https://$host/forum/index.php?showuser=$userId"
    }
}