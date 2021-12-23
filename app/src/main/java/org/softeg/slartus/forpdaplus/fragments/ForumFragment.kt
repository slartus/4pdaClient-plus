package org.softeg.slartus.forpdaplus.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.core.entities.SearchSettings
import org.softeg.slartus.forpdaplus.core.interfaces.SearchSettingsListener
import org.softeg.slartus.forpdaplus.feature_forum.ui.ForumFragment
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo

@AndroidEntryPoint
class ForumFragment : BaseGeneralContainerFragment(), SearchSettingsListener {
    private var mSearchSetting = SearchSettingsDialogFragment.createForumSearchSettings()

    private var mTitle: String? = null
    private var mName: String? = null
    private var mNeedLogin: Boolean = false

    override fun closeTab(): Boolean {
        return false
    }

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return ForumFragment().apply {
            this.arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeArrow()

        if (savedInstanceState != null) {
            mName = savedInstanceState.getString(NAME_KEY, mName)
            mTitle = savedInstanceState.getString(TITLE_KEY, mTitle)
            mNeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, mNeedLogin)

        }
        setTitle(mTitle)
    }

    override fun onPause() {
        super.onPause()

        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings()
    }

    override fun onResume() {
        super.onResume()

        removeArrow()
        MainActivity.searchSettings = mSearchSetting
    }

    override fun startLoad() {
        //reloadData()
    }

    override fun loadData(isRefresh: Boolean) {
        //loadForum(viewModel.forumId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_KEY, mName)
        outState.putString(TITLE_KEY, mTitle)
        outState.putBoolean(NEED_LOGIN_KEY, mNeedLogin)
    }

    /**
     * Заголовок списка
     */
    override fun getListTitle(): String? {
        return mTitle
    }

    /**
     * Уникальный идентификатор списка
     */
    override fun getListName(): String? {
        return mName
    }

    fun setBrickInfo(listTemplate: BrickInfo): Fragment {
        mTitle = listTemplate.title
        mName = listTemplate.name
        mNeedLogin = listTemplate.needLogin
        return this
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    companion object {
        const val FORUM_ID_KEY = "FORUM_ID_KEY"
        const val FORUM_TITLE_KEY = "FORUM_TITLE_KEY"

        const val NAME_KEY = "NAME_KEY"
        const val TITLE_KEY = "TITLE_KEY"
        const val NEED_LOGIN_KEY = "NEED_LOGIN_KEY"

        fun showActivity(forumId: String?, topicId: String?) {
            val args = Bundle()
            if (!TextUtils.isEmpty(forumId))
                args.putString(FORUM_ID_KEY, forumId)
            if (!TextUtils.isEmpty(topicId))
                args.putString(TopicsListFragment.KEY_TOPIC_ID, topicId)
            MainActivity.showListFragment(forumId + topicId, ForumBrickInfo().name, args)
        }
    }

    override fun getSearchSettings(): SearchSettings? {
        return childFragmentManager.fragments.filterIsInstance<SearchSettingsListener>()
            .mapNotNull { it.getSearchSettings() }
            .firstOrNull()
    }
}
