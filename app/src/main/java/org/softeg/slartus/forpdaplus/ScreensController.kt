package org.softeg.slartus.forpdaplus

import androidx.fragment.app.Fragment

class ScreensController {
    private var onShowScreenListener: OnShowScreenListener? = null
    fun setOnShowScreenListener(listener: OnShowScreenListener) {
        onShowScreenListener = listener
    }
    fun addTab(url: String?, fragment: Fragment?)
    {
        onShowScreenListener?.addTab(url, fragment)
    }

    fun addTab(title: String?, url: String?, fragment: Fragment?){
        onShowScreenListener?.addTab(title, url, fragment)
    }
}

interface OnShowScreenListener {
    fun addTab(url: String?, fragment: Fragment?)
    fun addTab(title: String?, url: String?, fragment: Fragment?)
}