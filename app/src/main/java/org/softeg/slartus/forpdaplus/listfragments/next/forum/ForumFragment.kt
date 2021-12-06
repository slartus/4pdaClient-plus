package org.softeg.slartus.forpdaplus.listfragments.next.forum

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import io.paperdb.Paper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdaapi.search.SearchSettings
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.databinding.ForumFragmentBinding
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.hosthelper.HostHelper
import java.io.Serializable
import java.util.*

@AndroidEntryPoint
class ForumFragment : GeneralFragment() {

    private var data = createListData()
    private var mSearchSetting = SearchSettingsDialogFragment.createForumSearchSettings()
    private var _binding: ForumFragmentBinding? = null
    private val binding get() = _binding!!
    private var mAdapter: ForumsAdapter? = null
    private var forumId: String? = null

    private var lastImageDownload =
        MainActivity.getPreferences().getBoolean("forum.list.show_images", true)

    private var mTitle: String? = null
    private var mName: String? = null
    private var mNeedLogin: Boolean = false

    private val mHandler = Handler(Looper.getMainLooper())
    private val viewModel: ForumViewModel by lazy {
        val viewModel: ForumViewModel by viewModels()
        viewModel.forumId = forumId
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
            forumId = savedInstanceState.getString(FORUM_ID_KEY, forumId)

        } else if (arguments != null) {
            forumId = arguments?.getString(FORUM_ID_KEY, null)
        }
        if (forumId == null) {
            forumId = Preferences.List.startForumId
        }
        setTitle(mTitle)
        initAdapter()
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            ForumViewModel.ViewState.Initialize -> {
                                setLoading(true)
                            }
                            is ForumViewModel.ViewState.Success -> {
                                data.items.clear()
                                data.items.addAll(uiState.items)
                                data.crumbs.clear()
                                data.crumbs.addAll(uiState.crumbs)

                                notifyDataSetChanged()
                                binding.list.refreshDrawableState()
                                binding.list.scrollToPosition(0)
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(R.string.mark_forum_as_read)
            ?.setOnMenuItemClickListener {
                markAsRead()
                false
            }
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(R.string.set_forum_starting)
            ?.setOnMenuItemClickListener {
                val f = data.crumbs[data.crumbs.size - 1]
                Preferences.List.setStartForum(
                    f.id,
                    f.title
                )
                Toast.makeText(activity, R.string.forum_setted_to_start, Toast.LENGTH_SHORT).show()
                false
            }
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

    }

    override fun onPause() {
        super.onPause()

        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings()
    }

    override fun onResume() {
        super.onResume()

        viewModel.load()

        removeArrow()
        MainActivity.searchSettings = mSearchSetting

        if (lastImageDownload == MainActivity.getPreferences()
                .getBoolean("forum.list.show_images", true)
        ) {
            mAdapter?.notifyDataSetChangedWithLayout()
            binding.list.refreshDrawableState()
            lastImageDownload =
                MainActivity.getPreferences().getBoolean("forum.list.show_images", true)
        }
    }

