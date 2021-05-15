package org.softeg.slartus.forpdaplus.listfragments

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.TopicApi
import org.softeg.slartus.forpdaapi.post.PostAttach
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.MenuListDialog
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.download.DownloadsService
import org.softeg.slartus.forpdaplus.listtemplates.TopicAttachmentBrickInfo
import org.softeg.slartus.hosthelper.HostHelper
import java.util.*

/*
 * Created by slinkin on 05.05.2014.
 */
class TopicAttachmentListFragment : BaseTaskListFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    @Throws(Throwable::class)
    override fun inBackground(isRefresh: Boolean): Boolean {
        mLoadResultList = TopicApi.getTopicAttachment(Client.getInstance(), args.getString(TOPIC_ID_KEY))
        return true
    }

    override fun deliveryResult(isRefresh: Boolean) {
        if (isRefresh) mData.clear()
        val ids: MutableList<CharSequence> = ArrayList()
        for (item in mData) {
            ids.add(item.id)
        }
        for (item in mLoadResultList) {
            if (ids.contains(item.id)) continue
            mData.add(item)
        }
        mLoadResultList.clear()
    }

    override fun onItemClick(adapterView: AdapterView<*>, v: View, position: Int, id: Long) {
        activity?.openContextMenu(v)
    }
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        if (info.id == -1L) return
        val o = adapter!!.getItem(info.id.toInt()) ?: return
        val item = o as IListItem
        if (TextUtils.isEmpty(item.id)) return
        val attach = item as PostAttach
        val list: MutableList<MenuListDialog> = ArrayList()
        list.add(MenuListDialog(getString(R.string.do_download), Runnable { DownloadsService.download(activity, attach.url.toString().replaceFirst("//${HostHelper.host}".toRegex(), "").trim { it <= ' ' }, false) }))
        list.add(MenuListDialog(getString(R.string.jump_to_page), Runnable { IntentActivity.showTopic(attach.postUrl) }))
        ExtUrl.showContextDialog(context, null, list)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.topic_attachments, menu)
    }

    private fun changeOrder() {
        mData.reverse()
        afterDeliveryResult()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_order_item -> {
                changeOrder()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun showActivity(topicId: CharSequence) {
            val args = Bundle()
            args.putString(TOPIC_ID_KEY, topicId.toString())
            MainActivity.showListFragment(topicId.toString(), TopicAttachmentBrickInfo.NAME, args)
        }

        const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    }
}