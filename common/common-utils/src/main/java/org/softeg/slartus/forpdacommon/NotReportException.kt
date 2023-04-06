package org.softeg.slartus.forpdacommon

import java.io.IOException

@JvmOverloads
fun notReportError(message: String?, cause: Throwable? = null): Nothing =
    throw NotReportException(message, cause)

open class NotReportException @JvmOverloads constructor(
    message: String?,
    cause: Throwable? = null
) : IOException(message, cause)