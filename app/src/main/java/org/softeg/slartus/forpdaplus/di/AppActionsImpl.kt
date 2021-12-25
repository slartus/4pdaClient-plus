package org.softeg.slartus.forpdaplus.di

import android.content.Context
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment
import org.softeg.slartus.forpdaplus.fragments.qms.QmsContactThemes
import org.softeg.slartus.forpdaplus.fragments.qms.QmsNewThreadFragment
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment
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

    override fun back(fragment: Fragment) {
        val brickFragment = fragment.getBrickFragment() ?: return

        (fragment.activity as? MainActivity?)?.tryRemoveTab(brickFragment.tag)
    }

    companion object {
        private fun Fragment.getBrickFragment(): Fragment? {
            return if (this is IBrickFragment) this else this.parentFragment?.getBrickFragment()
        }

    }
}