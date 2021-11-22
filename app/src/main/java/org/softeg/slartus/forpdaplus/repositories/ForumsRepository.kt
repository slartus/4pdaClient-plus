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
        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }

    val forumsSubject: BehaviorSubject<List<Forum>> = BehaviorSubject.createDefault(emptyList())
    private fun load(attemptCount: Int = 0) {
        if (attemptCount == 5) return
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
                    load(attemptCount + 1)
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
        startForumId: String?
    ): Forum? {
        return forumsSubject.value?.firstOrNull { it.id == startForumId }
    }

    init {
        loadDb()

        load()
    }
}