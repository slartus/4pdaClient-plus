package ru.slartus.feature_qms_contact_threads.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import ru.slartus.feature_qms_contact_threads.R
import ru.slartus.feature_qms_contact_threads.databinding.LayoutQmsThreadItemBinding

class QmsThreadFingerprint(
    private val onClickListener: (view: View?, item: QmsThreadItem) -> Unit
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
            onClickListener = onClickListener
        )
    }

    override fun getDiffUtil() = diffUtil
}

class QmsThreadViewHolder(
    binding: LayoutQmsThreadItemBinding,
    private val onClickListener: (view: View?, item: QmsThreadItem) -> Unit
) :
    BaseViewHolder<LayoutQmsThreadItemBinding, QmsThreadItem>(binding) {

    init {
        itemView.setOnClickListener { v -> onClickListener(v, item) }
    }

    override fun onBind(item: QmsThreadItem) {
        super.onBind(item)

        with(binding) {

            titleTextView.text = item.title
            messagesCountTextView.text = item.messagesCount.toString()
            newMessagesCountTextView.text = item.newMessagesCount.toString()
        }
    }
}

data class QmsThreadItem(
    val id: String,
    val title: String,
    val messagesCount: Int,
    val newMessagesCount: Int
) : Item