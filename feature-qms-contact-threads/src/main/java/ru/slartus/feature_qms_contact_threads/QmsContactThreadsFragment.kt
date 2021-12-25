package ru.slartus.feature_qms_contact_threads

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.AppActions
import org.softeg.slartus.forpdaplus.core.interfaces.IOnBackPressed
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import ru.slartus.feature_qms_contact_threads.databinding.FragmentQmsContactThreadsBinding
import ru.slartus.feature_qms_contact_threads.fingerprints.QmsThreadFingerprint
import ru.slartus.feature_qms_contact_threads.fingerprints.QmsThreadSelectableFingerprint
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class QmsContactThreadsFragment :
    BaseFragment<FragmentQmsContactThreadsBinding>(FragmentQmsContactThreadsBinding::inflate),
    IOnBackPressed {

    @Inject
    lateinit var appActions: Provider<AppActions>

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
        binding.newThreadFab.setOnClickListener {
            viewModel.onNewThreadClick()
        }
        binding.deleteThreadFab.setOnClickListener {
            viewModel.onDeleteSelectionClick()
        }
        subscribeToViewModel()

        binding.threadsRecyclerView.adapter = threadsAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_qms_threads, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_interlocutor_item -> {
                viewModel.onContactProfileClick()
            }
        }
        return super.onOptionsItemSelected(item)
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
                    viewModel.contact
                        .filterNotNull()
                        .collect {
                            setFragmentResult(
                                ARG_CONTACT_NICK,
                                bundleOf(ARG_CONTACT_NICK to it.nick)
                            )
                        }
                }
                launch {
                    viewModel.selectionMode
                        .filterNotNull()
                        .collect {
                            binding.deleteThreadFab.isVisible = it
                            binding.newThreadFab.isVisible = !it
                        }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden)
    }

    private fun onEvent(event: QmsContactThreadsViewModel.Event) {
        viewModel.onEventReceived()
        when (event) {
            QmsContactThreadsViewModel.Event.Empty -> {
                // ignore
            }
            is QmsContactThreadsViewModel.Event.Error -> Timber.e(event.exception)

            is QmsContactThreadsViewModel.Event.ShowQmsThread ->
                appActions.get().showQmsThread(
                    event.contactId,
                    event.contactNick,
                    event.threadId,
                    event.threadTitle
                )
            is QmsContactThreadsViewModel.Event.ShowContactProfile -> appActions.get()
                .showUserProfile(event.contactId, event.contactNick)
            is QmsContactThreadsViewModel.Event.ShowNewThread -> appActions.get()
                .showNewQmsContactThread(event.contactId, event.contactNick)
            is QmsContactThreadsViewModel.Event.ShowConfirmDeleteDialog -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_action)
                    .setMessage(
                        getString(
                            R.string.ask_deleting_dialogs,
                            event.selectedIds.size,
                            event.userNick
                        )
                    ).setPositiveButton(
                        R.string.ok
                    ) { dialog, _ ->
                        dialog.dismiss()
                        viewModel.onConfirmDeleteSelectionClick(event.selectedIds)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show()
            }
            is QmsContactThreadsViewModel.Event.Progress -> {
                binding.deleteThreadFab.isEnabled = !event.visible
                binding.newThreadFab.isEnabled = !event.visible
                binding.progressBcgView.isVisible = event.visible
                binding.progressBar.isVisible = event.visible
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
                accentBackground = getAccentBackgroundRes(),
                onClickListener = { _, item -> viewModel.onThreadClick(item) },
                onLongClickListener = { _, _ ->
                    viewModel.onLongClickListener()
                    true
                }),
            QmsThreadSelectableFingerprint(
                accentBackground = getAccentBackgroundRes(),
                onClickListener = { _, item ->
                    viewModel.onThreadSelectableClick(item)
                }),
        )
    )

    @DrawableRes
    private fun getAccentBackgroundRes(): Int {
        return when (viewModel.accentColor) {
            QmsContactThreadsViewModel.AccentColor.Blue -> R.drawable.qmsnewblue
            QmsContactThreadsViewModel.AccentColor.Gray -> R.drawable.qmsnewgray
            else -> R.drawable.qmsnew
        }
    }

    override fun onDestroyView() {
        binding.threadsRecyclerView.adapter = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_CONTACT_ID = "QmsContactThreadsFragment.CONTACT_ID"
        const val ARG_CONTACT_NICK = "QmsContactThreadsFragment.CONTACT_NICK"
    }

    override fun onBackPressed(): Boolean {
        return viewModel.onBack()
    }
}