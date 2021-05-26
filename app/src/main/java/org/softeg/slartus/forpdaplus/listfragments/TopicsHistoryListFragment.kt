package org.softeg.slartus.forpdaplus.listfragments

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.ListInfo
import org.softeg.slartus.forpdacommon.sameContentWith
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.MenuListDialog
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.repositories.ForumsRepository
import java.io.IOException
import java.util.*

/*
 * Created by slinkin on 20.03.14.
 */
class TopicsHistoryListFragment : TopicsListFragment() {
    private var dataSubscriber: Disposable? = null
    private fun subscribesData() {
        dataSubscriber?.dispose()

        dataSubscriber =
            ForumsRepository.instance
                .forumsSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        loadData(true)
                    },
                    {
                        AppLog.e(activity, it)
                    }
                )
    }

    override fun onPause() {
        super.onPause()
        dataSubscriber?.dispose()
        MainActivity.searchSettings = SearchSettingsDialogFragment.createDefaultSearchSettings()
    }

    override fun onResume() {
        super.onResume()
        subscribesData()
    }

    @Throws(IOException::class)
    override fun loadTopics(client: Client?, listInfo: ListInfo?): ArrayList<out IListItem?> {
        return TopicsHistoryTable.getTopicsHistory(listInfo)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        try {
            val info = menuInfo as AdapterContextMenuInfo?
            if (info!!.id == -1L) return
            val o = adapter!!.getItem(info.id.toInt()) ?: return
            val topic = o as IListItem
            val list: MutableList<MenuListDialog> = ArrayList()
            list.add(MenuListDialog(getString(R.string.delete_from_visited)) {
                TopicsHistoryTable.delete(topic.id)
                mData.remove(topic)
                adapter!!.notifyDataSetChanged()
            })
            ExtUrl.showContextDialog(context, null, list)
        } catch (ex: Throwable) {
            AppLog.e(ex)
        }
    }
}