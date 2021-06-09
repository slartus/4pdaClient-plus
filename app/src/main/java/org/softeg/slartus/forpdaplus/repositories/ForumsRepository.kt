package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdacommon.sameContentWith
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.PaperDb

class ForumsRepository private constructor() {
    private object Holder {
        val INSTANCE = ForumsRepository()
    }

    companion object {
        private val TAG = ForumsRepository::class.simpleName

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }

    val forumsSubject: BehaviorSubject<List<Forum>> = BehaviorSubject.createDefault(emptyList())
    private fun load() {
        InternetConnection.instance.loadDataOnInternetConnected({
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ForumsApi.loadForumsList()
                    response.forEach {
                        it.isHasForums = response.any { r -> r.parentId == it.id }
                    }
                    if (!response.sameContentWith(forumsSubject.value)) {
                        forumsSubject.onNext(response)
                        saveDb(response)
                    }
                } catch (ex: Throwable) {
                    withContext(Dispatchers.Main) {
                        AppLog.e(ex)
                    }
                    Thread.sleep(5000)
                    load()
                }
            }
        })
    }

    private fun loadDb() {
        val items = PaperDb.read("ForumsRepository.Forums", emptyList<Forum>())
        forumsSubject.onNext(items)
    }

    private fun saveDb(items: List<Forum>) {
        PaperDb.write("ForumsRepository.Forums", items)
    }

    fun findById(
        startForumId: String,
        recursive: Boolean,
        themesNode: Boolean
    ): Forum? {
        return forumsSubject.value?.firstOrNull { it.id == startForumId }
    }

    init {
        loadDb()

        load()
    }
}