package org.softeg.slartus.forpdaplus.topic.impl.screens.attachments.fingerprints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.topic.impl.R
import org.softeg.slartus.forpdaplus.topic.impl.databinding.LayoutTopicAttachmentItemBinding
import org.softeg.slartus.forpdaplus.topic.impl.screens.attachments.TopicAttachmentModel


class TopicAttachmentFingerprint(
    private val onClickListener: (view: View?, item: TopicAttachmentModel) -> Unit
) :
    ItemFingerprint<LayoutTopicAttachmentItemBinding, TopicAttachmentModel> {
    private val diffUtil = object : DiffUtil.ItemCallback<TopicAttachmentModel>() {
        override fun areItemsTheSame(
            oldItem: TopicAttachmentModel,
            newItem: TopicAttachmentModel
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: TopicAttachmentModel,
            newItem: TopicAttachmentModel
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_topic_attachment_item

    override fun isRelativeItem(item: Item) = item is TopicAttachmentModel

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutTopicAttachmentItemBinding, TopicAttachmentModel> {
        val binding = LayoutTopicAttachmentItemBinding.inflate(layoutInflater, parent, false)
        return TopicAttachmentViewHolder(
            binding = binding,
            onClickListener = onClickListener
        )
    }

    override fun getDiffUtil() = diffUtil
}

class TopicAttachmentViewHolder(
    binding: LayoutTopicAttachmentItemBinding,
    private val onClickListener: (view: View?, item: TopicAttachmentModel) -> Unit
) :
    BaseViewHolder<LayoutTopicAttachmentItemBinding, TopicAttachmentModel>(binding) {

    init {
        itemView.setOnClickListener { v -> onClickListener(v, item) }
    }

    override fun onBind(item: TopicAttachmentModel) {
        super.onBind(item)

        with(binding) {
            iconImageView.load(item.iconUrl)
            nameTextView.text = item.name
            dateTextView.text = listOfNotNull(item.date, item.count).joinToString(separator = " скачиваний: ")
            sizeTextView.text = item.size
        }
    }
}