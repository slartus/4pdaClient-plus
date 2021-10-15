package org.softeg.slartus.forpdacommon

val Throwable.uiMessage
    get() = this.localizedMessage ?: this.message ?: this.toString()

class ExceptionExtensions {
}