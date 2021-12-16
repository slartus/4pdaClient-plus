package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.BaseViewHolder
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.FingerprintAdapter
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.Item
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.ItemFingerprint

class QmsContactsAdapter(fingerprints: List<ItemFingerprint<*, *>>) :
    FingerprintAdapter(fingerprints) {
    var lastLongClickItem: Item? = null
        private set

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewBinding, Item> {
        val holder = super.onCreateViewHolder(parent, viewType)
        holder.itemView.setOnLongClickListener {
            lastLongClickItem = holder.item
            false
        }
        return holder
    }
}