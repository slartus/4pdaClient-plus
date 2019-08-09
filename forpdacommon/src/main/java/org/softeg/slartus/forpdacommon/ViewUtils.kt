package org.softeg.slartus.forpdacommon

import android.support.constraint.Group
import android.view.View


fun Group.setAllOnClickListener(listener: (view: View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}