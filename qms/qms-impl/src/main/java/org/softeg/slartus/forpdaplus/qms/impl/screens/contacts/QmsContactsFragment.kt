package org.softeg.slartus.forpdaplus.qms.impl.screens.contacts

import android.os.Bundle
import android.view.*
import android.widget.SearchView
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
import org.softeg.slartus.forpdaplus.qms.impl.R
import org.softeg.slartus.forpdaplus.qms.impl.databinding.FragmentQmsContactsBinding
import org.softeg.slartus.forpdaplus.qms.impl.screens.contacts.fingerprints.QmsContactFingerprint
import org.softeg.slartus.forpdaplus.qms.impl.screens.contacts.fingerprints.QmsContactHasNewFingerprint
import org.softeg.slartus.forpdaplus.qms.impl.screens.contacts.fingerprints.QmsContactItem
import ru.softeg.slartus.common.api.AppAccentColor
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_qms_contacts, menu)


        menu.findItem(R.id.menu_item_forum_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        val searchViewItem = menu.findItem(R.id.qms_contacts_search_item)
        val searchView: SearchView = searchViewItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.obtainEvent(QmsContactsEvent.OnSearchTextChanged(newText.orEmpty()))
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.qms_contacts_search_item -> {
                item.actionView.requestFocus()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
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
        viewModel.obtainEvent(QmsContactsEvent.HiddenChanged(hidden))
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

    private fun onEvent(event: QmsContactsAction) {
        viewModel.onEventReceived()
        when (event) {
            is QmsContactsAction.Error -> {
                Timber.e(event.exception)
            }
            is QmsContactsAction.ShowToast -> {
                Toast.makeText(requireContext(), event.resId, event.duration).show()
            }
            QmsContactsAction.Empty -> {
                // ignore
            }
        }
    }

    private fun onUiState(state: QmsContactsState) {
        contactsAdapter?.submitList(state.filteredItems)
        binding.emptyTextView.isVisible = state.filteredItems.isEmpty()
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
            AppAccentColor.Blue -> R.drawable.qmsnewblue
            AppAccentColor.Gray -> R.drawable.qmsnewgray
            AppAccentColor.Pink -> R.drawable.qmsnewpink
        }
    }

    override fun onDestroyView() {
        binding.contactsRecyclerView.adapter = null
        super.onDestroyView()
    }
}