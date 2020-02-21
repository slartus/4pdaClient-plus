package org.softeg.slartus.forpdaapi.vo

import org.softeg.slartus.forpdaapi.parsers.MentionItem
import java.io.Serializable
import kotlin.math.max

class MentionsResult : Serializable {
    private var pagesCount: Int = 0
    private var lastPageStartCount: Int = 0
    var mentions = emptyList<MentionItem>()
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
        return if (pagesCount>1) lastPageStartCount/(pagesCount-1) else 0
    }

}