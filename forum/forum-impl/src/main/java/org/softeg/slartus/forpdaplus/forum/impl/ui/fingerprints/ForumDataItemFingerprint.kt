package org.softeg.slartus.forpdaplus.forum.impl.ui.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import coil.load

import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.forum.impl.R
import org.softeg.slartus.forpdaplus.forum.impl.databinding.LayoutForumBinding


class ForumDataItemFingerprint(
    private val showImages: Boolean,
    private val onClickListener: (view: View?, item: ForumDataItem) -> Unit,
    private val onLongClickListener: (view: View?, item: ForumDataItem) -> Boolean
) :
    ItemFingerprint<LayoutForumBinding, ForumDataItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<ForumDataItem>() {
        override fun areItemsTheSame(
            oldItem: ForumDataItem,
            newItem: ForumDataItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ForumDataItem,
            newItem: ForumDataItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_forum

    override fun isRelativeItem(item: Item) = item is ForumDataItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutForumBinding, ForumDataItem> {
        val binding = LayoutForumBinding.inflate(layoutInflater, parent, false)
        return ForumDataViewHolder(binding, showImages, onClickListener, onLongClickListener)
    }

    override fun getDiffUtil() = diffUtil
}

class ForumDataViewHolder(
    binding: LayoutForumBinding,
    showImages: Boolean,
    private val onClickListener: (view: View?, item: ForumDataItem) -> Unit,
    private val onLongClickListener: (view: View?, item: ForumDataItem) -> Boolean
) :
    BaseViewHolder<LayoutForumBinding, ForumDataItem>(binding) {
    init {
        binding.iconImageView.isVisible = showImages
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        itemView.setOnLongClickListener { v -> onLongClickListener(v, item) }
    }

    override fun onBind(item: ForumDataItem) {
        super.onBind(item)

        with(binding) {
            titleTextView.text = item.title
            descriptionTextView.text = item.description
            descriptionTextView.isVisible = !item.description.isNullOrEmpty()
            if (iconImageView.isVisible && !item.iconUrl.isNullOrEmpty()) {
                iconImageView.load(item.iconUrl)
            }
        }
    }
}

data class ForumDataItem(
    val id: String?,
    val title: String?,
    val description: String?,
    val iconUrl: String?,
    val isHasForums: Boolean
) : Item