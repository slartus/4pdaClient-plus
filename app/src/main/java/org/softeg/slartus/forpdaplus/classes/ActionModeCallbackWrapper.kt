package org.softeg.slartus.forpdaplus.classes

import android.graphics.Rect
import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import org.softeg.slartus.forpdaplus.classes.AdvWebView.OnStartActionModeListener


@RequiresApi(api = Build.VERSION_CODES.M)
class ActionModeCallbackWrapper(
    private val mWrapped: ActionMode.Callback,
    private val actionModeListener: OnStartActionModeListener? = null
) : ActionMode.Callback2() {
    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        actionModeListener?.onCreateActionMode(actionMode, menu)
        return mWrapped.onCreateActionMode(actionMode, menu)
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return mWrapped.onPrepareActionMode(actionMode, menu)
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        return mWrapped.onActionItemClicked(actionMode, menuItem)
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        mWrapped.onDestroyActionMode(actionMode)
    }

    override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
        if (mWrapped is ActionMode.Callback2) {
            mWrapped.onGetContentRect(mode, view, outRect)
        } else {
            super.onGetContentRect(mode, view, outRect)
        }
    }
}