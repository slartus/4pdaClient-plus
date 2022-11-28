package org.softeg.slartus.forpdaplus.qms.impl.screens.thread

import android.os.Bundle
import android.os.Message
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.qms.impl.databinding.FragmentQmsMessageBinding
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageAction
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageEvent
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageState


class MessageFragment :
    BaseFragment<FragmentQmsMessageBinding>(FragmentQmsMessageBinding::inflate) {

    private val viewModel: MessageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        with(binding) {
            messageEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.obtainEvent(MessageEvent.MessageTextChanged(text?.toString().orEmpty()))
            }
        }
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

    private fun onState(state: MessageState) {
        with(binding) {
            messageEditText.setTextKeepState(state.message)
            sendButton.visibility = if (state.sendButtonVisible) View.VISIBLE else View.GONE
        }
    }

    private fun onAction(action: MessageAction?) {
        when (action) {

        }
    }
}