package org.softeg.slartus.forpdaplus.core_lib.coroutines

import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

val appErrorHandler = CoroutineExceptionHandler { _, ex ->
    MainScope().launch {
        Timber.e(ex)
    }
}

@Suppress("FunctionName")
fun AppDefaultScope(): CoroutineScope =
    ContextScope(SupervisorJob() + Dispatchers.Default + appErrorHandler)

@Suppress("FunctionName")
fun AppIOScope(): CoroutineScope = ContextScope(SupervisorJob() + Dispatchers.IO + appErrorHandler)

@Suppress("FunctionName")
fun AppMainScope(): CoroutineScope =
    ContextScope(SupervisorJob() + Dispatchers.IO + appErrorHandler)

class ContextScope(context: CoroutineContext) : CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    // CoroutineScope is used intentionally for user-friendly representation
    override fun toString(): String = "CoroutineScope(coroutineContext=$coroutineContext)"
}