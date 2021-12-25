package org.softeg.slartus.forpdaplus.tabs

class TabsManager {
    private object Holder {
        val INSTANCE = TabsManager()
    }

    companion object {
        const val TAG = "TabsManager"

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }

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

    private val mTabItems = mutableListOf<TabItem>()

    fun getTabItems(): MutableList<TabItem> {
        return mTabItems
    }

    fun getLastTabPosition(delPos: Int): Int {
        var delPos = delPos
        if (mTabItems.size - 1 < delPos) delPos--
        return delPos
    }

    fun isContainsByTag(tag: String?): Boolean {
        for (item in getTabItems()) if (item.tag == tag) return true
        return false
    }

    fun isContainsByUrl(url: String?): Boolean {
        for (item in getTabItems()) if (item.url == url) return true
        return false
    }

    fun getTabByTag(tag: String?): TabItem? {
        for (item in getTabItems()) if (item.tag == tag) return item
        return null
    }

    fun getTabByUrl(url: String): TabItem? {
        for (item in getTabItems()) if (item.url == url) return item
        return null
    }
}