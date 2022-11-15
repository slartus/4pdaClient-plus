package org.softeg.slartus.forpdaplus.tabs

import java.util.*

class TabsManager {

    private var currentFragmentTag: String? = null

    private var tabIterator = 0

    fun getTabIterator(): Int {
        return tabIterator
    }

    fun setTabIterator(tabIterator: Int) {
        this.tabIterator = tabIterator
    }

    fun clearTabIterator() {
        tabIterator = 0
    }

    fun plusTabIterator() {
        tabIterator++
    }

    fun getCurrentFragmentTag(): String? {
        return currentFragmentTag
    }

    fun setCurrentFragmentTag(s: String?) {
        currentFragmentTag = s
    }

    private val mTabItems: List<TabItem> = ArrayList()

    fun getTabItems(): List<TabItem> {
        return mTabItems
    }

    fun getLastTabPosition(delPos: Int): Int {
        var delPos = delPos
        if (mTabItems.size - 1 < delPos) delPos--
        return delPos
    }

    fun isContainsByTag(tag: String?): Boolean = mTabItems.any { it.tag == tag }

    fun isContainsByUrl(url: String?): Boolean = mTabItems.any { it.url == url }

    fun getTabByTag(tag: String?): TabItem? = mTabItems.firstOrNull { it.tag == tag }

    fun getTabByUrl(url: String?): TabItem? = mTabItems.firstOrNull { it.url == url }

    private object Holder {
        val INSTANCE = TabsManager()
    }

    companion object {
        const val TAG = "TabsManager"

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }
}