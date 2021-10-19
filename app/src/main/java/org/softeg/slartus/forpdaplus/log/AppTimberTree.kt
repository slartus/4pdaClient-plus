package org.softeg.slartus.forpdaplus.log

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.acra.ACRA
import org.apache.http.conn.ConnectTimeoutException
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.ShowInBrowserException
import org.softeg.slartus.forpdaplus.classes.Exceptions.MessageInfoException
import org.softeg.slartus.forpdaplus.common.AppLog
import ru.slartus.http.HttpException
import timber.log.Timber
import java.lang.ref.WeakReference
import java.net.*

internal class AppTimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null && priority != Log.VERBOSE && priority != Log.DEBUG) {
            when (t) {
                is ShowInBrowserException,
                is UnknownHostException,
                is NotReportException,
                is MessageInfoException,
                is ConnectTimeoutException,
                is SocketTimeoutException,
                is ConnectException,
                is HttpException,
                is SocketException,
                is ProtocolException -> Log.println(priority, tag, "$message $t")
                else -> ACRA.getErrorReporter().handleException(t)
            }
        }
    }
}

internal class ActivityTimberTree constructor(private val activityRef: WeakReference<Activity>) :
    Timber.Tree() {
    constructor(activity: Activity) : this(WeakReference(activity))

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null) {
            runBlocking {
                AppLog.e(activityRef.get(), t)
            }
        }
    }
}