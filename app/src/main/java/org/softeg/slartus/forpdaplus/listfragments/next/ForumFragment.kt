package org.softeg.slartus.forpdaplus.listfragments.next

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener

import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.classes.ForumsData
import org.softeg.slartus.forpdaapi.search.SearchSettings
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.ForumsTable
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.listfragments.ForumTopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo
import org.softeg.slartus.forpdaplus.prefs.Preferences

import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap

/*
 * Created by slartus on 24.02.2015.
 */
class ForumFragment : GeneralFragment(), LoaderManager.LoaderCallbacks<ForumFragment.ForumBranch> {
    protected var listView: RecyclerView? = null
        private set
    private var mEmptyTextView: TextView? = null
    protected var data = createListData()
        private set
    private var mSearchSetting = SearchSettingsDialogFragment.createForumSearchSettings()


    private var mAdapter: ForumsAdapter? = null
    private var m_ForumId: String? = null

    internal var lastImageDownload = MainActivity.getPreferences().getBoolean("forum.list.show_images", true)

    private val loaderId: Int
        get() = ForumLoader.ID

    private var m_Title: String? = null
    private var m_Name: String? = null
    private var m_NeedLogin: Boolean? = false

    private val mHandler = Handler()

    override fun closeTab(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeArrow()
        if (arguments != null)
            m_ForumId = arguments!!.getString(FORUM_ID_KEY, null)
        if (savedInstanceState != null) {
            m_Name = savedInstanceState.getString(NAME_KEY, m_Name)
            m_Title = savedInstanceState.getString(TITLE_KEY, m_Title)
            m_NeedLogin = savedInstanceState.getBoolean(NEED_LOGIN_KEY, m_NeedLogin!!)

            if (savedInstanceState.containsKey(DATA_KEY)) {
                data = savedInstanceState.getSerializable(DATA_KEY) as ForumFragment.ForumBranch
            }

        }
        if (m_ForumId == null) {
            m_ForumId = Preferences.List.getStartForumId()
        }
        setTitle(m_Title)
        initAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
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
                            .onPositive { _, _ -> context?.let { _context -> UpdateForumStructTask(_context).execute() } }
                            .negativeText(R.string.cancel).show()
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
        removeArrow()
        MainActivity.searchSettings = mSearchSetting

        if (lastImageDownload == MainActivity.getPreferences().getBoolean("forum.list.show_images", true)) {
            mAdapter!!.notifyDataSetChangedWithLayout()
            if (listView != null) listView!!.refreshDrawableState()
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
                .onPositive { dialog, which ->
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

    protected fun createListData(): ForumFragment.ForumBranch {
        return ForumFragment.ForumBranch()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        listView!!.layoutManager = mLayoutManager
        if (savedInstanceState != null && savedInstanceState.containsKey(SCROLL_POSITION_KEY)) {
            listView!!.scrollToPosition(savedInstanceState.getInt(SCROLL_POSITION_KEY))
        }
        setListViewAdapter()
        if (data.items.size == 0)
            reloadData()
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.forum_fragment, container, false)
        assert(view != null)
        listView = findViewById(android.R.id.list) as RecyclerView

        registerForContextMenu(listView!!)
        mEmptyTextView = findViewById(android.R.id.empty) as TextView?


        return view
    }

    fun reloadData() {
        loadData(true)
    }

    override fun startLoad() {
        reloadData()
    }

    override fun loadData(isRefresh: Boolean) {
        loadForum(m_ForumId)
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


    override fun onCreateLoader(id: Int, args: Bundle): Loader<ForumFragment.ForumBranch>? {
        var loader: Loader<ForumBranch>? = null
        if (id == loaderId) {
            setLoading(true)
            loader = createLoader(id, args)

        }
        return loader
    }

    private fun createLoader(id: Int, args: Bundle): Loader<ForumBranch>? {
        var loader: ForumLoader? = null
        if (id == ForumLoader.ID) {
            setLoading(true)
            context?.let {
                loader = ForumLoader(it, args)
            }


        }
        return loader
    }

    override fun onLoadFinished(loader: Loader<ForumFragment.ForumBranch>, data: ForumFragment.ForumBranch?) {
        if (data?.error != null) {
            AppLog.e(activity, data.error)
        } else if (data != null) {
            if (data.items.size == 0 && data.items.size > 1) {
                val forum = data.items[data.items.size - 1]
                ForumTopicsListFragment.showForumTopicsList(activity, forum.id, forum.title)
                return
            }
            this.data.items.clear()
            this.data.items.addAll(data.items)
            this.data.crumbs.clear()
            this.data.crumbs.addAll(data.crumbs)

            notifyDataSetChanged()
            listView!!.refreshDrawableState()
            listView!!.scrollToPosition(0)
        }

        setLoading(false)
    }

    override fun onLoaderReset(loader: Loader<ForumFragment.ForumBranch>) {

    }

    protected fun setEmptyText(s: String) {
        mEmptyTextView?.text = s
    }

    protected fun setLoading(loading: Boolean?) {
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
        outState.putString(NAME_KEY, m_Name)
        outState.putString(TITLE_KEY, m_Title)
        outState.putBoolean(NEED_LOGIN_KEY, m_NeedLogin!!)
        outState.putString(FORUM_ID_KEY, m_ForumId)
        outState.putSerializable(DATA_KEY, data)
        try {
            if (listView != null) {
                outState.putInt(SCROLL_POSITION_KEY,
                        (listView!!.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition())
            }
        } catch (ex: Throwable) {
            AppLog.e(ex)
        }

    }

    protected fun notifyDataSetChanged() {
        mAdapter!!.notifyDataSetChanged()
    }

    protected fun setListViewAdapter() {
        listView!!.adapter = mAdapter
    }

    protected fun initAdapter() {
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
                    ForumTopicsListFragment.showForumTopicsList(activity, forum.id, forum.title)
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
                ForumTopicsListFragment.showForumTopicsList(activity, forum.id, forum.title)
            }


        }, object : ForumsAdapter.OnLongClickListener {
            private fun show(id: String) {
                ExtUrl.showSelectActionDialog(mainActivity, getString(R.string.link), "http://4pda.ru/forum/index.php?showforum=$id")
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
        return m_Title
    }

    /**
     * Уникальный идентификатор списка
     */
    override fun getListName(): String? {
        return m_Name
    }

    override fun onBackPressed(): Boolean {
        if (data.crumbs.size > 1) {
            loadForum(data.crumbs[data.crumbs.size - 2].id)
            return true
        }
        return false
    }

    fun setBrickInfo(listTemplate: BrickInfo): Fragment {
        m_Title = listTemplate.title
        m_Name = listTemplate.name
        m_NeedLogin = listTemplate.needLogin
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

    private class ForumsAdapter// Provide a suitable constructor (depends on the kind of dataset)
    internal constructor(private val mHeaderset: List<Forum>, private val mDataset: List<Forum>,
                         private val mOnClickListener: OnClickListener, private val mOnLongClickListener: OnLongClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val HEADER_VIEW_TYPE = 0
        private val HEADER_CURRENT_VIEW_TYPE = 1
        private val HEADER_CURRENT_NOTOPICS_VIEW_TYPE = 2
        private val DATA_VIEW_TYPE = 3


        private val mIsShowImages: Boolean? = Preferences.Forums.isShowImages()

        private val viewHolders = HashMap<Int, RecyclerView.ViewHolder>()

        interface OnClickListener {
            fun onItemClick(v: View)

            fun onHeaderClick(v: View)

            fun onHeaderTopicsClick(v: View)

        }

        interface OnLongClickListener {
            fun onItemClick(v: View)

            fun onHeaderClick(v: View)

            fun onHeaderTopicsClick(v: View)

        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            // each data item is just a string in this case
            internal var mText1: TextView = v.findViewById(android.R.id.text1)
            internal var mText2: TextView = v.findViewById(android.R.id.text2)
            internal var mImageView: ImageView = v.findViewById(R.id.imageView3)

        }

        internal class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            // each data item is just a string in this case
            var mText: TextView = v.findViewById(R.id.textView3)
        }


        private fun getItem(position: Int): Forum? {
            when (getItemViewType(position)) {
                HEADER_CURRENT_NOTOPICS_VIEW_TYPE, HEADER_CURRENT_VIEW_TYPE, HEADER_VIEW_TYPE -> return mHeaderset[position]
                DATA_VIEW_TYPE -> return mDataset[position - mHeaderset.size]
            }
            return null
        }

        override fun getItemViewType(position: Int): Int {
            // Just as an example, return 0 or 2 depending on position
            // Note that unlike in ListView adapters, types don't have to be contiguous
            return if (position < mHeaderset.size) {
                if (position == mHeaderset.size - 1) {
                    if (!mHeaderset[position].isHasTopics) HEADER_CURRENT_NOTOPICS_VIEW_TYPE else HEADER_CURRENT_VIEW_TYPE
                } else HEADER_VIEW_TYPE
            } else DATA_VIEW_TYPE
        }


        internal fun notifyDataSetChangedWithLayout() {
            // mIsShowImages = Preferences.Forums.isShowImages();
            notifyDataSetChanged()
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): RecyclerView.ViewHolder? {
            //if (!viewHolders.containsKey(viewType)) {

                return when (viewType) {
                            DATA_VIEW_TYPE -> {
                                val v = LayoutInflater.from(parent.context)
                                        .inflate(R.layout.forum_item, parent, false)

                                val viewHolder = ViewHolder(v)
                                if (mIsShowImages != true)
                                    viewHolder.mImageView.visibility = View.GONE
                                v.setOnClickListener { v1 -> mOnClickListener.onItemClick(v1) }
                                v.setOnLongClickListener { v12 ->
                                    mOnLongClickListener.onItemClick(v12)
                                    true
                                }
                                viewHolder
                            }
                            HEADER_VIEW_TYPE -> {
                                val headerV = LayoutInflater.from(parent.context)
                                        .inflate(R.layout.forum_header_item, parent, false)


                                val headerViewHolder = HeaderViewHolder(headerV)
                                headerV.setOnClickListener { v13 -> mOnClickListener.onHeaderClick(v13) }
                                headerV.setOnLongClickListener { v14 ->
                                    mOnLongClickListener.onHeaderClick(v14)
                                    true
                                }
                                headerViewHolder
                            }
                            HEADER_CURRENT_VIEW_TYPE -> {
                                val headerCV = LayoutInflater.from(parent.context)
                                        .inflate(R.layout.forum_header_current_item, parent, false)


                                val headerCViewHolder = HeaderViewHolder(headerCV)
                                headerCV.setOnClickListener { v15 -> mOnClickListener.onHeaderTopicsClick(v15) }
                                headerCV.setOnLongClickListener { v16 ->
                                    mOnLongClickListener.onHeaderTopicsClick(v16)
                                    true
                                }

                                headerCViewHolder
                            }
                            HEADER_CURRENT_NOTOPICS_VIEW_TYPE -> {
                                val headerCNV = LayoutInflater.from(parent.context)
                                        .inflate(R.layout.forum_header_notopics_item, parent, false)


                                val headerCNViewHolder = HeaderViewHolder(headerCNV)
                                headerCNV.setOnClickListener { v17 -> mOnClickListener.onHeaderClick(v17) }
                                headerCNV.setOnLongClickListener { v18 ->
                                    mOnLongClickListener.onHeaderClick(v18)
                                    false
                                }

                                headerCNViewHolder
                            }
                            else -> throw IllegalArgumentException()
                        }
//                viewHolders[viewType] = item
//            }
//            return viewHolders[viewType]
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewType = getItemViewType(position)
            val forum = getItem(position)!!
            when (viewType) {
                DATA_VIEW_TYPE -> {
                    val viewHolder = holder as ViewHolder

                    viewHolder.mText1.text = forum.title
                    viewHolder.mText2.text = forum.description

                    if (forum.iconUrl != null && mIsShowImages!!) {
                        ImageLoader.getInstance().displayImage(forum.iconUrl,
                                holder.mImageView,
                                object : ImageLoadingListener {

                                    override fun onLoadingStarted(p1: String, p2: View) {
                                        p2.visibility = View.INVISIBLE
                                        //holder.mProgressBar.setVisibility(View.VISIBLE);
                                    }

                                    override fun onLoadingFailed(p1: String, p2: View, p3: FailReason) {
                                        // holder.mProgressBar.setVisibility(View.INVISIBLE);
                                    }

                                    override fun onLoadingComplete(p1: String, p2: View, p3: Bitmap) {
                                        p2.visibility = View.VISIBLE
                                        // holder.mProgressBar.setVisibility(View.INVISIBLE);
                                    }

                                    override fun onLoadingCancelled(p1: String, p2: View) {

                                    }
                                })
                    }
                }
                HEADER_VIEW_TYPE -> {
                    val headerViewHolder = holder as HeaderViewHolder
                    headerViewHolder.mText.text = forum.title
                }
                HEADER_CURRENT_VIEW_TYPE -> {
                    val headerCViewHolder = holder as HeaderViewHolder
                    headerCViewHolder.mText.text = forum.title
                }
                HEADER_CURRENT_NOTOPICS_VIEW_TYPE -> {
                    val headerCNViewHolder = holder as HeaderViewHolder
                    headerCNViewHolder.mText.text = forum.title
                }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount(): Int {
            return mHeaderset.size + mDataset.size
        }
    }

    private class ForumLoader internal constructor(context: Context, val args: Bundle) : AsyncTaskLoader<ForumFragment.ForumBranch>(context) {
        internal var mApps: ForumFragment.ForumBranch? = null


        override fun loadInBackground(): ForumFragment.ForumBranch? {
            return try {
                ForumsTable.getForums(args.getString(FORUM_ID_KEY))
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

    private inner class UpdateForumStructTask internal constructor(context: Context) : AsyncTask<String, String, ForumsData>() {

        private val dialog: MaterialDialog = MaterialDialog.Builder(context)
                .progress(true, 0)
                .cancelListener { cancel(true) }
                .content(R.string.refreshing_forum_struct)
                .build()

        override fun onCancelled() {
            Toast.makeText(activity, R.string.canceled_refreshing_forum_struct, Toast.LENGTH_SHORT).show()
        }

        override fun doInBackground(vararg forums: String): ForumsData? {

            try {

                if (isCancelled) return null

                val res = ForumsApi.loadForums(Client.getInstance(), object : ProgressState() {
                    override fun update(message: String, percents: Int) {
                        publishProgress(String.format("%s %d", message, percents))
                    }
                })
                publishProgress(App.getContext().getString(R.string.update_base))
                ForumsTable.updateForums(res.items)
                return res
            } catch (e: Throwable) {
                val res = ForumsData()
                res.error = e

                return res
            }

        }

        override fun onProgressUpdate(vararg progress: String) {
            mHandler.post { dialog.setContent(progress[0]) }
        }

        override fun onPreExecute() {
            try {
                this.dialog.show()
            } catch (ex: Exception) {
                AppLog.e(null, ex)
            }

        }


        override fun onPostExecute(data: ForumsData?) {
            try {
                if (this.dialog.isShowing) {
                    this.dialog.dismiss()
                }
            } catch (ex: Exception) {
                AppLog.e(null, ex)
            }

            loadData(true)
            if (data != null) {
                if (data.error != null) {
                    AppLog.e(activity, data.error)
                }
            }
        }
    }

    companion object {
        private const val DATA_KEY = "BrickFragmentListBase.DATA_KEY"
        private const val SCROLL_POSITION_KEY = "SCROLL_POSITION_KEY"
        const val FORUM_ID_KEY = "FORUM_ID_KEY"
        const val FORUM_TITLE_KEY = "FORUM_TITLE_KEY"


        const val NAME_KEY = "NAME_KEY"
        const val TITLE_KEY = "TITLE_KEY"
        const val NEED_LOGIN_KEY = "NEED_LOGIN_KEY"

        fun showActivity(context: Context, forumId: String, topicId: String) {
            val args = Bundle()
            if (!TextUtils.isEmpty(forumId))
                args.putString(ForumFragment.FORUM_ID_KEY, forumId)
            if (!TextUtils.isEmpty(topicId))
                args.putString(TopicsListFragment.KEY_TOPIC_ID, topicId)
            MainActivity.showListFragment(forumId + topicId, ForumBrickInfo().name, args)
        }
    }
}
