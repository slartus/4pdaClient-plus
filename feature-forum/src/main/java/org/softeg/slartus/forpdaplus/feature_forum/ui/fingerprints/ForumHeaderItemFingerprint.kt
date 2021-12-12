package org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil

import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.R

import org.softeg.slartus.forpdaplus.feature_forum.databinding.ForumHeaderItemBinding

class ForumHeaderItemFingerprint(
    private val onClickListener: (view: View?, item: ForumHeaderItem) -> Unit,
    private val onLongClickListener: (view: View?, item: ForumHeaderItem) -> Boolean
) :
    ItemFingerprint<ForumHeaderItemBinding, ForumHeaderItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<ForumHeaderItem>() {
        override fun areItemsTheSame(
            oldItem: ForumHeaderItem,
            newItem: ForumHeaderItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ForumHeaderItem,
            newItem: ForumHeaderItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.forum_header_item

    override fun isRelativeItem(item: Item) = item is ForumHeaderItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ForumHeaderItemBinding, ForumHeaderItem> {
        val binding = ForumHeaderItemBinding.inflate(layoutInflater, parent, false)
        return ForumHeaderViewHolder(binding, onClickListener, onLongClickListener)
    }

    override fun getDiffUtil() = diffUtil

}

class ForumHeaderViewHolder(
    binding: ForumHeaderItemBinding,
    private val onClickListener: (view: View?, item: ForumHeaderItem) -> Unit,
    private val onLongClickListener: (view: View?, item: ForumHeaderItem) -> Boolean
) :
    BaseViewHolder<ForumHeaderItemBinding, ForumHeaderItem>(binding) {

    override fun onBind(item: ForumHeaderItem) {
        super.onBind(item)
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        itemView.setOnLongClickListener { v -> onLongClickListener(v, item) }
        with(binding) {
            titleTextView.text = item.title
        }
    }
}

data class ForumHeaderItem(val id: String?, val title: String?) : Item