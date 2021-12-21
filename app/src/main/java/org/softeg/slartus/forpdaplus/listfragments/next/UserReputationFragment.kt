package org.softeg.slartus.forpdaplus.listfragments.next

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import org.jsoup.Jsoup
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.ListInfo
import org.softeg.slartus.forpdaapi.ReputationEvent
import org.softeg.slartus.forpdaapi.ReputationsApi.loadReputation
import org.softeg.slartus.forpdaapi.classes.ListData
import org.softeg.slartus.forpdaapi.classes.ReputationsListData
import org.softeg.slartus.forpdaplus.*
import org.softeg.slartus.forpdaplus.classes.ForumUser
import org.softeg.slartus.forpdaplus.classes.MenuListDialog
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import org.softeg.slartus.forpdaplus.listtemplates.UserReputationBrickInfo
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl.Companion.instance
import org.softeg.slartus.hosthelper.HostHelper
import java.io.IOException
import java.util.*

/*
 * Created by slinkin on 19.02.2015.
 */
class UserReputationFragment : BrickFragmentListBase() {
    override fun closeTab(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    private val userId: String
        get() = Args.getString(USER_ID_KEY)?:""

    private val userNick: String
        get() = Args.getString(USER_NICK_KEY, "")

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(USER_ID_KEY, userId)
        outState.putString(USER_NICK_KEY, userNick)
        super.onSaveInstanceState(outState)
    }

    override fun getLoaderId(): Int {
        return ItemsLoader.ID
    }

    override fun createAdapter(): BaseAdapter {
        return ListAdapter(activity, data.items)
    }

    override fun getViewResourceId(): Int {
        return R.layout.list_fragment
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        activity!!.openContextMenu(view)
    }

