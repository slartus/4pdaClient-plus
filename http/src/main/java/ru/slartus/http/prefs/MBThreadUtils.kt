package ru.slartus.http.prefs

object MBThreadUtils {
    fun doOnBackground(runnable: Runnable) {
        val thread = Thread(runnable)
        thread.start()
    }
}
