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
import org.softeg.slartus.forpdaplus.classes.common.ExtColor
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import ru.softeg.slartus.forum.api.TopicUsersRepository
import javax.inject.Inject

/*
 * Created by slinkin on 17.06.2015.
 */
@AndroidEntryPoint
class TopicReadersListFragment : BaseLoaderListFragment() {
    private var m_TopicId: String? = null

    @Inject
    lateinit var topicUsersRepository: TopicUsersRepository
    override fun onResume() {
        super.onResume()
        setArrow()
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
        val topicId = args.getString(TOPIC_ID_KEY) ?: return ListData()
        val readers = runBlocking {
            topicUsersRepository.getTopicReaders(topicId)
        }
        return ListData().apply {
            items.addAll(readers.map {
                OldUser().apply {
                    mid = it.id
                    nick = it.nick
                    htmlColor = it.htmlColor
                }
            })
        }
    }

    inner class UsersAdapter(context: Context?, private val mUsers: ArrayList<IListItem>) :
        BaseAdapter() {
        private var m_Inflater: LayoutInflater

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
                convertView = m_Inflater.inflate(R.layout.topic_reader_item, parent, false)
                holder = ViewHolder()
                assert(convertView != null)
                holder.txtNick = convertView.findViewById(R.id.txtNick)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            val user = getItem(position) as OldUser
            holder.txtNick!!.text = Html.fromHtml(user.nick)
            try {
                holder.txtNick!!.setTextColor(ExtColor.parseColor(correctHtmlColor(user.htmlColor)))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            convertView!!.setOnClickListener { openProfile(user) }
            return convertView
        }

        private fun correctHtmlColor(color: String?): String? {
            var color = color
            when (color) {
                "green" -> color = "#10BB10" //Обычный и активынй пользователь
                "#FF9900" -> color = "#EC9B22" //Друг 4пда
                "purple" -> color = "purple" //Почетные форумчане
                "#32CD32" -> color = "#4EC14E" //FAQMakers
                "#9A60FF" -> color = "#8461C0" //Участники спецпроекта
                "#B100BF" -> color = "#8E1E97" //Бизнессмены
                "#0099FF" -> color = "#107AC0" //Пощники модератора и модераторы
                "blue" -> color = "#4545E5" //Супермодераторы
                "red" -> color = "#CB3838" //Администраторы
            }
            return color
        }

        inner class ViewHolder {
            var txtNick: TextView? = null
        }
    }

    fun openProfile(user: OldUser) {
        ProfileFragment.showProfile(user.mid, user.mid)
    }

    companion object {
        const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    }
}