package org.softeg.slartus.forpdaplus.listfragments.next.forum.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.databinding.ForumHeaderCurrentItemBinding

class ForumHeaderCurrentItemFingerprint(
    private val onClickListener: (view: View?, item: ForumCurrentHeaderItem) -> Unit,
    private val onLongClickListener: (view: View?, item: ForumCurrentHeaderItem) -> Boolean
) :
    ItemFingerprint<ForumHeaderCurrentItemBinding, ForumCurrentHeaderItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<ForumCurrentHeaderItem>() {
        override fun areItemsTheSame(
            oldItem: ForumCurrentHeaderItem,
            newItem: ForumCurrentHeaderItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ForumCurrentHeaderItem,
            newItem: ForumCurrentHeaderItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.forum_header_current_item

    override fun isRelativeItem(item: Item) = item is ForumCurrentHeaderItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ForumHeaderCurrentItemBinding, ForumCurrentHeaderItem> {
        val binding = ForumHeaderCurrentItemBinding.inflate(layoutInflater, parent, false)
        return ForumCurrentHeaderViewHolder(binding, onClickListener, onLongClickListener)
    }

    override fun getDiffUtil() = diffUtil
}

class ForumCurrentHeaderViewHolder(binding: ForumHeaderCurrentItemBinding,
                                   private val onClickListener: (view: View?, item: ForumCurrentHeaderItem) -> Unit,
                                   private val onLongClickListener: (view: View?, item: ForumCurrentHeaderItem) -> Boolean) :
    BaseViewHolder<ForumHeaderCurrentItemBinding, ForumCurrentHeaderItem>(binding) {
    override fun onBind(item: ForumCurrentHeaderItem) {
        super.onBind(item)
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        itemView.setOnLongClickListener { v -> onLongClickListener(v, item) }
        with(binding) {
            titleTextView.text = item.title
        }
    }
}

data class ForumCurrentHeaderItem(val id: String?, val title: String) : Item