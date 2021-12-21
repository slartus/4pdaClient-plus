package org.softeg.slartus.forpdaplus.listfragments

/*
 * Created by slinkin on 10.04.2014.
 */

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.AppTheme
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.listfragments.adapters.ListAdapter
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class BaseListFragment : BaseBrickFragment(), AdapterView.OnItemClickListener {
    protected var mData = ArrayList<IListItem>()

    protected var mHandler = Handler()
    private var _firstVisibleRow = 0
    private var _top = 0

    private val listViewHeader: View?
        get() = null

    protected var listView: ListView? = null
        private set
    private var mEmptyTextView: TextView? = null

    protected open val viewId: Int
        get() = R.layout.list_fragment

    var adapter: BaseAdapter? = null
        protected set
    protected var mListViewLoadMoreFooter: ListViewLoadMoreFooter? = null

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private val notifyAdapter = Runnable { adapter?.notifyDataSetChanged() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {

            _firstVisibleRow = savedInstanceState.getInt(FIRST_VISIBLE_ROW_KEY, _firstVisibleRow)
            _top = savedInstanceState.getInt(TOP_KEY, _top)
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(viewId, container, false)
        assert(view != null)
        listView = findViewById(android.R.id.list) as ListView
        listView!!.onItemClickListener = this
        val header = listViewHeader
        if (header != null)
            listView!!.addHeaderView(header)
        mEmptyTextView = findViewById(android.R.id.empty) as TextView
        listView!!.emptyView = mEmptyTextView
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (args != null)
            outState.putAll(args)

        outState.putInt(FIRST_VISIBLE_ROW_KEY, _firstVisibleRow)
        outState.putInt(TOP_KEY, _top)
        super.onSaveInstanceState(outState)
    }

    open fun setCount() {
        val count = adapter!!.count
        mListViewLoadMoreFooter?.setCount(count, count)
        mListViewLoadMoreFooter?.setState(ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED)
    }

    override fun getContext(): Context? {
        return activity
    }

    protected fun saveListViewScrollPosition() {
        if (listView == null)
            return
        _firstVisibleRow = listView!!.firstVisiblePosition
        val v = listView!!.getChildAt(0)
        _top = v?.top ?: 0
    }

    protected fun restoreListViewScrollPosition() {
        listView!!.setSelectionFromTop(_firstVisibleRow, _top)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (needLogin() == true) {
            val coroutineExceptionHandler =
                CoroutineExceptionHandler { _, throwable ->
                    AppLog.e(throwable)
                }
            lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                UserInfoRepositoryImpl.instance.userInfo
                    .distinctUntilChanged()
                    .collect {
                        if (it.logined) {
                            withContext(Dispatchers.Main) {
                                loadData(true)
                            }
                        }
                    }
            }
        }
        mListViewLoadMoreFooter = ListViewLoadMoreFooter(view.context, listView!!)
        mListViewLoadMoreFooter?.setOnLoadMoreClickListener {
            mListViewLoadMoreFooter?.setState(ListViewLoadMoreFooter.STATE_LOADING)
            loadData(false)
        }


        mSwipeRefreshLayout = createSwipeRefreshLayout(view)
    }

    private fun createSwipeRefreshLayout(view: View): SwipeRefreshLayout {
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.ptr_layout)
        swipeRefreshLayout.setOnRefreshListener { loadData(true) }
        swipeRefreshLayout.setColorSchemeResources(AppTheme.mainAccentColor)
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppTheme.swipeRefreshBackground)
        return swipeRefreshLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        registerForContextMenu(listView!!)
        setListShown(false)
        adapter = createAdapter()


        setListAdapter(adapter)
    }

    private fun setListAdapter(mAdapter: BaseAdapter?) {
        listView?.adapter = mAdapter
    }

    protected fun setListShown(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        //mListView.setVisibility(b?);
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null)
            mHandler.postDelayed(notifyAdapter, 300)

    }

    protected open fun createAdapter(): BaseAdapter {
        return ListAdapter(
            activity!!,
            mData,
            GeneralFragment.getPreferences().getBoolean("showSubMain", false)
        )
    }

    protected fun setLoading(loading: Boolean?) {
        try {
            if (activity == null) return
            //mSwipeRefreshLayout.setRefreshing(loading);
            mSwipeRefreshLayout?.post { mSwipeRefreshLayout?.isRefreshing = loading!! }
            if (loading!!) {
                setEmptyText(App.getContext().getString(R.string.loading))
            } else {
                setEmptyText(App.getContext().getString(R.string.no_data))
            }
        } catch (ignore: Throwable) {
            android.util.Log.e("TAG", ignore.toString())
        }

    }

    protected fun setEmptyText(s: String) {
        mEmptyTextView!!.text = s
    }

    override fun onItemClick(adapterView: AdapterView<*>, v: View, position: Int, id: Long) {

    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!Preferences.Lists.scrollByButtons)
            return false

        val action = event.action

        val scrollView = listView
        val visibleItemsCount = scrollView!!.lastVisiblePosition - scrollView.firstVisiblePosition

        val keyCode = event.keyCode
        if (Preferences.System.isScrollUpButton(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN)
                scrollView.setSelection(max(scrollView.firstVisiblePosition - visibleItemsCount, 0))
            return true// true надо обязательно возвращать даже если не ACTION_DOWN иначе звук нажатия
        }
        if (Preferences.System.isScrollDownButton(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN)
                scrollView.setSelection(min(scrollView.lastVisiblePosition, scrollView.count - 1))
            return true// true надо обязательно возвращать даже если не ACTION_DOWN иначе звук нажатия
        }

        return false
    }

    companion object {

        const val FIRST_VISIBLE_ROW_KEY = "FIRST_VISIBLE_ROW_KEY"
        const val TOP_KEY = "TOP_KEY"
    }
}
