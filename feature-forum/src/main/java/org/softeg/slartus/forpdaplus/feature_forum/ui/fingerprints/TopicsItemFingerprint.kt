package org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.R
import org.softeg.slartus.forpdaplus.feature_forum.databinding.LayoutTopicsBinding

class TopicsItemItemFingerprint(
    private val onClickListener: (view: View?, item: TopicsItemItem) -> Unit
) :
    ItemFingerprint<LayoutTopicsBinding, TopicsItemItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<TopicsItemItem>() {
        override fun areItemsTheSame(
            oldItem: TopicsItemItem,
            newItem: TopicsItemItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: TopicsItemItem,
            newItem: TopicsItemItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_topics

    override fun isRelativeItem(item: Item) = item is TopicsItemItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutTopicsBinding, TopicsItemItem> {
        val binding = LayoutTopicsBinding.inflate(layoutInflater, parent, false)
        return TopicsItemViewHolder(binding, onClickListener)
    }

    override fun getDiffUtil() = diffUtil
}

class TopicsItemViewHolder(
    binding: LayoutTopicsBinding,
    private val onClickListener: (view: View?, item: TopicsItemItem) -> Unit
) :
    BaseViewHolder<LayoutTopicsBinding, TopicsItemItem>(binding) {
    init {
        itemView.setOnClickListener { v -> onClickListener(v, item) }
    }
}

data class TopicsItemItem(
    val id: String?
) : Item