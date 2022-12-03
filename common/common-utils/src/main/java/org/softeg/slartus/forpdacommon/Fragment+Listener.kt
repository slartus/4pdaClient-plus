package org.softeg.slartus.forpdacommon

import android.content.Context
import androidx.fragment.app.Fragment

inline fun <reified T> Fragment.getListener(context: Context): T = when {
    context is T -> context
    parentFragment is T -> parentFragment as T
    else -> error("$context must implement ${T::class}")
}