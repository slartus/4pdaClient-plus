package org.softeg.slartus.forpdaplus.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog

fun Fragment.showProgress(@StringRes messageId: Int) = MaterialDialog.Builder(requireContext())
    .progress(true, 0)
    .content(messageId)
    .build().apply {
        show()
    }

fun showProgress(context: Context, @StringRes messageId: Int) = MaterialDialog.Builder(context)
    .progress(true, 0)
    .content(messageId)
    .build().apply {
        show()
    }