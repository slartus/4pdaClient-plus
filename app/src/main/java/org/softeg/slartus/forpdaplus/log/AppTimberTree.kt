package org.softeg.slartus.forpdaplus.log

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.common.AppLog

import timber.log.Timber
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

internal class AppTimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
//            return
//        }
//        if (strings.size > MAX_LOG_LENGTH) {
//            strings.poll()
//        }
//        val logTimeStamp: String = simpleDateFormat.format(
//            Date()
//        )
//        val str = "$logTimeStamp: $tag - $message"
//        strings.add(str)
    }

    companion object {
//        @SuppressLint("ConstantLocale")
//        private val simpleDateFormat = SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault())
//        private const val MAX_LOG_LENGTH = 1000
//        private val strings: Queue<String> = ConcurrentLinkedQueue()
    }
}

internal class ActivityTimberTree(activity: Activity) : Timber.Tree() {
    private val activityRef = WeakReference(activity)
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null) {
            AppLog.e(activityRef.get(), NotReportException(t.message))
        }
    }
}
//
//class ReleaseTree : Timber.DebugTree() {
//
//    override fun log(priority: Int, tag: String?, message: String,
//                     t: Throwable?) {
//        if (priority == Log.VERBOSE) {
//            return
//        }
//        //print to logcat
//        Log.println(priority, tag, message)
//
//        Crashlytics.log(priority, tag, message)
//        val t = t ?: Exception(message)
//        Crashlytics.logException(t)
//    }
//}