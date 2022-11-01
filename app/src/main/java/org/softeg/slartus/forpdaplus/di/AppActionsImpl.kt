package org.softeg.slartus.forpdaplus.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.download.DownloadsService
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes
import org.softeg.slartus.forpdaplus.fragments.qms.QmsNewThreadFragment
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import javax.inject.Inject

class AppActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppActions {
    override fun showUrlActions(context: Context, titleRes: Int, url: String) {
        ExtUrl.showSelectActionDialog(
            context,
            context.getString(titleRes), url
        )
    }

    override fun showUrlActions(context: Context, title: String, url: String) {
        ExtUrl.showSelectActionDialog(context, title, url)
    }

    override fun openTopic(topicUrl: String) {
        IntentActivity.showTopic(topicUrl)
    }

    override fun startDownload(url: String) {
        DownloadsService.download(context, url, false)
    }

    override fun showForumTopicsList(forumId: String?, forumTitle: String?) {
        ForumTopicsListFragment.showForumTopicsList(forumId, forumTitle)
    }

    override fun showQmsContactThreads(contactId: String, contactNick: String?) {
        QmsContactThemes.showThemes(contactId, contactNick)
    }

    override fun showQmsThread(
        contactId: String,
        contactNick: String?,
        threadId: String,
        threadTitle: String?
    ) {
        QmsChatFragment.openChat(contactId, contactNick, threadId, threadTitle)
    }

    override fun showUserProfile(contactId: String, contactNick: String?) {
        ProfileFragment.showProfile(contactId, contactNick)
    }

    override fun showNewQmsContactThread(contactId: String, contactNick: String?) {
        QmsNewThreadFragment.showUserNewThread(contactId, contactNick)
    }
}