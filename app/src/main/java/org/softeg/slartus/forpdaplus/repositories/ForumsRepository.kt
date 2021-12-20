package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdacommon.sameContentWith
import org.softeg.slartus.forpdaplus.db.PaperDb

class ForumsRepository private constructor() {
    private object Holder {
        val INSTANCE = ForumsRepository()
    }

    val forumsSubject: BehaviorSubject<List<Forum>> = BehaviorSubject.createDefault(emptyList())

    init {
        loadDb()

        loadAsync()
    }

    private fun loadAsync() {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
        InternetConnection.instance.loadDataOnInternetConnected({
            MainScope().launch(coroutineExceptionHandler) {
                load()
            }
        })
    }

    suspend fun load(): LoadResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = ForumsApi.loadForumsList()
                response.forEach {
                    it.isHasForums = response.any { r -> r.parentId == it.id }
                }
                if (!response.sameContentWith(forumsSubject.value)) {
                    forumsSubject.onNext(response)
                    saveDb(response)
                }
                LoadResult.Success
            } catch (ex: Throwable) {
                LoadResult.Error(ex)
            }
        }
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

    companion object {
        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }

    sealed class LoadResult {
        object Success : LoadResult()
        class Error(val error: Throwable) : LoadResult()
    }
}