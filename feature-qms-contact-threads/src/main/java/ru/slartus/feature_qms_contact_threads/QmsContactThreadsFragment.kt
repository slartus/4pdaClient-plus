package ru.slartus.feature_qms_contact_threads

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import ru.slartus.feature_qms_contact_threads.databinding.FragmentQmsContactThreadsBinding
import ru.slartus.feature_qms_contact_threads.fingerprints.QmsThreadFingerprint
import timber.log.Timber

@AndroidEntryPoint
class QmsContactThreadsFragment :
    BaseFragment<FragmentQmsContactThreadsBinding>(FragmentQmsContactThreadsBinding::inflate) {
    private val viewModel: QmsContactThreadsViewModel by lazy {
        val viewModel: QmsContactThreadsViewModel by viewModels()
        viewModel.setArguments(arguments)
        viewModel
    }

    private var threadsAdapter: QmsContactThreadsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        threadsAdapter = createContactsAdapter().apply {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.threadsRecyclerView)
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.onReloadClick()
        }
        subscribeToViewModel()

        binding.threadsRecyclerView.adapter = threadsAdapter
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        onUiState(state)
                    }
                }
                launch {
                    viewModel.loading.collect {
                        setLoading(it)
                    }
                }
                launch {
                    viewModel.events.collect {
                        onEvent(it)
                    }
                }
                launch {
                    viewModel.contact.collect {
                        Timber.i(it?.nick)
                    }
                }
            }
        }
    }

    private fun onEvent(event: QmsContactThreadsViewModel.Event) {
        viewModel.onEventReceived()
        when (event) {
            is QmsContactThreadsViewModel.Event.Error -> {
                Timber.e(event.exception)
            }
            is QmsContactThreadsViewModel.Event.ShowToast -> {
                Toast.makeText(requireContext(), event.resId, event.duration).show()
            }
            QmsContactThreadsViewModel.Event.Empty -> {
                // ignore
            }
        }
    }

    private fun onUiState(state: QmsContactThreadsViewModel.UiState) {
        when (state) {
            QmsContactThreadsViewModel.UiState.Initialize -> {
                // nothing
            }
            is QmsContactThreadsViewModel.UiState.Items -> {
                threadsAdapter?.submitList(state.items)
                binding.emptyTextView.isVisible = state.items.isEmpty()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        try {
            if (activity == null) return
            binding.swipeToRefresh.isRefreshing = loading
        } catch (ignore: Throwable) {
            Timber.w("TAG", ignore.toString())
        }
    }

    private fun createContactsAdapter() = QmsContactThreadsAdapter(
        listOf(
            QmsThreadFingerprint(
                onClickListener = { _, item -> }),
        )
    )

    override fun onDestroyView() {
        binding.threadsRecyclerView.adapter = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_CONTACT_ID = "QmsContactThreadsFragment.CONTACT_ID"
    }
}