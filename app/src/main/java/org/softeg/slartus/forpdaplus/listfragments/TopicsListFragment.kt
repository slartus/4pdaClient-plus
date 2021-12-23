package org.softeg.slartus.forpdaplus.listfragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaapi.*
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.ActionSelectDialogFragment
import org.softeg.slartus.forpdaplus.classes.ActionSelectDialogFragment.execute
import org.softeg.slartus.forpdaplus.classes.ActionSelectDialogFragment.showSaveNavigateActionDialog
import org.softeg.slartus.forpdaplus.classes.MenuListDialog
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter
import org.softeg.slartus.forpdaplus.core_lib.coroutines.AppIOScope
import org.softeg.slartus.forpdaplus.fragments.ForumFragment.Companion.showActivity
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment.Companion.newPostWithAttach
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment.Companion.showActivity
import org.softeg.slartus.forpdaplus.listfragments.adapters.SortedListAdapter
import org.softeg.slartus.forpdaplus.listtemplates.FavoritesBrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.prefs.TopicsListPreferencesActivity
import org.softeg.slartus.forpdaplus.prefs.TopicsPreferenceFragment
import org.softeg.slartus.forpdaplus.repositories.InternetConnection
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepositoryImpl.Companion.instance
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge
import java.io.IOException
import java.net.URISyntaxException
import java.text.ParseException
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class TopicsListFragment : BaseTaskListFragment() {
    @JvmField
    protected var mListInfo = ListInfo()

    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    protected abstract fun loadTopics(client: Client?, listInfo: ListInfo?): ArrayList<out IListItem?>
    override fun loadCache() {
        clearNotification(2)
        super.loadCache()
        sort()
    }

    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    public override fun inBackground(isRefresh: Boolean): Boolean {
        mListInfo = ListInfo()
        mListInfo.from = if (isRefresh) 0 else mData.size
        mLoadResultList = loadTopics(Client.getInstance(), mListInfo)
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
        sort()
    }

    protected open fun sort() {
        Collections.sort(mData, comparator)
    }

    override fun setCount() {
        val count = max(mListInfo.outCount, mData.size)
        val footer = mListViewLoadMoreFooter
        if (footer != null) {
            footer.setCount(mData.size, count)
            footer.setState(
                    if (mData.size == count) ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED else ListViewLoadMoreFooter.STATE_LOAD_MORE
            )
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        try {
            val info = menuInfo as AdapterContextMenuInfo?
            if (info!!.id == -1L) return
            val adapter = adapter
            val context = context
            if (adapter != null && context != null) {
                val o = adapter.getItem(info.id.toInt()) ?: return
                val topic = o as IListItem
                if (TextUtils.isEmpty(topic.id)) return
                val list: MutableList<MenuListDialog> = ArrayList()
                list.add(MenuListDialog(context.getString(R.string.navigate_getfirstpost)) { showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_FIRST_POST, "") })
                list.add(MenuListDialog(getContext()!!.getString(R.string.navigate_getlastpost)) { showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_POST, "view=getlastpost") })
                list.add(MenuListDialog(getContext()!!.getString(R.string.navigate_getnewpost)) { showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_NEW_POST, "view=getnewpost") })
                list.add(MenuListDialog(getContext()!!.getString(R.string.navigate_last_url)) {
                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_URL,
                            TopicUtils.getUrlArgs(topic.id, Topic.NAVIGATE_VIEW_LAST_URL.toString(), "")
                                    ?: "")
                })
                list.add(MenuListDialog(getContext()!!.getString(R.string.notes_by_topic)) {
                    val args = Bundle()
                    args.putString(NotesListFragment.TOPIC_ID_KEY, topic.id.toString())
                    MainActivity.showListFragment(topic.id.toString(), NotesBrickInfo().name, args)
                })
                list.add(MenuListDialog(getString(R.string.link)) { showLinkMenu(getContext(), topic) })
                list.add(MenuListDialog(getString(R.string.options)) { showOptionsMenu(getContext(), mHandler, topic) })
                ExtUrl.showContextDialog(getContext(), null, list)
            }
        } catch (ex: Exception) {
            AppLog.e(this.context, ex)
        }
    }

    private fun showLinkMenu(context: Context?, topic: IListItem) {
        val list: List<MenuListDialog> = ArrayList()
        ExtUrl.addUrlSubMenu(mHandler, context, list,
                TopicUtils.getTopicUrl(topic.id.toString(), TopicUtils.getOpenTopicArgs(topic.id,
                        listName) ?: ""), topic.id.toString(),
                topic.main.toString())
        ExtUrl.showContextDialog(context, getString(R.string.link), list)
    }

    private fun showOptionsMenu(context: Context?, mHandler: Handler?, topic: IListItem) {
        val list: MutableList<MenuListDialog> = ArrayList()
        configureOptionsMenu(context, mHandler, list, topic)
        ExtUrl.showContextDialog(context, getString(R.string.options), list)
    }

    private fun runTopicActionAsync(topic: Topic, actionAsync: () -> String?, onSuccessAction: (() -> Unit)? = null) {
        Toast.makeText(context, R.string.request_sent, Toast.LENGTH_SHORT).show()
        topic.inProgress(true)
        adapter?.notifyDataSetChanged()
        InternetConnection.instance.loadDataOnInternetConnected({
            AppIOScope().launch {
                try {
                    val result = actionAsync()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            App.getContext(),
                            "\"" + topic.title.substring(
                                0,
                                min(10, topic.title.length - 1)
                            ) + "...\": " + result,
                            Toast.LENGTH_SHORT
                        ).show()
                        topic.inProgress(false)
                        onSuccessAction?.let {
                            it()
                        }
                    }
                } catch (ex: Throwable) {
                    withContext(Dispatchers.Main) {
                        AppLog.e(context, ex)
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        topic.inProgress(false)
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun addToFavoritesAsync(topic: Topic, emailType: String?) {
        runTopicActionAsync(topic,
                { TopicApi.changeFavorite(Client.getInstance(), topic.id, emailType) })
    }

    private fun removeFromFavoritesAsync(topic: Topic) {
        runTopicActionAsync(topic,
                { TopicApi.deleteFromFavorites(Client.getInstance(), topic.id) },
                { mData.remove(topic) })
    }

    private fun togglePinAsync(topic: FavTopic) {
        runTopicActionAsync(topic,
                {
                    TopicApi.pinFavorite(Client.getInstance(), topic.id, if (topic.isPinned) TopicApi.TRACK_TYPE_UNPIN else TopicApi.TRACK_TYPE_PIN)
                },
                {
                    topic.isPinned = !topic.isPinned
                }
        )
    }

    private fun configureOptionsMenu(context: Context?, mHandler: Handler?, optionsMenu: MutableList<MenuListDialog>,
                                     listItem: IListItem) {
        val topic: Topic = if (listItem is Topic) {
            listItem
        } else {
            Topic(listItem.id.toString(), listItem.main.toString())
        }
        if (Client.getInstance().logined && !topic.isInProgress) {
            val isFavotitesList = FavoritesBrickInfo.NAME == listName
            val title = if (isFavotitesList) context!!.getString(R.string.change_subscription) else context!!.getString(R.string.add_to_favorites)
            optionsMenu.add(MenuListDialog(title) {
                TopicUtils.showSubscribeSelectTypeDialog(context, mHandler!!, topic) { emailType -> addToFavoritesAsync(topic, emailType) }
            })
            if (isFavotitesList) {
                optionsMenu.add(MenuListDialog(context.getString(R.string.delete_from_favorites)) {
                    removeFromFavoritesAsync(topic)
                })
                val favTopic = topic as FavTopic
                optionsMenu.add(MenuListDialog((if (favTopic.isPinned) context.getString(R.string.unpin) else context.getString(R.string.pin)) + context.getString(R.string.in_favorites_combined)) {
                    togglePinAsync(topic)
                })
            }
            optionsMenu.add(MenuListDialog(context.getString(R.string.open_topics_forum)) { showActivity(topic.forumId, topic.id) })
        }
        optionsMenu.add(MenuListDialog(getString(R.string.attachments)) { showActivity(listItem.id) })
    }

    private fun showTopicActivity(topic: IListItem, args: String) {
        ExtTopic.showActivity(topic.main, topic.id, args)
        //topicAfterClick(topic);
    }

    private fun showSaveNavigateActionDialog(topic: IListItem, selectedAction: CharSequence, params: String) {
        val context = context
        if (context != null) {
            showSaveNavigateActionDialog(context, String.format("%s.navigate_action", listName),
                    selectedAction.toString()
            ) { showTopicActivity(topic, params) }
        }
    }

    /**
     * Извне создание поста
     */
    private fun tryCreatePost(topic: IListItem): Boolean {
        val extras = arguments ?: return false
        if (!extras.containsKey(Intent.EXTRA_STREAM) &&
                !extras.containsKey(Intent.EXTRA_TEXT) &&
                !extras.containsKey(Intent.EXTRA_HTML_TEXT)) return false
        val context = context
        if (context != null) {
            newPostWithAttach(context,
                    null, topic.id.toString(), Client.getInstance().authKey, extras)
            val activity: Activity? = activity
            activity?.finish()
        }
        return true
    }

    override fun onItemClick(adapterView: AdapterView<*>, v: View, position: Int, id: Long) {
        val adapter = adapter
        if (!v.hasWindowFocus() || adapter == null) return
        try {
            val itemId = ListViewMethodsBridge.getItemId(activity, position, id).toLong()
            if (itemId < 0 || adapter.count <= itemId) return
            val o = adapter.getItem(itemId.toInt()) ?: return
            val topic = o as IListItem
            if (TextUtils.isEmpty(topic.id)) return
            if (tryCreatePost(topic)) return
            if (!instance.getLogined()) {
                Toast.makeText(context, "Залогиньтесь для просмотра тем форума!", Toast.LENGTH_LONG).show()
            }
            execute(activity!!,
                    getString(R.string.default_action), String.format("%s.navigate_action", listName),
                    arrayOf(getString(R.string.navigate_getfirstpost), getString(R.string.navigate_getlastpost), getString(R.string.navigate_getnewpost), getString(R.string.navigate_last_url)),
                    arrayOf(Topic.NAVIGATE_VIEW_FIRST_POST, Topic.NAVIGATE_VIEW_LAST_POST, Topic.NAVIGATE_VIEW_NEW_POST, Topic.NAVIGATE_VIEW_LAST_URL),
                    object : ActionSelectDialogFragment.OkListener {
                        override fun execute(value: CharSequence?) {
                            showTopicActivity(topic, TopicUtils.getUrlArgs(topic.id, value.toString(), Topic.NAVIGATE_VIEW_FIRST_POST.toString())
                                    ?: "")
                        }
                    },
                    getString(R.string.default_action_notify)
            )
        } catch (ex: Throwable) {
            AppLog.e(activity, ex)
        }
    }

    fun topicAfterClick(id: String) {
        for (item in mData) {
            if (item.id == id) {
                item.state = IListItem.STATE_NORMAL
                adapter!!.notifyDataSetChanged()
                updateItem(item)
                return
            }
        }
    }

    protected open fun updateItem(topic: IListItem?) {
        saveCache()
    }

    override fun createAdapter(): BaseAdapter {
        return SortedListAdapter(activity, mData, getPreferences().getBoolean("showSubMain", false))
    }

    private val comparator: Comparator<in IListItem>
        get() = Comparator { listItem1: IListItem?, listItem2: IListItem ->
            if (listItem1 !is Topic) return@Comparator 0
            val i: Int
            when (Preferences.List.getListSort(listName, Preferences.List.defaultListSort())) {
                "sortorder.desc" -> return@Comparator compareBySortOrder(listItem1, listItem2 as Topic, -1)
                "sortorder.asc" -> return@Comparator compareBySortOrder(listItem1, listItem2 as Topic, 1)
                "date.desc" -> return@Comparator compareByDate(listItem1, listItem2 as Topic, -1)
                "date_and_new.desc" -> {
                    i = compareByNew(listItem1, listItem2 as Topic, -1)
                    return@Comparator if (i == 0) compareByDate(listItem1, listItem2, -1) else i
                }
                "title.desc" -> return@Comparator compareByTitle(listItem1, listItem2 as Topic, -1)
                "date.asc" -> return@Comparator compareByDate(listItem1, listItem2 as Topic, 1)
                "date_and_new.asc" -> {
                    i = compareByNew(listItem1, listItem2 as Topic, 1)
                    return@Comparator if (i == 0) compareByDate(listItem1, listItem2, 1) else i
                }
                "title.asc" -> return@Comparator compareByTitle(listItem1, listItem2 as Topic, 1)
                else -> return@Comparator compareByDate(listItem1, listItem2 as Topic, -1)
            }
        }

    private fun compareBySortOrder(listItem1: Topic, listItem2: Topic, k: Int): Int {
        if (TextUtils.isEmpty(listItem1.sortOrder) && TextUtils.isEmpty(listItem2.sortOrder)) return 0
        if (TextUtils.isEmpty(listItem1.sortOrder)) return k
        return if (TextUtils.isEmpty(listItem2.sortOrder)) -k else k * listItem1.sortOrder.toString().compareTo(listItem2.sortOrder.toString())
    }

    private fun compareByDate(listItem1: Topic, listItem2: Topic, k: Int): Int {
        if (listItem1.lastMessageDate == null && listItem2.lastMessageDate == null) return 0
        return if (listItem1.lastMessageDate == null) k else k * if (listItem1.lastMessageDate.after(listItem2.lastMessageDate)) 1 else -1
    }

    private fun compareByNew(listItem1: Topic, listItem2: Topic, k: Int): Int {
        if (listItem1.state == listItem2.state) return 0
        return if (listItem1.state == Topic.FLAG_NEW) k else -k
    }

    private fun compareByTitle(listItem1: Topic, listItem2: Topic, k: Int): Int {
        if (listItem1.title == null && listItem2.title == null) return 0
        return if (listItem1.title == null) k else k * listItem1.title.compareTo(listItem2.title)
    }

    private fun showSettings() {
        val settingsActivity = Intent(
                context, TopicsListPreferencesActivity::class.java)
        //settingsActivity.putExtra("LIST_NAME",getListName());
        TopicsPreferenceFragment.ListName = listName
        context!!.startActivity(settingsActivity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.list_settings_item) {
            showSettings()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (menu.findItem(R.id.list_settings_item) != null) menu.findItem(R.id.list_settings_item).isVisible = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu.findItem(R.id.list_settings_item) != null)
        inflater.inflate(R.menu.topics, menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeArrow()
    }

    override fun onResume() {
        super.onResume()
        removeArrow()
    }

    companion object {
        const val KEY_TOPIC_ID = "KEY_TOPIC_ID"
    }


}