package org.softeg.slartus.forpdaplus.core_lib.extensions

import android.app.Dialog

fun Dialog.dismissSafe() {
    if (isShowing) {
        runCatching {
            dismiss()
        }.onFailure {
            it.printStackTrace()
        }
    }
}