package org.softeg.slartus.forpdaplus.feature_news.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.softeg.slartus.forpdaplus.feature_news.databinding.LayoutNewsListItemBinding

class NewsListAdapter :
    PagingDataAdapter<UiNewsListItem, NotesListViewHolder>(NewsListItemComparator) {
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

object NewsListItemComparator : DiffUtil.ItemCallback<UiNewsListItem>() {
    override fun areItemsTheSame(oldItem: UiNewsListItem, newItem: UiNewsListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UiNewsListItem, newItem: UiNewsListItem): Boolean {
        return oldItem == newItem
    }
}

class NotesListViewHolder(private val binding: LayoutNewsListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private var currentItem: UiNewsListItem? = null

    fun bind(item: UiNewsListItem) {
        currentItem = item

        binding.titleTextView.text = item.title ?: ""
        binding.authorTextView.text = item.author ?: ""
        binding.dateTextView.text = item.date
        binding.descriptionTextView.text = item.description ?: ""
        binding.commentsTextView.text = item.commentsCount ?: ""
        binding.commentsImage.isVisible = item.commentsCount != null
        Glide
            .with(binding.posterImageView)
            .load(item.imgUrl)
            .centerCrop()
            .into(binding.posterImageView)

    }
}