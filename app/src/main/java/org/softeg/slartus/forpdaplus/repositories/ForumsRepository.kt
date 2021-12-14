package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdacommon.sameContentWith
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository
import org.softeg.slartus.forpdaplus.db.PaperDb

@Deprecated("use org.softeg.slartus.forpdaplus.core.repositories.ForumRepository instead.")
class ForumsRepository private constructor() {
    private object Holder {
        val INSTANCE = ForumsRepository()
    }
    val forumsSubject: BehaviorSubject<List<Forum>> = BehaviorSubject.createDefault(emptyList())

    fun init(forumRepository: ForumRepository) {
        GlobalScope.launch {
            forumRepository.forum
                .distinctUntilChanged()
                .collect { rawItems ->
                    forumsSubject.onNext(rawItems.map {
                        Forum(it.id, it.title ?: "").apply {
                            description = it.description
                            isHasTopics = it.isHasTopics
                            isHasForums = it.isHasForums
                            iconUrl = it.iconUrl
                            parentId = it.parentId
                        }
                    })
                }
        }

        InternetConnection.instance.loadDataOnInternetConnected({
            GlobalScope.launch {
                forumRepository.load()
            }
        })
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