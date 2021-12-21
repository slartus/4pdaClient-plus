package org.softeg.slartus.forpdaplus.di

import android.content.Context
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.core.LinkManager
import javax.inject.Inject

class LinkManagerImpl @Inject constructor() : LinkManager {
    override fun showUrlActions(context: Context, titleRes: Int, url: String) {
        ExtUrl.showSelectActionDialog(
            context,
            context.getString(titleRes), url
        )
    }
}