package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.core.repositories.ForumRepository

@Deprecated("use org.softeg.slartus.forpdaplus.core.repositories.ForumRepository instead.")
class ForumsRepository private constructor() {
    private object Holder {
        val INSTANCE = ForumsRepository()
    }

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        MainScope().launch {
            AppLog.e(ex)
        }
    }
    val forumsSubject: BehaviorSubject<List<Forum>> = BehaviorSubject.createDefault(emptyList())

    fun init(forumRepository: ForumRepository) {
        MainScope().launch(errorHandler) {
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
            MainScope().launch(errorHandler) {
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
}