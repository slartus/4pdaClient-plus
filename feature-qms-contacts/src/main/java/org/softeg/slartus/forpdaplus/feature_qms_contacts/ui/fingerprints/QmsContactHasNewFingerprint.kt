package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.Transformation
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint
import org.softeg.slartus.forpdaplus.feature_qms_contacts.R
import org.softeg.slartus.forpdaplus.feature_qms_contacts.databinding.LayoutQmsContactHasNewItemBinding

class QmsContactHasNewFingerprint(
    private val squareAvatars: Boolean,
    private val showAvatars: Boolean,
    @DrawableRes
    private val accentBackground: Int,
    private val onClickListener: (view: View?, item: QmsContactItem) -> Unit
) :
    ItemFingerprint<LayoutQmsContactHasNewItemBinding, QmsContactItem> {
    private val diffUtil = object : DiffUtil.ItemCallback<QmsContactItem>() {
        override fun areItemsTheSame(
            oldItem: QmsContactItem,
            newItem: QmsContactItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: QmsContactItem,
            newItem: QmsContactItem
        ) = oldItem == newItem
    }

    override fun getLayoutId() = R.layout.layout_qms_contact_has_new_item

    override fun isRelativeItem(item: Item) = item is QmsContactItem && item.newMessagesCount != 0

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<LayoutQmsContactHasNewItemBinding, QmsContactItem> {
        val binding = LayoutQmsContactHasNewItemBinding.inflate(layoutInflater, parent, false)
        return QmsContactHasNewViewHolder(
            binding = binding,
            squareAvatars = squareAvatars,
            showAvatars = showAvatars,
            accentBackground = accentBackground,
            onClickListener = onClickListener
        )
    }

    override fun getDiffUtil() = diffUtil
}

class QmsContactHasNewViewHolder(
    binding: LayoutQmsContactHasNewItemBinding,
    squareAvatars: Boolean,
    private val showAvatars: Boolean,
    @DrawableRes
    private val accentBackground: Int,
    private val onClickListener: (view: View?, item: QmsContactItem) -> Unit
) :
    BaseViewHolder<LayoutQmsContactHasNewItemBinding, QmsContactItem>(binding) {
    private val avatarTransformations: List<Transformation> =
        if (squareAvatars) emptyList() else listOf(CircleCropTransformation())
    val imageLoader = ImageLoader.Builder(itemView.context)
        .componentRegistry {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder(itemView.context))
            } else {
                add(GifDecoder())
            }
        }
        .build()

    init {
        itemView.setOnClickListener { v -> onClickListener(v, item) }
        binding.avatarImageView.isVisible = showAvatars
        binding.newMessagesCountTextView.setBackgroundResource(accentBackground)
    }

    override fun onBind(item: QmsContactItem) {
        super.onBind(item)

        with(binding) {
            if (showAvatars) {
                avatarImageView.load(item.avatarUrl, imageLoader) {
                    transformations(avatarTransformations)
                }
            }
            nickTextView.text = item.nick
            newMessagesCountTextView.text = item.newMessagesCount.toString()
        }
    }
}