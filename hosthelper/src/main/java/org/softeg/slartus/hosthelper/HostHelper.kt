package org.softeg.slartus.hosthelper

fun String?.is4pdaHost() =
    this?.matches(Regex(HostHelper.hostPattern + ".*", RegexOption.IGNORE_CASE)) == true

class HostHelper {
    companion object {
        @JvmStatic
        val host = "4pda.to"
        val schemedHost = "https://$host"
        val endPoint = "$schemedHost/forum/index.php"

        @JvmStatic
        val hostPattern = "(?:^|.*[^a-zA-Z0-9])4pda\\.(?:to|ru)"
    }
}