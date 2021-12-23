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
import ru.slartus.feature_qms_contact_threads.databinding.LayoutQmsThreadItemBinding

class QmsThreadFingerprint(
    @DrawableRes
    private val accentBackground: Int,
    private val onClickListener: (view: View?, item: QmsThreadItem) -> Unit,
    private val onLongClickListener: (view: View?, item: QmsThreadItem) -> Boolean
) :
    ItemFingerprint<LayoutQmsThreadItemBinding, QmsThreadItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<QmsThreadItem>() {
        override fun areItemsTheSame(
            oldItem: QmsThreadItem,
            newItem: QmsThreadItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: QmsThreadItem,
            newItem: QmsThreadItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_qms_thread_item

    override fun isRelativeItem(item: Item) = item is QmsThreadItem

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutQmsThreadItemBinding, QmsThreadItem> {
        val binding = LayoutQmsThreadItemBinding.inflate(layoutInflater, parent, false)
        return QmsThreadViewHolder(
            binding = binding,
            accentBackground = accentBackground,
            onClickListener = onClickListener,
            onLongClickListener = onLongClickListener
        )
    }

    override fun getDiffUtil() = diffUtil
}

class QmsThreadViewHolder(
    binding: LayoutQmsThreadItemBinding,
    @DrawableRes
    accentBackground: Int,
    private val onClickListener: (view: View?, item: QmsThreadItem) -> Unit,
    private val onLongClickListener: (view: View?, item: QmsThreadItem) -> Boolean
) :
    BaseViewHolder<LayoutQmsThreadItemBinding, QmsThreadItem>(binding) {
    private val totalTextFormat =
        itemView.context.getString(org.softeg.slartus.forpdaplus.core_res.R.string.qms_thread_total)

    init {
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        itemView.setOnLongClickListener { v -> onLongClickListener(v, item) }
        binding.newMessagesCountTextView.setBackgroundResource(accentBackground)
    }

    override fun onBind(item: QmsThreadItem) {
        super.onBind(item)

        with(binding) {
            titleTextView.text = item.title
            messagesCountTextView.text = totalTextFormat.format(item.messagesCount)
            newMessagesCountTextView.text = item.newMessagesCount.toString()
            newMessagesCountTextView.isVisible = item.newMessagesCount > 0
            dateTextView.text = item.lastMessageDate
        }
    }
}

data class QmsThreadItem(
    override val id: String,
    override val title: String,
    override val messagesCount: Int,
    override val newMessagesCount: Int,
    override val lastMessageDate: String?
) : ThreadItem