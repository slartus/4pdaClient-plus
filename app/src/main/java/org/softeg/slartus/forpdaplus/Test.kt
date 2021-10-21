package org.softeg.slartus.forpdaplus

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core_api.http.NewsListService
import timber.log.Timber

class Test {
    fun run(newsListService: NewsListService) {
        val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Timber.e(throwable)
        }
        MainScope().launch(Dispatchers.Default + handler) {
            val res = newsListService.getNewsList(1)
            Timber.d(res.toString())
        }
    }

}