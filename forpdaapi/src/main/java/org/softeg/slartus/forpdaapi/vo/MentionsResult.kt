package org.softeg.slartus.forpdaapi.vo

import java.util.regex.Pattern
import kotlin.math.max

class MentionsResult(var body:String) {
    private var pagesCount: Int = 0
    private var lastPageStartCount: Int = 0

    private var currentPage: Int = 0

    fun setPagesCount(pagesCount: String) {
        this.pagesCount = Integer.parseInt(pagesCount) + 1
    }

    fun setLastPageStartCount(value: String) {
        this.lastPageStartCount = max(Integer.parseInt(value), lastPageStartCount)
    }

    fun setCurrentPage(currentPage: String) {
        this.currentPage = Integer.parseInt(currentPage)
    }

    fun getPagesCount(): Int {
        return pagesCount
    }

    fun getCurrentPage(): Int {
        return currentPage
    }

    fun getPostsPerPageCount(): Int {
        return lastPageStartCount
    }
}