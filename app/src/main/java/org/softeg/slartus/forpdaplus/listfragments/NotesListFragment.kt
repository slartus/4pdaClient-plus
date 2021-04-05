package org.softeg.slartus.forpdaplus.listfragments

import android.app.Activity
import android.text.TextUtils
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.Topic
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.MenuListDialog
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.NotesTable
import org.softeg.slartus.forpdaplus.fragments.NoteFragment
import org.softeg.slartus.forpdaplus.repositories.NotesRepository
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge
import java.io.IOException
import java.text.ParseException
import java.util.*

/*
* Created by slinkin on 21.03.14.
*/
class NotesListFragment : BaseListFragment() {
    //    @Override
    //    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
    //        return NotesRepository.getInstance().
    //                NotesTable.getNotes(args!=null?args.getString(TOPIC_ID_KEY):null);
    //    }
    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun loadData(isRefresh: Boolean) {
        super.loadData(isRefresh)
        loadData()
    }

    override fun onPause() {
        super.onPause()
        dataSubscriber?.dispose()
    }

    private fun loadData(){
        dataSubscriber?.dispose()
        dataSubscriber =
                NotesRepository.instance
                        .notesSubject
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { notes ->
                                    setLoading(false)
                                    mData.clear()
                                    val topicId = args?.getString(TOPIC_ID_KEY)
                                    notes
                                            .filter { topicId == null || it.TopicId == topicId }
                                            .map { note ->
                                                val createDate = note.Date

                                                val topic = Topic(note.Id, note.Title)
                                                topic.description = note.Body
                                                topic.lastMessageAuthor = note.User
                                                topic.lastMessageDate = createDate
                                                topic
                                            }
                                            .forEach {
                                                mData.add(it)
                                            }
                                    adapter?.notifyDataSetChanged()
                                    setCount()
                                },
                                {
                                    setLoading(false)
                                    AppLog.e(it)
                                }
                        )
    }

    var dataSubscriber: Disposable? = null

    override fun onItemClick(adapterView: AdapterView<*>, v: View, position: Int, id: Long) {
        var id = id
        if (!v.hasWindowFocus()) return
        id = ListViewMethodsBridge.getItemId(activity, position, id).toLong()
        if (id < 0 || adapter!!.count <= id) return
        val o = adapter!!.getItem(id.toInt()) ?: return
        val topic = o as IListItem
        if (TextUtils.isEmpty(topic.id)) return
        try {
            val note = NotesTable.getNote(topic.id.toString())
            if (note != null) {
                if (note.Url != null) {
                    IntentActivity.tryShowUrl(context as Activity?, mHandler, note.Url, true, false, null)
                } else {
                    NoteFragment.showNote(topic.id.toString())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        if (info.id == -1L) return
        if (info.id < 0 || adapter!!.count <= info.id) return
        val o = adapter!!.getItem(info.id.toInt()) ?: return
        val topic = o as IListItem
        val list: MutableList<MenuListDialog> = ArrayList()
        AddLinksSubMenu(list, topic)
        list.add(MenuListDialog(App.getContext().getString(R.string.delete)) {
            MaterialDialog.Builder(context!!)
                    .title(R.string.confirm_action)
                    .content(R.string.ask_delete_note)
                    .cancelable(true)
                    .negativeText(R.string.cancel)
                    .positiveText(R.string.delete)
                    .onPositive { dialog: MaterialDialog?, which: DialogAction? ->
                        try {
                            NotesRepository.instance.delete(topic.id.toString())
                        } catch (ex: Throwable) {
                            AppLog.e(context, ex)
                        }
                    }
                    .show()
        })
        ExtUrl.showContextDialog(context, null, list)
    }

    private fun AddLinksSubMenu(list: MutableList<MenuListDialog>, topic: IListItem) {
        try {
            val note = NotesTable.getNote(topic.id.toString())
            if (note != null) {
                val links = note.links
                if (links.size != 0) {
                    list.add(MenuListDialog(App.getContext().getString(R.string.links)) {
                        val list1: MutableList<MenuListDialog> = ArrayList()
                        for (pair in links) {
                            list1.add(MenuListDialog(pair.first.toString()) { IntentActivity.tryShowUrl(context as Activity?, mHandler, pair.second.toString(), true, false, null) })
                        }
                        ExtUrl.showContextDialog(context, null, list1)
                    })
                }
            }
        } catch (e: Throwable) {
            AppLog.e(context, e)
        }
    }

    companion object {
        const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    }
}