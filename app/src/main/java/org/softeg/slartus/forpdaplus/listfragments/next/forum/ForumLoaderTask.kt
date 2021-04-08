package org.softeg.slartus.forpdaplus.listfragments.next.forum

import android.content.Context
import android.os.Bundle
import androidx.loader.content.AsyncTaskLoader
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.db.ForumsTable

/*
 * Created by slinkin on 04.02.2019.
 */
internal class ForumLoaderTask internal constructor(context: Context, val args: Bundle?) : AsyncTaskLoader<ForumFragment.ForumBranch>(context) {
    private var mApps: ForumFragment.ForumBranch? = null


    override fun loadInBackground(): ForumFragment.ForumBranch? {
        return try {
            ForumsTable.getForums(args?.getString(ForumFragment.FORUM_ID_KEY))
        } catch (e: Throwable) {
            val forumPage = ForumFragment.ForumBranch()
            forumPage.error = e
            forumPage
        }

    }

    override fun deliverResult(apps: ForumFragment.ForumBranch?) {

        mApps = apps

        if (isStarted) {
            super.deliverResult(apps)
        }

    }

    override fun onStartLoading() {
        if (mApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mApps)
        }

        if (takeContentChanged() || mApps == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad()
        }
    }


    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad()
    }

    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mApps != null) {
            mApps = null
        }
    }

    companion object {
        internal val ID = App.getInstance().uniqueIntValue
    }

}
