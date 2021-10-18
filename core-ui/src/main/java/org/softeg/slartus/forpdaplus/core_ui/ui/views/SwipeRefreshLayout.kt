package org.softeg.slartus.forpdaplus.core_ui.ui.views

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.softeg.slartus.forpdaplus.core_ui.AppTheme.Companion.mainAccentColor
import org.softeg.slartus.forpdaplus.core_ui.AppTheme.Companion.swipeRefreshBackground

fun SwipeRefreshLayout.configure(){
    setColorSchemeResources(mainAccentColor)
    setProgressBackgroundColorSchemeResource(swipeRefreshBackground)
}