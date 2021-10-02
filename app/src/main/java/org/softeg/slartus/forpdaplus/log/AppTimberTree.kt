package org.softeg.slartus.forpdaplus.log

import android.annotation.SuppressLint
import android.util.Log
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.feature_preferences.App
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

internal class AppTimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        if (strings.size > MAX_LOG_LENGTH) {
            strings.poll()
        }
        val logTimeStamp: String = simpleDateFormat.format(
            Date()
        )
        val str = "$logTimeStamp: $tag - $message"
        strings.add(str)

        AppLog.e(App.getInstance(), t)
    }

    companion object {
        @SuppressLint("ConstantLocale")
        private val simpleDateFormat = SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault())
        private const val MAX_LOG_LENGTH = 1000
        private val strings: Queue<String> = ConcurrentLinkedQueue()
    }
}