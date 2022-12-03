package org.softeg.slartus.forpdacommon

import android.app.Service
import android.content.Context
import androidx.constraintlayout.widget.Group
import android.view.View
import android.view.inputmethod.InputMethodManager


fun Group.setAllOnClickListener(listener: (view: View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun View.showKeyboard() {
    this.requestFocus()
    val imm = this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}
