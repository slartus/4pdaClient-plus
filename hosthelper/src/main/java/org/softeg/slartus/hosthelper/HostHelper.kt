package org.softeg.slartus.hosthelper

fun String?.is4pdaHost() =
    this?.matches(Regex(HostHelper.hostPattern + ".*", RegexOption.IGNORE_CASE)) == true

class HostHelper {
    companion object {
        @JvmStatic
        val host = "4pda.to"

        @JvmStatic
        val hostPattern = "(?:^|.*[^a-zA-Z0-9])4pda\\.(?:to|ru)"

        fun getTopicUrl(topicId: String) = "https://$host/forum/index.php?showtopic=$topicId"
        fun getUserUrl(userId: String) = "https://$host/forum/index.php?showuser=$userId"
    }
}