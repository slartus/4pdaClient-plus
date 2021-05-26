package org.softeg.slartus.forpdaplus.fragments.search

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R

import org.softeg.slartus.forpdaplus.classes.ForumsAdapter
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.repositories.ForumsRepository
import java.util.*

/*
 * Created by slinkin on 24.04.2014.
 */   class ForumsTreeDialogFragment : DialogFragment() {
    private var m_ListView: ListView? = null
    private var m_Spinner: Spinner? = null
    private var m_ListViewAdapter: ForumsAdapter? = null
    private var m_SpinnerAdapter: SpinnerAdapter? = null
    private var m_Progress: View? = null
    private val m_Forums = ArrayList<CheckableForumItem>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.forums_tree_dialog_fragment, null as ViewGroup?)!!
        m_ListView = view.findViewById(android.R.id.list)
        initListView()
        m_Spinner = view.findViewById(R.id.selected_spinner)
        initSpinner()
        m_Progress = view.findViewById(R.id.progress)
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return MaterialDialog.Builder(activity!!)
            .customView(view, false)
            .title(R.string.forum)
            .positiveText(R.string.accept)
            .negativeText(R.string.cancel)
            .onPositive { _, _ ->
                val intent = Intent()
                intent.putExtra(
                    FORUM_IDS_KEY,
                    m_ListViewAdapter!!.checkedIds.toTypedArray()
                )
                targetFragment!!.onActivityResult(
                    SearchSettingsDialogFragment.FORUMS_DIALOG_REQUEST,
                    OK_RESULT,
                    intent
                )
            }
            .onNegative { _, _ ->
                targetFragment!!.onActivityResult(
                    SearchSettingsDialogFragment.FORUMS_DIALOG_REQUEST,
                    CANCEL_RESULT,
                    null
                )
            }
            .build()
    }

    private fun initListView() {
        m_ListView!!.isFastScrollEnabled = true
        m_ListViewAdapter = ForumsAdapter(
            activity,
            m_Forums
        )
        m_ListView!!.adapter = m_ListViewAdapter
        m_ListView!!.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            m_ListViewAdapter!!.toggleChecked(position)
            m_SpinnerAdapter!!.notifyDataSetChanged()
        }
    }

    private fun initSpinner() {
        m_SpinnerAdapter = SpinnerAdapter(
            activity, m_Forums
        )
        m_Spinner!!.adapter = m_SpinnerAdapter
        m_Spinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                if (l == 0L) return
                val item = m_SpinnerAdapter!!.getItem(l.toInt())
                m_ListView!!.setSelection(m_ListViewAdapter!!.getPosition(item))
                m_Spinner!!.setSelection(0)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private var dataSubscriber: Disposable? = null
    private fun subscribesData() {
        dataSubscriber?.dispose()
        setLoading(true)
        dataSubscriber =
            ForumsRepository.instance
                .forumsSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { setLoading(true) }
                .doAfterTerminate { setLoading(false) }
                .subscribe(
                    { items ->
                        deliveryResult(items)
                        setLoading(false)
                    },
                    {
                        setLoading(false)
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


    private fun deliveryResult(result: List<org.softeg.slartus.forpdaapi.Forum>) {
        m_Forums.clear()
        addForumCaptions(
            m_Forums, result, null, 0, Arrays.asList(
                *arguments!!.getStringArray(
                    FORUM_IDS_KEY
                )
            )
        )
        m_ListViewAdapter!!.notifyDataSetChanged()
        m_SpinnerAdapter!!.notifyDataSetChanged()
    }

    private fun setLoading(b: Boolean) {
        m_Progress!!.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun addForumCaptions(
        forums: ArrayList<CheckableForumItem>,
        forum: List<org.softeg.slartus.forpdaapi.Forum>,
        parentForum: org.softeg.slartus.forpdaapi.Forum?,
        level: Int, checkIds: Collection<String?>
    ) {
        if (parentForum == null) {
            forums.add(CheckableForumItem("all", ">> " + getString(R.string.all_forums)).apply {
                this.level = level
                IsChecked = checkIds.contains(this.Id)
            })
            addForumCaptions(
                forums,
                forum,
                org.softeg.slartus.forpdaapi.Forum(null, ""),
                level + 1,
                checkIds
            )
        } else {
            forum
                .filter { it.parentId == parentForum.id }
                .forEach {
                    forums.add(CheckableForumItem(it.id, it.title).apply {
                        this.level = level
                        IsChecked = checkIds.contains(this.Id)
                    })
                    addForumCaptions(forums, forum, it, level + 1, checkIds)
                }
        }
    }

    inner class SpinnerAdapter(
        context: Context?,
        private val mForums: ArrayList<CheckableForumItem>
    ) : BaseAdapter() {
        private val m_Inflater: LayoutInflater = LayoutInflater.from(context)
        override fun getCount(): Int {
            var c = 1
            for (f in mForums) {
                if (f.IsChecked) c++
            }
            return c
        }

        override fun getItem(i: Int): CheckableForumItem? {
            if (i == 0) {
                return CheckableForumItem("", getString(R.string.total) + ": " + (count - 1))
            }
            var c = 1
            for (f in mForums) {
                if (f.IsChecked && c == i) return f
                if (f.IsChecked) c++
            }
            return null
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val holder: ViewHolder
            var rowView = convertView
            if (rowView == null) {
                rowView = m_Inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null)
                holder = ViewHolder()
                assert(rowView != null)
                holder.text = rowView
                    .findViewById(android.R.id.text1)
                rowView.tag = holder
            } else {
                holder = rowView.tag as ViewHolder
            }
            val item = getItem(position)
            holder.text!!.text = item?.Title
            return rowView
        }

        inner class ViewHolder {
            var text: TextView? = null
        }

    }

    companion object {
        const val IS_DIALOG_KEY = "IS_DIALOG_KEY"
        const val FORUM_IDS_KEY = "FORUM_IDS_KEY"
        const val OK_RESULT = 0
        const val CANCEL_RESULT = 1

        @JvmStatic
        fun newInstance(
            dialog: Boolean?,
            checkedForumIds: Collection<String?>
        ): ForumsTreeDialogFragment {
            val args = Bundle()
            args.putBoolean(IS_DIALOG_KEY, dialog!!)

            args.putStringArray(FORUM_IDS_KEY, checkedForumIds.toTypedArray())
            val fragment = ForumsTreeDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}