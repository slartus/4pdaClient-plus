package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.subjects.BehaviorSubject
import org.softeg.slartus.forpdaplus.tabs.TabItem

class TabsRepository private constructor() {
    private object Holder {
        val INSTANCE = TabsRepository()
    }

    private val tabs = ArrayList<TabItem>()
    val tabsSubject: BehaviorSubject<List<TabItem>> = BehaviorSubject.createDefault(emptyList())

    fun add(tabItem: TabItem) {
        tabs.add(tabItem)
        tabsSubject.onNext(tabs)
    }

    fun remove(tabItem: TabItem) {
        tabs.remove(tabItem)
        tabsSubject.onNext(tabs)
    }

    fun clear() {
        tabs.clear()
    }

    fun getTabItems(): List<TabItem> {
        return tabs
    }

    fun size() = tabs.size

    var currentFragmentTag: String? = null
        set(value) {
            field = value
            tabsSubject.onNext(tabs)
        }

    fun getLastTabPosition(delPos: Int): Int {
        if (tabs.size - 1 < delPos) return delPos - 1
        return delPos
    }

    fun isContainsByTag(tag: String?) = tabs.any { it.tag == tag }

    fun isContainsByUrl(url: String) = tabs.any { it.url == url }

    fun getTabByTag(tag: String?): TabItem? = tabs.firstOrNull { it.tag == tag }

    fun getTabByUrl(url: String): TabItem? = tabs.firstOrNull { it.url == url }

    fun setTabTitle(tabItem: TabItem, title: String): TabsRepository {
        tabItem.title = title
        return this;
    }

    fun setTabUrl(tabItem: TabItem, url: String): TabsRepository {
        tabItem.url = url
        return this;
    }

    fun setParentTag(tabItem: TabItem, tag: String): TabsRepository {
        tabItem.parentTag = tag

        return this;
    }

    fun apply() {
        tabsSubject.onNext(tabs)
    }

    companion object {
        private val TAG = NotesRepository::class.simpleName

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }
}