package ru.slartus.forpda.feature_preferences

import android.content.Context
import java.lang.ref.WeakReference

object App {
    private var contextRef: WeakReference<Context>? = null
    fun init(context: Context) {
        contextRef = WeakReference(context)
    }

    fun instance() = contextRef?.get()!!
}