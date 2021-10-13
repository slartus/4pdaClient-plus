package org.softeg.slartus.forpdaplus.navigation

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.Creator
import com.github.terrakok.cicerone.androidx.FragmentScreen

open class DialogScreen constructor(
    key: String? = null,
    private val dialogCreator: Creator<FragmentFactory, DialogFragment>
) : FragmentScreen {
    override val screenKey: String = key ?: dialogCreator::class.java.name
    override val clearContainer = false

    fun createDialog(factory: FragmentFactory): DialogFragment {
        return dialogCreator.create(factory)
    }

    override fun createFragment(factory: FragmentFactory): Fragment {
        return dialogCreator.create(factory)
    }
}
