package org.softeg.slartus.forpdaplus.core_lib.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding

open class FingerprintAdapter(
    private val fingerprints: List<ItemFingerprint<*, *>>,
) : ListAdapter<Item, BaseViewHolder<ViewBinding, Item>>(
    FingerprintDiffUtil(fingerprints)
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewBinding, Item> {
        val inflater = LayoutInflater.from(parent.context)
        return fingerprints.find { it.getLayoutId() == viewType }
            ?.getViewHolder(inflater, parent)
            ?.let { it as BaseViewHolder<ViewBinding, Item> }
            ?: throw IllegalArgumentException("View type not found: $viewType")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ViewBinding, Item>, position: Int) {
        holder.onBind(currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return fingerprints.find { it.isRelativeItem(item) }
            ?.getLayoutId()
            ?: throw IllegalArgumentException("View type not found: $item")
    }

}