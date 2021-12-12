package org.softeg.slartus.forpdaplus.listfragments.next.forum

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaapi.search.SearchSettings
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.FingerprintAdapter
import org.softeg.slartus.forpdaplus.databinding.ForumFragmentBinding
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.next.forum.fingerprints.ForumDataItemFingerprint
import org.softeg.slartus.forpdaplus.listfragments.next.forum.fingerprints.ForumHeaderCurrentItemFingerprint
import org.softeg.slartus.forpdaplus.listfragments.next.forum.fingerprints.ForumHeaderItemFingerprint
import org.softeg.slartus.forpdaplus.listfragments.next.forum.fingerprints.ForumHeaderNoTopicsItemFingerprint
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.hosthelper.HostHelper

@AndroidEntryPoint
class ForumFragment : GeneralFragment() {
    private var mSearchSetting = SearchSettingsDialogFragment.createForumSearchSettings()
    private var _binding: ForumFragmentBinding? = null
    private val binding get() = _binding!!

    private var mAdapter: FingerprintAdapter? = createAdapter().apply {
        this.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    private var mTitle: String? = null
    private var mName: String? = null
    private var mNeedLogin: Boolean = false

    private val viewModel: ForumViewModel by lazy {
        val viewModel: ForumViewModel by viewModels()
        viewModel.forumId = viewModel.forumId ?: arguments?.getString(FORUM_ID_KEY, null)
                ?: Preferences.List.startForumId
        viewModel
    }

    override fun closeTab(): Boolean {
        return false
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

    override fun getView(): View? {
        return _binding?.root
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ForumFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeToRefresh.setOnRefreshListener {
            reloadData()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            ForumViewModel.ViewState.Initialize -> {
                                setLoading(true)
                            }
                            is ForumViewModel.ViewState.Success -> {
                                mAdapter?.submitList(uiState.items) {
                                    if (uiState.scrollToTop)
                                        binding.list.scrollToPosition(0)
                                }
                            }
                            is ForumViewModel.ViewState.Error -> {
                                AppLog.e(activity, uiState.exception)
                            }
                        }
                    }
                }
                launch {
                    viewModel.loading.collect {
                        setLoading(it)
                    }
                }
            }
        }

        setListViewAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_forum, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mark_forum_as_read -> {
                markAsRead()
                true
            }
            R.id.set_forum_starting -> {
                viewModel.getCurrentForum()?.let { f ->
                    Preferences.List.setStartForum(f.id, f.title)
                    Toast.makeText(
                        requireContext(),
                        R.string.forum_setted_to_start,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

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

    private fun createAdapter() = FingerprintAdapter(
        listOf(
            ForumHeaderItemFingerprint(
                { _, item -> loadForum(item.id) },
                { _, item ->
                    show(item.id)
                    true
                }),
            ForumHeaderCurrentItemFingerprint(
                { _, item -> ForumTopicsListFragment.showForumTopicsList(item.id, item.title) },
                { _, item ->
                    show(item.id)
                    true
                }),
            ForumHeaderNoTopicsItemFingerprint(
                { _, item -> loadForum(item.id) },
                { _, item ->
                    show(item.id)
                    true
                }),
            ForumDataItemFingerprint(
                MainActivity.getPreferences()
                    .getBoolean("forum.list.show_images", true),
                { _, item ->
                    if (item.isHasForums) {
                        loadForum(item.id)
                        val searchSettings = SearchSettings()
                        searchSettings.source = "all"
                        searchSettings.forumsIds.add(item.id + "")
                        mSearchSetting = searchSettings
                        MainActivity.searchSettings = mSearchSetting
                    } else {
                        ForumTopicsListFragment.showForumTopicsList(item.id, item.title)
                    }
                },
                { _, item ->
                    show(item.id)
                    true
                })
        )
    )

    private fun markAsRead() {
        if (!viewModel.isLogined()) {
            Toast.makeText(activity, R.string.need_login, Toast.LENGTH_SHORT).show()
            return
        }
        MaterialDialog.Builder(requireActivity())
            .title(R.string.confirm_action)
            .content(getString(R.string.mark_forum_as_read) + "?")
            .positiveText(R.string.yes)
            .onPositive { _, _ -> markForumRead() }
            .negativeText(R.string.cancel)
            .show()
    }

    private fun markForumRead() {
        Toast.makeText(activity, R.string.request_sent, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                viewModel.getCurrentForum()?.let { f ->
                    viewModel.markForumRead(f.id ?: "-1")
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        activity,
                        R.string.forum_setted_read,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
                    AppLog.e(activity, e)
                }
            }
        }
    }

    private fun reloadData() {
        viewModel.reload()
    }

    override fun startLoad() {
        reloadData()
    }

    override fun loadData(isRefresh: Boolean) {
        loadForum(viewModel.forumId)
    }

    private fun loadForum(forumId: String?) {
        viewModel.refreshDataState(forumId)
    }

    private fun setLoading(loading: Boolean) {
        try {
            if (activity == null) return
            binding.swipeToRefresh.isRefreshing = loading
        } catch (ignore: Throwable) {
            Log.e("TAG", ignore.toString())
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_KEY, mName)
        outState.putString(TITLE_KEY, mTitle)
        outState.putBoolean(NEED_LOGIN_KEY, mNeedLogin)
    }

    private fun setListViewAdapter() {
        binding.list.adapter = mAdapter
    }

    private fun show(id: String?) {
        ExtUrl.showSelectActionDialog(
            mainActivity,
            getString(R.string.link),
            "https://${HostHelper.host}/forum/index.php?showforum=$id"
        )

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

    override fun onBackPressed(): Boolean {
        return viewModel.onBack()

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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
}
