package org.softeg.slartus.forpdaplus.core

import android.content.Context
import androidx.annotation.StringRes

interface AppActions {
    fun showUrlActions(context: Context, @StringRes titleRes: Int, url: String)
    fun showForumTopicsList(forumId: String?, forumTitle: String?)
}