package org.softeg.slartus.forpdaplus.fragments.search

import java.util.regex.Pattern
import kotlin.math.max

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.10.12
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
class SearchResult( var redirectUrl: String?) {
    var pagesCount: Int = 0
        private set

    private var lastPageStartCount: Int = 0
    var currentPage: Int = 0
        private set

    fun setPagesCount(pagesCount: String) {
        this.pagesCount = Integer.parseInt(pagesCount) + 1
    }

    fun setLastPageStartCount(value: String) {
        this.lastPageStartCount = max(Integer.parseInt(value), lastPageStartCount)
    }

    fun setCurrentPage(currentPage: String) {
        this.currentPage = Integer.parseInt(currentPage)
    }

    fun getPostsPerPageCount(url: String): Int {
        var lastUrl = url
        val redirectUri = redirectUrl
        if (redirectUri != null)
            lastUrl = redirectUri.toString()
        val p = Pattern.compile("st=(\\d+)")
        val m = p.matcher(lastUrl)
        if (m.find())
            lastPageStartCount = Math.max(Integer.parseInt(m.group(1)), lastPageStartCount)

        return lastPageStartCount / (pagesCount - 1)
    }
}
