package org.softeg.slartus.forpdaplus

import android.app.Activity
import org.softeg.slartus.forpdaplus.common.AppLog
import timber.log.Timber
import java.lang.ref.WeakReference

class TimberTree(private val activity: WeakReference<Activity>) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null) {
            AppLog.e(activity.get() ?: App.getInstance(), t)
        }
    }
}