    private fun markAsRead() {
        if (!Client.getInstance().logined) {
            Toast.makeText(activity, R.string.need_login, Toast.LENGTH_SHORT).show()
            return
        }
        MaterialDialog.Builder(requireActivity())
            .title(R.string.confirm_action)
            .content(getString(R.string.mark_forum_as_read) + "?")
            .positiveText(R.string.yes)
            .onPositive { _, _ ->
                Toast.makeText(activity, R.string.request_sent, Toast.LENGTH_SHORT).show()
                Thread {
                    var ex: Throwable? = null
                    try {
                        val f = data.crumbs[data.crumbs.size - 1]
                        ForumsApi.markForumAsRead(Client.getInstance(), f.id ?: "-1")

                    } catch (e: Throwable) {
                        ex = e
                    }

                    val finalEx = ex

                    mHandler.post {
                        try {
                            if (finalEx != null) {
                                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
                                AppLog.e(activity, finalEx)
                            } else {
                                Toast.makeText(
                                    activity,
                                    R.string.forum_setted_read,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (ex1: Exception) {
                            AppLog.e(activity, ex1)
                        }

                    }
                }.start()
            }

            .negativeText(R.string.cancel)
            .show()
    }

    private fun createListData(): ForumBranch {
        return ForumBranch()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION_KEY)) {
            binding.list.scrollToPosition(savedInstanceState.getInt(SCROLL_POSITION_KEY))
        }
        setListViewAdapter()
        if (data.items.size == 0)
            reloadData()
    }

    private fun reloadData() {
        loadData(true)
    }

    override fun startLoad() {
        reloadData()
    }

    override fun loadData(isRefresh: Boolean) {
        loadForum(forumId)
    }

    fun loadForum(forumId: String?) {
        this.forumId = forumId
        viewModel.refreshDataState(forumId)

    }

    private fun setLoading(@Suppress("UNUSED_PARAMETER") loading: Boolean?) {
        try {
            if (activity == null) return

            //            if (loading) {
            //                setEmptyText("Загрузка..");
            //            } else {
            //                setEmptyText("Нет данных");
            //            }
        } catch (ignore: Throwable) {

            Log.e("TAG", ignore.toString())
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_KEY, mName)
        outState.putString(TITLE_KEY, mTitle)
        outState.putBoolean(NEED_LOGIN_KEY, mNeedLogin)
        outState.putString(FORUM_ID_KEY, forumId)
        saveCache()
        try {

            outState.putInt(
                SCROLL_POSITION_KEY,
                (binding.list.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            )

        } catch (ex: Throwable) {
            AppLog.e(ex)
        }

    }

    private fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    private fun setListViewAdapter() {
        binding.list.adapter = mAdapter
    }

    @Suppress("DEPRECATION")
    private fun initAdapter() {
        mAdapter = ForumsAdapter(data.crumbs, data.items, object : ForumsAdapter.OnClickListener {
            override fun onItemClick(v: View) {
                val itemPosition = binding.list.getChildPosition(v)
                val forum = data.items[itemPosition - data.crumbs.size]
                if (forum.isHasForums) {
                    loadForum(forum.id)
                    val searchSettings = SearchSettings()
                    searchSettings.source = "all"
                    searchSettings.forumsIds.add(forum.id + "")
                    mSearchSetting = searchSettings
                    MainActivity.searchSettings = mSearchSetting
                } else {
                    ForumTopicsListFragment.showForumTopicsList(forum.id, forum.title)
                }
            }

            override fun onHeaderClick(v: View) {
                val itemPosition = binding.list.getChildPosition(v)
                val forum = data.crumbs[itemPosition]
                loadForum(forum.id)
            }

            override fun onHeaderTopicsClick(v: View) {
                val itemPosition = binding.list.getChildPosition(v)
                val forum = data.crumbs[itemPosition]
                ForumTopicsListFragment.showForumTopicsList(forum.id, forum.title)
            }

        }, object : ForumsAdapter.OnLongClickListener {
            private fun show(id: String?) {
                ExtUrl.showSelectActionDialog(
                    mainActivity,
                    getString(R.string.link),
                    "https://${HostHelper.host}/forum/index.php?showforum=$id"
                )
            }

            override fun onItemClick(v: View) {
                show(data.items[binding.list.getChildPosition(v) - data.crumbs.size].id)
            }

            override fun onHeaderClick(v: View) {
                show(data.crumbs[binding.list.getChildPosition(v)].id)
            }

            override fun onHeaderTopicsClick(v: View) {
                show(data.crumbs[binding.list.getChildPosition(v)].id)
            }
        })
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
        if (data.crumbs.size > 1) {
            loadForum(data.crumbs[data.crumbs.size - 2].id)
            return true
        }
        return false
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

    class ForumBranch : Serializable {
        private var mCrumbs: MutableList<Forum>? = null

        val crumbs: MutableList<Forum>
            get() {
                if (mCrumbs == null)
                    mCrumbs = ArrayList()
                return mCrumbs!!
            }

        private var mItems: MutableList<Forum>? = null

        val items: MutableList<Forum>
            get() {
                if (mItems == null)
                    mItems = ArrayList()
                return mItems!!
            }
    }

    private fun saveCache() {
        Paper.book().write(listName, data)
    }

    private fun loadCache() {
        data = Paper.book().read(listName, data)
    }

    companion object {
        private const val SCROLL_POSITION_KEY = "SCROLL_POSITION_KEY"
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
