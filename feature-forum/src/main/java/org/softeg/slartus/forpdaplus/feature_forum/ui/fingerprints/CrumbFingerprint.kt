package org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.R
import org.softeg.slartus.forpdaplus.feature_forum.databinding.LayoutCrumbBinding

class CrumbFingerprint(
    private val onClickListener: (view: View?, item: CrumbItem) -> Unit,
    private val onLongClickListener: (view: View?, item: CrumbItem) -> Boolean
) :
    ItemFingerprint<LayoutCrumbBinding, CrumbItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<CrumbItem>() {
        override fun areItemsTheSame(
            oldItem: CrumbItem,
            newItem: CrumbItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CrumbItem,
            newItem: CrumbItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_crumb

    override fun isRelativeItem(item: Item) = item is CrumbItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutCrumbBinding, CrumbItem> {
        val binding = LayoutCrumbBinding.inflate(layoutInflater, parent, false)
        return CrumbViewHolder(binding, onClickListener, onLongClickListener)
    }

    override fun getDiffUtil() = diffUtil
}

class CrumbViewHolder(
    binding: LayoutCrumbBinding,
    private val onClickListener: (view: View?, item: CrumbItem) -> Unit,
    private val onLongClickListener: (view: View?, item: CrumbItem) -> Boolean
) :
    BaseViewHolder<LayoutCrumbBinding, CrumbItem>(binding) {
    init {
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        itemView.setOnLongClickListener { v -> onLongClickListener(v, item) }
    }

    override fun onBind(item: CrumbItem) {
        super.onBind(item)

        with(binding) {
            titleTextView.text = item.title
        }
    }
}

data class CrumbItem(val id: String?, val title: String?) : Item