package org.softeg.slartus.forpdaplus.feature_news.ui

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdacommon.isToday
import org.softeg.slartus.forpdacommon.isYesterday
import org.softeg.slartus.forpdaplus.feature_news.data.NewsListItem
import org.softeg.slartus.forpdaplus.feature_news.databinding.LayoutNewsListItemBinding
import java.text.SimpleDateFormat
import java.util.*

class NewsListAdapter :
    PagingDataAdapter<NewsListItem, NotesListViewHolder>(NewsListItemComparator) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotesListViewHolder {
        val binding =
            LayoutNewsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesListViewHolder, position: Int) {
        val item = getItem(position)
        // Note that item may be null. ViewHolder must support binding a
        // null item as a placeholder.
        holder.bind(item!!)
    }
}

object NewsListItemComparator : DiffUtil.ItemCallback<NewsListItem>() {
    override fun areItemsTheSame(oldItem: NewsListItem, newItem: NewsListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NewsListItem, newItem: NewsListItem): Boolean {
        return oldItem == newItem
    }
}

class NotesListViewHolder(private val binding: LayoutNewsListItemBinding) :
    RecyclerView.ViewHolder(binding.root),
    View.OnCreateContextMenuListener {

    private var currentItem: NewsListItem? = null

    fun bind(item: NewsListItem) {
        currentItem = item

        binding.titleTextView.text = item.title ?: ""
        binding.authorTextView.text = item.author ?: ""
        binding.dateTextView.text = dateToDisplay(item.date)
        binding.descriptionTextView.text = item.description.fromHtml()
        val comments = item.commentsCount?.toString()
        binding.commentsTextView.text = comments ?: ""
        binding.commentsImage.isVisible = comments != null
        Glide
            .with(binding.posterImageView)
            .load(item.imgUrl)
            .centerCrop()
            .into(binding.posterImageView)

    }

    private fun dateToDisplay(date: Date?): String? {
        return try {
            when {
                date == null -> null
//                else->DateUtils.getRelativeTimeSpanString(date.time)?.toString()
                date.isToday -> displayTodayTimeFormat.format(date)
                date.isYesterday -> displayTodayTimeFormat.format(date)
                else -> displayDateFormat.format(date)
            }
        } catch (ex: Exception) {
            date.toString()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        p2: ContextMenu.ContextMenuInfo?
    ) {

    }

    companion object {
        val displayDateFormat by lazy { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }
        val displayTodayTimeFormat by lazy {
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            )
        }
        val displayYesterdayTimeFormat by lazy {
            SimpleDateFormat(
                "'вчера в' HH:mm",
                Locale.getDefault()
            )
        }
    }
}