package org.softeg.slartus.forpdaplus.fragments.qms

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.QmsContactsFragment
import org.softeg.slartus.forpdaplus.fragments.BaseBrickContainerFragment

class QmsContactsList : BaseBrickContainerFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return QmsContactsFragment().apply {
            this.arguments = args
        }
    }
}