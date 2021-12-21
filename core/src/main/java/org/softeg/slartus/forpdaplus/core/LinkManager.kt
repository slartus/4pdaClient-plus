package org.softeg.slartus.forpdaplus.core

import android.content.Context
import androidx.annotation.StringRes

interface LinkManager {
    fun showUrlActions(context: Context, @StringRes titleRes: Int, url: String)
}