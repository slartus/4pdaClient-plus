package org.softeg.slartus.forpdaplus.listtemplates

import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R

class CheckHumanityBrickInfo : BrickInfo() {
    override fun getTitle(): String {
        return App.getContext().getString(R.string.check_humanity_title)
    }

    override fun getName(): String = NAME

    override fun getIcon(): Int = R.drawable.settings_grey

    override fun createFragment(): Fragment? = null

    companion object{
        const val NAME = "CheckHumanityBrickInfo"
    }
}