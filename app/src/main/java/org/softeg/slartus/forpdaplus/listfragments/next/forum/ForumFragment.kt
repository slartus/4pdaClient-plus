package org.softeg.slartus.forpdaplus.listfragments.next.forum

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import io.paperdb.Paper
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdaapi.search.SearchSettings
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import org.softeg.slartus.forpdaplus.prefs.Preferences
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*

/*
 * Created by slartus on 24.02.2015.
 */
class ForumFragment : GeneralFragment(), LoaderManager.LoaderCallbacks<ForumFragment.ForumBranch> {
    private var listView: RecyclerView? = null
    private var mEmptyTextView: TextView? = null
    private var data = createListData()
    private var mSearchSetting = SearchSettingsDialogFragment.createForumSearchSettings()


    private var mAdapter: ForumsAdapter? = null
    private var mForumId: String? = null

    private var lastImageDownload = MainActivity.getPreferences().getBoolean("forum.list.show_images", true)

    private val loaderId: Int
        get() = ForumLoaderTask.ID

    private var mTitle: String? = null
    private var mName: String? = null
    private var mNeedLogin: Boolean = false

    private val mHandler = Handler()

    override fun closeTab(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeArrow()
        if (arguments != null)
            mForumId = arguments?.getString(FORUM_ID_KEY, null)
        if (savedInstanceState != null) {
            mName = savedInstanceState.getString(NAME_KEY, mName)
            mTitle = savedInstanceState.getString(TITLE_KEY, mTitle)
            mNeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, mNeedLogin)

            loadCache()

        }
        if (mForumId == null) {
            mForumId = Preferences.List.getStartForumId()
        }
        setTitle(mTitle)
        initAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.add(R.string.mark_forum_as_read)
                ?.setOnMenuItemClickListener {
                    markAsRead()
                    false
                }
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu?.add(R.string.set_forum_starting)
                ?.setOnMenuItemClickListener {
                    val f = data.crumbs[data.crumbs.size - 1]
                    Preferences.List.setStartForum(f.id,
                            f.title)
                    Toast.makeText(activity, R.string.forum_setted_to_start, Toast.LENGTH_SHORT).show()
                    false
                }
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu?.add(R.string.refresh_forum_struct)
                ?.setOnMenuItemClickListener {
                    MaterialDialog.Builder(activity!!)
                            .title(R.string.attention)
                            .content(R.string.forum_refresh_content)
                            .positiveText(R.string.refresh)
                            .onPositive { _, _ -> refreshForumStruct() }
                            .negativeText(R.string.cancel).show()
                    false
                }
                ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    private fun refreshForumStruct() {
        context?.let {
            UpdateForumStructTask(WeakReference(it), object : IProgressListener {
                override fun onProgressChange(dialog: MaterialDialog, message: String) {
                    mHandler.post { dialog.setContent(message) }
                }

                override fun done() {
                    loadData(true)
                }
            }).execute()
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

        if (lastImageDownload == MainActivity.getPreferences().getBoolean("forum.list.show_images", true)) {
            mAdapter?.notifyDataSetChangedWithLayout()
            listView?.refreshDrawableState()
            lastImageDownload = MainActivity.getPreferences().getBoolean("forum.list.show_images", true)
        }
    }

    private fun markAsRead() {
        if (!Client.getInstance().logined) {
            Toast.makeText(activity, R.string.need_login, Toast.LENGTH_SHORT).show()
            return
        }
        MaterialDialog.Builder(activity!!)
                .title(R.string.confirm_action)
                .content(getString(R.string.mark_forum_as_read) + "?")
                .positiveText(R.string.yes)
                .onPositive { _, _ ->
                    Toast.makeText(activity, R.string.request_sent, Toast.LENGTH_SHORT).show()
                    Thread {
                        var ex: Throwable? = null
                        try {
                            val f = data.crumbs[data.crumbs.size - 1]
                            ForumsApi.markForumAsRead(Client.getInstance(), if (f.id == null) "-1" else f.id)

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
                                    Toast.makeText(activity, R.string.forum_setted_read, Toast.LENGTH_SHORT).show()
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

        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        listView?.layoutManager = mLayoutManager
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION_KEY)) {
            listView?.scrollToPosition(savedInstanceState.getInt(SCROLL_POSITION_KEY))
        }
        setListViewAdapter()
        if (data.items.size == 0)
            reloadData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.forum_fragment, container, false)
        assert(view != null)
        listView = findViewById(android.R.id.list) as RecyclerView

        registerForContextMenu(listView!!)
        mEmptyTextView = findViewById(android.R.id.empty) as TextView?


        return view
    }

    private fun reloadData() {
        loadData(true)
    }

    override fun startLoad() {
        reloadData()
    }

    override fun loadData(isRefresh: Boolean) {
        loadForum(mForumId)
    }

    fun loadForum(forumId: String?) {
        val args = Bundle()
        args.putString(FORUM_ID_KEY, forumId)

        setLoading(true)
        if (loaderManager.getLoader<Any>(loaderId) != null)
            loaderManager.restartLoader(loaderId, args, this)
        else
            loaderManager.initLoader(loaderId, args, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ForumBranch> {
        setLoading(true)
        return createLoader(args)
    }

    private fun createLoader(args: Bundle?): Loader<ForumBranch> {
        setLoading(true)
        return ForumLoaderTask(context ?: App.getContext(), args)
    }

    override fun onLoadFinished(loader: Loader<ForumBranch>, data: ForumBranch?) {
        if (data?.error != null) {
            AppLog.e(activity, data.error)
        } else if (data != null) {
            if (data.items.size == 0 && data.items.size > 1) {
                val forum = data.items[data.items.size - 1]
                ForumTopicsListFragment.showForumTopicsList(forum.id, forum.title)
                return
            }
            this.data.items.clear()
            this.data.items.addAll(data.items)
            this.data.crumbs.clear()
            this.data.crumbs.addAll(data.crumbs)

            notifyDataSetChanged()
            listView?.refreshDrawableState()
            listView?.scrollToPosition(0)
        }

        setLoading(false)
    }

    override fun onLoaderReset(loader: Loader<ForumBranch>) {

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

            android.util.Log.e("TAG", ignore.toString())
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_KEY, mName)
        outState.putString(TITLE_KEY, mTitle)
        outState.putBoolean(NEED_LOGIN_KEY, mNeedLogin)
        outState.putString(FORUM_ID_KEY, mForumId)
        saveCache()
        try {
            if (listView != null) {
                outState.putInt(SCROLL_POSITION_KEY,
                        (listView?.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition())
            }
        } catch (ex: Throwable) {
            AppLog.e(ex)
        }

    }

    private fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    private fun setListViewAdapter() {
        listView?.adapter = mAdapter
    }

    @Suppress("DEPRECATION")
    private fun initAdapter() {
        mAdapter = ForumsAdapter(data.crumbs, data.items, object : ForumsAdapter.OnClickListener {
            override fun onItemClick(v: View) {
                val itemPosition = listView!!.getChildPosition(v)
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
                val itemPosition = listView!!.getChildPosition(v)
                val forum = data.crumbs[itemPosition]
                loadForum(forum.id)
            }

            override fun onHeaderTopicsClick(v: View) {
                val itemPosition = listView!!.getChildPosition(v)
                val forum = data.crumbs[itemPosition]
                ForumTopicsListFragment.showForumTopicsList(forum.id, forum.title)
            }


        }, object : ForumsAdapter.OnLongClickListener {
            private fun show(id: String) {
                ExtUrl.showSelectActionDialog(mainActivity, getString(R.string.link), "https://4pda.ru/forum/index.php?showforum=$id")
            }

            override fun onItemClick(v: View) {
                show(data.items[listView!!.getChildPosition(v) - data.crumbs.size].id)
            }

            override fun onHeaderClick(v: View) {
                show(data.crumbs[listView!!.getChildPosition(v)].id)
            }

            override fun onHeaderTopicsClick(v: View) {
                show(data.crumbs[listView!!.getChildPosition(v)].id)
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
        var error: Throwable? = null

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