    override fun onLoadFinished(loader: Loader<ListData>, data: ListData) {
        super.onLoadFinished(loader, data)
        if (data.ex == null) {
            if (data is ReputationsListData) {
                if (supportActionBar != null) setSubtitle(data.rep)
                Args.putString(USER_NICK_KEY, data.user)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        addLoadMoreFooter(inflater.context)
        return v
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        if (info.id == -1L) return
        val item = adapter.getItem(info.id.toInt()) as ReputationEvent
        val list: MutableList<MenuListDialog> = ArrayList()
        if (item.sourceUrl != null && !item.sourceUrl.contains("forum/index.php?showuser=")) {
            list.add(MenuListDialog(getString(R.string.jump_to_page), Runnable { IntentActivity.tryShowUrl(activity, Handler(), item.sourceUrl, true, false) }))
        }
        ForumUser.onCreateContextMenu(activity, list, item.userId, item.user)
        ExtUrl.showContextDialog(context, item.user, list)
    }

    override fun getLoadArgs(): Bundle {
        val args = Args
        args.putInt(START_KEY, data.items.size)
        return args
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.reputation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile_rep_item -> {
                ProfileFragment.showProfile(userId, userNick)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val loginedAndNotSelf = Client.getInstance().logined && userId != instance.getId()
    }

    override fun createLoader(id: Int, args: Bundle): AsyncTaskLoader<ListData> {
        var loader: ItemsLoader? = null
        if (id == ItemsLoader.ID) {
            setLoading(true)
            loader = ItemsLoader(activity, args)
        }
        return loader!!
    }

    private class ItemsLoader(context: Context?, val args: Bundle) : AsyncTaskLoader<ListData>(context!!) {
        var mApps: ListData? = null

        /**
         * Загрузка ссылки на изображение, которое является плюсовой репой
         */
        @Throws(IOException::class)
        private fun loadRepImage() {
            val body = Client.getInstance().performGet("https://${HostHelper.host}/forum/index.php?act=rep&view=history&mid=236113&mode=to&order=asc").responseBody
            var el = Jsoup
                    .parse(body)
                    .select("td.row1>img")
                    .first()
            if (el != null) {
                val plusImage = el.attr("src")
                if (plusImage != null) {
                    GeneralFragment.getPreferences().edit()
                            .putString("repPlusImage", plusImage)
                            .putBoolean("needLoadRepImage", false)
                            .apply()
                    return
                }
            }
            if (el != null) el = el.select("tr:nth-last-child(2) td img").first()
            if (el != null) {
                val plusImage = el.attr("src")
                if (plusImage != null) GeneralFragment.getPreferences().edit()
                        .putString("repPlusImage", plusImage)
                        .putBoolean("needLoadRepImage", false)
                        .apply()
            }
        }

        override fun loadInBackground(): ListData {
            return try {
                loadRepImage()
                val listInfo = ListInfo()
                listInfo.from = if (args.getBoolean(IS_REFRESH_KEY)) 0 else args.getInt(START_KEY)
                loadReputation(Client.getInstance(),
                        args.getString(USER_ID_KEY),
                        args.getBoolean(USER_FROM_KEY), listInfo,
                        GeneralFragment.getPreferences().getString("repPlusImage", "https://s.4pda.to/ShmfPSURw3VD2aNlTerb3hvYwGCMxd4z0muJ.gif"))
            } catch (e: Throwable) {
                val forumPage = ListData()
                forumPage.ex = e
                forumPage
            }
        }

        override fun deliverResult(apps: ListData?) {
            mApps = apps
            if (isStarted) {
                super.deliverResult(apps)
            }
        }

        override fun onStartLoading() {
            if (mApps != null) { // If we currently have a result available, deliver it
// immediately.
                deliverResult(mApps)
            }
            if (takeContentChanged() || mApps == null) { // If the data has changed since the last time it was loaded
// or is not currently available, start a load.
                forceLoad()
            }
        }

        override fun onStopLoading() { // Attempt to cancel the current load task if possible.
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
            val ID = App.getInstance().uniqueIntValue
        }

    }

    private class ListAdapter(context: Context?, data: ArrayList<out IListItem>) : BaseAdapter() {
        private val mInflater: LayoutInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private var mData: ArrayList<out IListItem> = data


        override fun getCount(): Int {
            return mData.size
        }

        override fun getItem(i: Int): Any {
            return mData[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(position: Int, v: View?, parent: ViewGroup): View? {
            var view = v
            val holder: ViewHolder
            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_reputation, parent, false)
                holder = ViewHolder()
                holder.flag = view.findViewById(R.id.imgFlag)
                holder.topLeft = view.findViewById(R.id.txtTopLeft)
                holder.topRight = view.findViewById(R.id.txtTopRight)
                holder.main = view.findViewById(R.id.txtMain)
                holder.subMain = view.findViewById(R.id.txtSubMain)
                holder.progress = view.findViewById(R.id.progressBar)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }
            val topic = mData[position]
            holder.topLeft!!.text = topic.topLeft
            holder.topRight!!.text = topic.topRight
            holder.main!!.text = topic.main
            holder.subMain!!.text = topic.subMain
            setVisibility(holder.progress, if (topic.isInProgress) View.VISIBLE else View.INVISIBLE)
            when (topic.state) {
                IListItem.STATE_GREEN -> {
                    setVisibility(holder.flag, View.VISIBLE)
                    holder.flag!!.text = "+"
                    holder.flag!!.setBackgroundResource(R.drawable.plusrep)
                }
                IListItem.STATE_RED -> {
                    setVisibility(holder.flag, View.VISIBLE)
                    holder.flag!!.setBackgroundResource(R.drawable.minusrep)
                    holder.flag!!.text = "-"
                }
                else -> setVisibility(holder.flag, View.INVISIBLE)
            }
            return view
        }

        private fun setVisibility(v: View?, visibility: Int) {
            if (v!!.visibility != visibility) v.visibility = visibility
        }

        inner class ViewHolder {
            var flag: TextView? = null
            var progress: View? = null
            var topLeft: TextView? = null
            var topRight: TextView? = null
            var main: TextView? = null
            var subMain: TextView? = null
        }

    }

    @JvmOverloads
    fun plusRep(uId: String? = userId, uNick: String? = userNick) {
        plusRep(activity, Handler(), "0", uId, uNick)
    }

    @JvmOverloads
    fun minusRep(uId: String? = userId, uNick: String? = userNick) {
        minusRep(activity, Handler(), "0", uId, uNick)
    }

    companion object {
        const val USER_ID_KEY = "USER_ID_KEY"
        const val USER_NICK_KEY = "USER_NICK_KEY"
        const val USER_FROM_KEY = "USER_FROM_KEY"
        @JvmStatic
        fun showActivity(userId: CharSequence, from: Boolean) {
            val args = Bundle()
            args.putString(USER_ID_KEY, userId.toString())
            if (from) args.putBoolean(USER_FROM_KEY, true)
            MainActivity.showListFragment(userId.toString(), UserReputationBrickInfo.NAME, args)
        }

        private const val START_KEY = "START_KEY"

        @JvmStatic
        fun plusRep(activity: Activity?, handler: Handler?, userId: String?, userNick: String?) {
            plusRep(activity, handler, "0", userId, userNick)
        }

        @JvmStatic
        fun minusRep(activity: Activity?, handler: Handler?, userId: String?, userNick: String?) {
            minusRep(activity, handler, "0", userId, userNick)
        }

        @JvmStatic
        fun plusRep(activity: Activity?, handler: Handler?, postId: String, userId: String?, userNick: String?) {
            showChangeRep(activity, handler, postId, userId, userNick, "add", App.getContext().getString(R.string.increase_reputation))
        }

        @JvmStatic
        fun minusRep(activity: Activity?, handler: Handler?, postId: String, userId: String?, userNick: String?) {
            showChangeRep(activity, handler, postId, userId, userNick, "minus", App.getContext().getString(R.string.decrease_reputation))
        }

        private fun showChangeRep(activity: Activity?, handler: Handler?, postId: String, userId: String?, userNick: String?, type: String, title: String) {
            ForumUser.startChangeRep(activity, handler, userId, userNick, postId, type, title)
        }
    }
}