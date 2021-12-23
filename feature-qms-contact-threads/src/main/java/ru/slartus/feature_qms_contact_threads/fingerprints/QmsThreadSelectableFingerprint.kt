package ru.slartus.feature_qms_contact_threads.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import ru.slartus.feature_qms_contact_threads.R
import ru.slartus.feature_qms_contact_threads.databinding.LayoutQmsThreadSelectableItemBinding

class QmsThreadSelectableFingerprint(
    @DrawableRes
    private val accentBackground: Int,
    private val onClickListener: (view: View?, item: QmsThreadSelectableItem) -> Unit
) :
    ItemFingerprint<LayoutQmsThreadSelectableItemBinding, QmsThreadSelectableItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<QmsThreadSelectableItem>() {
        override fun areItemsTheSame(
            oldItem: QmsThreadSelectableItem,
            newItem: QmsThreadSelectableItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: QmsThreadSelectableItem,
            newItem: QmsThreadSelectableItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_qms_thread_selectable_item

    override fun isRelativeItem(item: Item) = item is QmsThreadSelectableItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutQmsThreadSelectableItemBinding, QmsThreadSelectableItem> {
        val binding = LayoutQmsThreadSelectableItemBinding.inflate(layoutInflater, parent, false)
        return QmsThreadSelectableViewHolder(
            binding = binding,
            accentBackground = accentBackground,
            onClickListener = onClickListener
        )
    }

    override fun getDiffUtil() = diffUtil
}

class QmsThreadSelectableViewHolder(
    binding: LayoutQmsThreadSelectableItemBinding,
    @DrawableRes
    accentBackground: Int,
    private val onClickListener: (view: View?, item: QmsThreadSelectableItem) -> Unit
) :
    BaseViewHolder<LayoutQmsThreadSelectableItemBinding, QmsThreadSelectableItem>(binding) {
    private val totalTextFormat =
        itemView.context.getString(org.softeg.slartus.forpdaplus.core_res.R.string.qms_thread_total)

    init {
        itemView.setOnClickListener { v ->
            onClickListener(v, item)
        }
        binding.newMessagesCountTextView.setBackgroundResource(accentBackground)
    }

    override fun onBind(item: QmsThreadSelectableItem) {
        super.onBind(item)

        with(binding) {
            titleTextView.text = item.title
            messagesCountTextView.text = totalTextFormat.format(item.messagesCount)
            newMessagesCountTextView.text = item.newMessagesCount.toString()
            newMessagesCountTextView.isVisible = item.newMessagesCount > 0
            selectedCheckBox.isChecked = item.selected
            dateTextView.text = item.lastMessageDate
        }
    }
}

data class QmsThreadSelectableItem(
    override val id: String,
    override val title: String,
    override val messagesCount: Int,
    override val newMessagesCount: Int,
    override val lastMessageDate: String?,
    val selected: Boolean = false
) : ThreadItem