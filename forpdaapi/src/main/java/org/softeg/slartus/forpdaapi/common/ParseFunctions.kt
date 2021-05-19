package org.softeg.slartus.forpdaapi.common

object ParseFunctions {

    fun cfDecodeEmail(encodedString: String): String {
        val email = java.lang.StringBuilder(50)

        val r = Integer.parseInt(encodedString.substring(0, 2), 16)

        var n = 2
        while (n < encodedString.length) {
            val i = encodedString.substring(n, n + 2).toInt(16) xor r
            email.append(i.toChar().toString())
            n += 2
        }

        return email.toString()
    }

    @JvmStatic
    fun decodeEmails(page: String): String {
        return Regex(
            "<(span|a)[^>]*data-cfemail=\"([^\"]+)\"[^>]*>\\[email&#160;protected\\]<\\/\\1>",
            RegexOption.IGNORE_CASE
        ).replace(
            page, transform = { matchResult ->
                try {
                    cfDecodeEmail(matchResult.groupValues[2])
                }catch(ex:Throwable) {
                    matchResult.value
                }
            })
    }
}