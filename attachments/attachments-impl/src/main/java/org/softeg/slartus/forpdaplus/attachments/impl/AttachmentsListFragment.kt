package org.softeg.slartus.forpdaplus.attachments.impl

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.attachments.impl.databinding.FragmentAttachmentsListBinding
import org.softeg.slartus.forpdaplus.attachments.impl.models.AttachmentsListAction
import org.softeg.slartus.forpdaplus.attachments.impl.models.AttachmentsListEvent
import org.softeg.slartus.forpdaplus.attachments.impl.models.AttachmentsListState
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment


@AndroidEntryPoint
class AttachmentsListFragment :
    BaseFragment<FragmentAttachmentsListBinding>(FragmentAttachmentsListBinding::inflate) {
    private val viewModel: AttachmentsListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.viewStates.collect { state ->
                        onState(state)
                    }
                }

                launch {
                    viewModel.viewActions.collect { action ->
                        onAction(action)
                    }
                }
            }
        }
    }

    private fun onAction(action: AttachmentsListAction?) {
        if (action != null)
            viewModel.obtainEvent(AttachmentsListEvent.ActionInvoked)
        when (action) {

        }
    }

    private fun onState(state: AttachmentsListState) {
        with(binding) {

        }
    }
}