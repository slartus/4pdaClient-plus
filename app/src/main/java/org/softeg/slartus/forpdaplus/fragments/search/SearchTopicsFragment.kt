package org.softeg.slartus.forpdaplus.fragments.search

import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.ListInfo
import org.softeg.slartus.forpdaapi.search.SearchApi.getSearchTopicsResult
import org.softeg.slartus.forpdaapi.search.SearchSettings
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import java.io.IOException
import java.net.URISyntaxException
import java.text.ParseException

/**
 * Created by radiationx on 15.11.15.
 */
class SearchTopicsFragment : TopicsListFragment(), ISearchResultView, AbsListView.OnScrollListener {
    override val viewId: Int = R.layout.list_translucent_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = super.onCreateView(inflater, container, savedInstanceState)
        listView?.setOnScrollListener(this)
        return view
    }

    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    override fun loadTopics(client: Client, listInfo: ListInfo?): ArrayList<out IListItem?> {
        return getSearchTopicsResult(client, args.getString(SEARCH_URL_KEY).orEmpty(), mListInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_topics, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.link_item) {
            ExtUrl.showSelectActionDialog(
                mainActivity,
                getString(R.string.link),
                args.getString(SEARCH_URL_KEY).orEmpty()
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun saveCache() {}
    override fun loadCache() {}

    override fun getResultView(): String {
        return SearchSettings.RESULT_VIEW_TOPICS
    }

    override fun search(searchQuery: String) {
        if (args == null) args = Bundle()
        args.putString(SEARCH_URL_KEY, searchQuery)
        loadData(true)
    }

    override fun onScrollStateChanged(absListView: AbsListView, scrollState: Int) {}
    override fun onScroll(
        absListView: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
    }

    override fun onPause() {
        super.onPause()
        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings()
    }

    override fun onResume() {
        super.onResume()
        MainActivity.searchSettings = SearchSettings.parse(args.getString(SEARCH_URL_KEY))
        setTitle(getString(R.string.search))
        setSubtitle(MainActivity.searchSettings.query)
        setArrow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    companion object {
        private const val SEARCH_URL_KEY = "SEARCH_SETTINGS_KEY"

        @JvmStatic
        fun newFragment(searchUrl: CharSequence): SearchTopicsFragment {
            val fragment = SearchTopicsFragment()
            val args = Bundle()
            args.putString(SEARCH_URL_KEY, searchUrl.toString())
            fragment.arguments = args
            return fragment
        }
    }
}