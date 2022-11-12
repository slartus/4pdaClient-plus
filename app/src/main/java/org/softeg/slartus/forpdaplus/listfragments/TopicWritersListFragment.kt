package org.softeg.slartus.forpdaplus.listfragments

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.OldUser
import org.softeg.slartus.forpdaapi.classes.ListData
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import ru.softeg.slartus.forum.api.TopicUsersRepository
import javax.inject.Inject

/*
 * Created by slinkin on 17.06.2015.
 */
@AndroidEntryPoint
class TopicWritersListFragment : BaseLoaderListFragment() {
    private var m_TopicId: String? = null

    @Inject
    lateinit var topicUsersRepository: TopicUsersRepository

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            m_TopicId = savedInstanceState.getString(TOPIC_ID_KEY)
        } else if (arguments != null) {
            m_TopicId = arguments!!.getString(TOPIC_ID_KEY)
        }
        setArrow()
    }

    override fun getLoadArgs(): Bundle {
        val args = Bundle()
        args.putString(TOPIC_ID_KEY, m_TopicId)
        return args
    }

    override fun createAdapter(): BaseAdapter {
        return UsersAdapter(
            activity, data.items
        )
    }

    override fun useCache(): Boolean {
        return false
    }

    override fun getViewResourceId(): Int {
        return R.layout.list_fragment
    }

    @Throws(Throwable::class)
    override fun loadData(
        loaderId: Int,
        args: Bundle
    ): ListData {
        val topicId = args.getString(TopicReadersListFragment.TOPIC_ID_KEY) ?: return ListData()
        val writers = runBlocking {
            topicUsersRepository.getTopicWriters(topicId)
        }
        return ListData().apply {
            items.addAll(writers.map {
                OldUser().apply {
                    mid = it.id
                    nick = it.nick
                    MessagesCount = it.postsCount.toString()
                }
            })
        }
    }

    inner class UsersAdapter(context: Context?, private val mUsers: ArrayList<IListItem>) :
        BaseAdapter() {
        protected var m_Inflater: LayoutInflater

        init {
            m_Inflater = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return mUsers.size
        }

        override fun getItem(position: Int): Any {
            return mUsers[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
            var convertView = view
            val holder: ViewHolder
            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.topic_writer_item, parent, false)
                holder = ViewHolder()
                assert(convertView != null)
                holder.txtCount = convertView.findViewById(R.id.txtMessagesCount)
                holder.txtNick = convertView.findViewById(R.id.txtNick)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            val user = getItem(position) as OldUser
            holder.txtCount!!.text = user.MessagesCount
            holder.txtNick!!.text = Html.fromHtml(user.nick)
            convertView!!.setOnClickListener { openProfile(user) }
            return convertView!!
        }

        inner class ViewHolder {
            var txtNick: TextView? = null
            var txtCount: TextView? = null
        }
    }

    fun openProfile(user: OldUser) {
        ProfileFragment.showProfile(user.mid, user.mid)
    }

    companion object {
        const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    }
}