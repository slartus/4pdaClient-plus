package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.FingerprintAdapter
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.feature_qms_contacts.R
import org.softeg.slartus.forpdaplus.feature_qms_contacts.databinding.FragmentQmsContactsBinding
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactFingerprint
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactHasNewFingerprint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class QmsContactsFragment :
    BaseFragment<FragmentQmsContactsBinding>(FragmentQmsContactsBinding::inflate) {

    @Inject
    lateinit var appActions: AppActions

    private val viewModel: QmsContactsViewModel by viewModels()

    private var contactsAdapter: FingerprintAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        contactsAdapter = createContactsAdapter().apply {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.onReloadClick()
        }
        subscribeToViewModel()

        binding.contactsRecyclerView.adapter = contactsAdapter
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
            }
        }
    }

    private fun onEvent(event: QmsContactsViewModel.Event) {
        viewModel.onEventReceived()
        when (event) {
            is QmsContactsViewModel.Event.Error -> {
                Timber.e(event.exception)
            }
            is QmsContactsViewModel.Event.ShowToast -> {
                Toast.makeText(requireContext(), event.resId, event.duration).show()
            }
            QmsContactsViewModel.Event.Empty -> {
                // ignore
            }
            is QmsContactsViewModel.Event.OpenContactThreads -> appActions.showQmsContactThreads(
                event.contactId,
                event.contactNick
            )
        }
    }

    private fun onUiState(state: QmsContactsViewModel.UiState) {
        when (state) {
            QmsContactsViewModel.UiState.Initialize -> {
                // nothing
            }
            is QmsContactsViewModel.UiState.Items -> {
                contactsAdapter?.submitList(state.items)
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

    private fun createContactsAdapter() = FingerprintAdapter(
        listOf(
            QmsContactFingerprint(
                squareAvatars = viewModel.squareAvatars,
                showAvatars = viewModel.showAvatars,
                onClickListener = { _, item -> viewModel.onContactClick(item) },
                onLongClickListener = { _, item ->
                    viewModel.onContactLongClick(item)
                    true
                }),
            QmsContactHasNewFingerprint(
                squareAvatars = viewModel.squareAvatars,
                showAvatars = viewModel.showAvatars,
                accentBackground = getAccentBackgroundRes(),
                onClickListener = { _, item -> viewModel.onContactClick(item) },
                onLongClickListener = { _, item ->
                    viewModel.onContactLongClick(item)
                    true
                })
        )
    )

    @DrawableRes
    private fun getAccentBackgroundRes(): Int {
        return when (viewModel.accentColor) {
            QmsContactsViewModel.AccentColor.Blue -> R.drawable.qmsnewblue
            QmsContactsViewModel.AccentColor.Gray -> R.drawable.qmsnewgray
            else -> R.drawable.qmsnew
        }
    }

    override fun onDestroyView() {
        binding.contactsRecyclerView.adapter = null
        super.onDestroyView()
    }
}