package org.softeg.slartus.forpdaplus.core.ui.dialogs

/**
 * Created by radiationx on 31.01.16.
 */
data class MenuItemAction(val title: String?, val runnable: () -> Unit) {
    constructor(title: String?, runnable: Runnable) : this(title, { runnable.run() })
}