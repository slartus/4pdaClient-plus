package org.softeg.slartus.forpdaplus.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.core.interfaces.IOnBackPressed
import org.softeg.slartus.forpdaplus.listfragments.BaseBrickFragment

abstract class BaseBrickContainerFragment :
    BaseBrickFragment(R.layout.fragment_base_brick_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.fragment_container, getFragmentInstance())
                .commitAllowingStateLoss()
        }
    }

    abstract fun getFragmentInstance(): Fragment

    override fun onBackPressed(): Boolean {
        return childFragmentManager.fragments.any { (it as? IOnBackPressed)?.onBackPressed() == true }
    }

}

abstract class BaseGeneralContainerFragment :
    GeneralFragment(R.layout.fragment_base_brick_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.fragment_container, getFragmentInstance())
                .commitAllowingStateLoss()
        }
    }

    abstract fun getFragmentInstance(): Fragment

    override fun onBackPressed(): Boolean {
        return childFragmentManager.fragments.any { (it as? IOnBackPressed)?.onBackPressed() == true }
    }

}