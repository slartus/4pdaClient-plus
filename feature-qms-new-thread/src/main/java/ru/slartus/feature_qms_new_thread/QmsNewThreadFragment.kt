package ru.slartus.feature_qms_new_thread

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import ru.slartus.feature_qms_new_thread.databinding.FragmentQmsNewThreadBinding
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class QmsNewThreadFragment :
    BaseFragment<FragmentQmsNewThreadBinding>(FragmentQmsNewThreadBinding::inflate) {

    @Inject
    lateinit var appActions: Provider<AppActions>
    private val viewModel: QmsNewThreadViewModel by lazy {
        val viewModel: QmsNewThreadViewModel by viewModels()
        viewModel.setArguments(arguments)
        viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        binding.userNickEditText.addTextChangedListener {
            viewModel.onNickChanged(it?.toString())
        }
        binding.subjectEditText.addTextChangedListener {
            viewModel.onSubjectChanged(it?.toString())
        }
        binding.messageEditText.addTextChangedListener {
            viewModel.onMessageChanged(it?.toString())
        }
        binding.sendButton.setOnClickListener {
            viewModel.onSendClick()
        }
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        onUiState(state)
                    }
                }
                launch {
                    viewModel.events.collect {
                        onEvent(it)
                    }
                }
                launch {
                    viewModel.contact
                        .filterNotNull()
                        .collect {
                            setFragmentResult(
                                ARG_CONTACT_NICK,
                                bundleOf(ARG_CONTACT_NICK to it.nick)
                            )
                        }
                }
            }
        }
    }

    private fun onEvent(event: QmsNewThreadViewModel.Event) {
        viewModel.onEventReceived()
        when (event) {
            QmsNewThreadViewModel.Event.Empty -> {
                // ignore
            }
            is QmsNewThreadViewModel.Event.Error -> Timber.e(event.exception)
            is QmsNewThreadViewModel.Event.Progress -> {
                binding.progressView.isVisible = event.visible
                binding.progressBar.isVisible = event.visible
            }
            is QmsNewThreadViewModel.Event.SendEnable -> {
                binding.sendButton.isEnabled = event.enable
            }
            is QmsNewThreadViewModel.Event.OpenChat -> {
                appActions.get().back(this)
                appActions.get().showQmsThread(
                    event.contactId,
                    event.contactNick,
                    event.threadId,
                    event.threadTitle
                )
            }
            is QmsNewThreadViewModel.Event.Toast -> Toast.makeText(
                requireContext(),
                event.resId,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onUiState(state: QmsNewThreadViewModel.Data) {
        binding.userNickEditText.setTextKeepState(state.model.nick ?: "")
        binding.userNickLayout.isVisible = state.showUserNick
        binding.subjectEditText.setTextKeepState(state.model.subject ?: "")
        binding.messageEditText.setTextKeepState(state.model.message ?: "")
    }

    companion object {

        const val ARG_CONTACT_ID = "QmsNewThreadFragment.CONTACT_ID"
        const val ARG_CONTACT_NICK = "QmsNewThreadFragment.CONTACT_NICK"
    }
}