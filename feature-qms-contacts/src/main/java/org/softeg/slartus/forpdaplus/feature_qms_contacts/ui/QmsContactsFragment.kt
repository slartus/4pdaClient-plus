package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
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
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.feature_qms_contacts.R
import org.softeg.slartus.forpdaplus.feature_qms_contacts.databinding.FragmentQmsContactsBinding
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactFingerprint
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactHasNewFingerprint
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class QmsContactsFragment :
    BaseFragment<FragmentQmsContactsBinding>(FragmentQmsContactsBinding::inflate) {

    @Inject
    lateinit var appActions: Provider<AppActions>

    private val viewModel: QmsContactsViewModel by viewModels()

    private var contactsAdapter: QmsContactsAdapter? = null

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
        registerForContextMenu(binding.contactsRecyclerView)
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.onReloadClick()
        }
        subscribeToViewModel()

        binding.contactsRecyclerView.adapter = contactsAdapter
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (v is RecyclerView) {
            val adapter = binding.contactsRecyclerView.adapter as QmsContactsAdapter
            adapter.lastLongClickItem?.let { item ->
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.delete).apply {
                    this.setOnMenuItemClickListener {
                        viewModel.onContactDeleteClick(item as QmsContactItem)
                        true
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden)
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

    private fun createContactsAdapter() = QmsContactsAdapter(
        listOf(
            QmsContactFingerprint(
                squareAvatars = viewModel.squareAvatars,
                showAvatars = viewModel.showAvatars,
                onClickListener = { _, item -> onContactClick(item) }),
            QmsContactHasNewFingerprint(
                squareAvatars = viewModel.squareAvatars,
                showAvatars = viewModel.showAvatars,
                accentBackground = getAccentBackgroundRes(),
                onClickListener = { _, item -> onContactClick(item) })
        )
    )

    private fun onContactClick(contactItem: QmsContactItem) {
        appActions.get()
            .showQmsContactThreads(
                contactItem.id,
                contactItem.nick
            )
    }

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