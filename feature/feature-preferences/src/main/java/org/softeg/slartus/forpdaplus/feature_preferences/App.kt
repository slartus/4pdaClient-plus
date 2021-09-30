package org.softeg.slartus.forpdaplus.feature_preferences

import android.content.Context
import java.lang.ref.WeakReference

object App {
    private var contextRef: WeakReference<Context>? = null
    fun init(context: Context) {
        contextRef = WeakReference(context)
    }

    @JvmStatic
    fun getInstance() = contextRef?.get()!!

    @JvmStatic
    fun getPreferences() = contextRef?.get()?.preferences
}