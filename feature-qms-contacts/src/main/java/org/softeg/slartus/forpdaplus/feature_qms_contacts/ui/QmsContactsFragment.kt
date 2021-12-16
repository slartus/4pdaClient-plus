package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.feature_qms_contacts.databinding.QmsContactsFragmentBinding

@AndroidEntryPoint
class QmsContactsFragment :
    BaseFragment<QmsContactsFragmentBinding>(QmsContactsFragmentBinding::inflate) {
    private val viewModel: QmsContactsViewModel by viewModels()
}