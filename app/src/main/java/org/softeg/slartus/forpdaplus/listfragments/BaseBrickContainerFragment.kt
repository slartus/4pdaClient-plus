package org.softeg.slartus.forpdaplus.listfragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment

abstract class BaseBrickContainerFragment :
    BaseBrickFragment(R.layout.fragment_base_brick_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.fragment_container, getFragmentInstance(savedInstanceState))
                .commitAllowingStateLoss()
        }
    }

    abstract fun getFragmentInstance(savedInstanceState: Bundle?): Fragment
}

abstract class BaseGeneralContainerFragment :
    GeneralFragment(R.layout.fragment_base_brick_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.fragment_container, getFragmentInstance(savedInstanceState))
                .commitAllowingStateLoss()
        }
    }

    abstract fun getFragmentInstance(savedInstanceState: Bundle?): Fragment
}