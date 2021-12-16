package org.softeg.slartus.forpdaplus.di

import android.content.Context
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import javax.inject.Inject

class AppActionsImpl @Inject constructor() : AppActions {
    override fun showUrlActions(context: Context, titleRes: Int, url: String) {
        ExtUrl.showSelectActionDialog(
            context,
            context.getString(titleRes), url
        )
    }

    override fun showForumTopicsList(forumId: String?, forumTitle: String?) {
        ForumTopicsListFragment.showForumTopicsList(forumId, forumTitle)
    }

    override fun showQmsContactThreads(contactId: String?, contactNick: String?) {
        QmsContactThemes.showThemes(contactId, contactNick)
    }
}