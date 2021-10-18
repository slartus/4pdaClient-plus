package org.softeg.slartus.forpdaplus.core_ui.ui.dialogs

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

class MenuItemActionsDialog : MaterialDialog {
    constructor(context: Context, title: CharSequence, items: List<MenuItemAction>) :
            super(
                Builder(context)
                    .title(title)
                    .items(items.map { it.title })
                    .itemsCallback { _, _, position, _ -> items[position].runnable.invoke() }
            )

    constructor(context: Context, items: List<MenuItemAction>) :
            super(
                Builder(context)
                    .items(items.map { it.title })
                    .itemsCallback { _, _, position, _ -> items[position].runnable.invoke() }
            )
}