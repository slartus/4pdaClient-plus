package org.softeg.slartus.forpdaplus.core.ui.dialogs

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

class MenuItemActionsDialog(context: Context, title: CharSequence, items: List<MenuItemAction>) :
    MaterialDialog(
        Builder(context)
            .title(title)
            .items(items.map { it.title })
            .itemsCallback { dialog, itemView, position, text -> items[position].runnable.invoke() }
    ) {

}
