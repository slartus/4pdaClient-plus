package org.softeg.slartus.forpdaplus.listfragments.next.forum.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.databinding.ForumHeaderNotopicsItemBinding

class ForumHeaderNoTopicsItemFingerprint(private val onClickListener: (view: View?, item: ForumNoTopicsHeaderItem) -> Unit,
                                         private val onLongClickListener: (view: View?, item: ForumNoTopicsHeaderItem) -> Boolean) :
    ItemFingerprint<ForumHeaderNotopicsItemBinding, ForumNoTopicsHeaderItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<ForumNoTopicsHeaderItem>() {
        override fun areItemsTheSame(
            oldItem: ForumNoTopicsHeaderItem,
            newItem: ForumNoTopicsHeaderItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ForumNoTopicsHeaderItem,
            newItem: ForumNoTopicsHeaderItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.forum_header_notopics_item

    override fun isRelativeItem(item: Item) = item is ForumNoTopicsHeaderItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ForumHeaderNotopicsItemBinding, ForumNoTopicsHeaderItem> {
        val binding = ForumHeaderNotopicsItemBinding.inflate(layoutInflater, parent, false)
        return ForumNoTopicsHeaderViewHolder(binding, onClickListener, onLongClickListener)
    }

    override fun getDiffUtil() = diffUtil
}

class ForumNoTopicsHeaderViewHolder(binding: ForumHeaderNotopicsItemBinding,
                                    private val onClickListener: (view: View?, item: ForumNoTopicsHeaderItem) -> Unit,
                                    private val onLongClickListener: (view: View?, item: ForumNoTopicsHeaderItem) -> Boolean) :
    BaseViewHolder<ForumHeaderNotopicsItemBinding, ForumNoTopicsHeaderItem>(binding) {
    override fun onBind(item: ForumNoTopicsHeaderItem) {
        super.onBind(item)
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        itemView.setOnLongClickListener { v -> onLongClickListener(v, item) }
        with(binding) {
            titleTextView.text = item.title
        }
    }
}

data class ForumNoTopicsHeaderItem(val id: String?, val title: String) : Item