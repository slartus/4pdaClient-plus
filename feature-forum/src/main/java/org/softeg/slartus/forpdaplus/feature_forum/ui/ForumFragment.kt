package org.softeg.slartus.forpdaplus.feature_forum.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.core.LinkManager
import org.softeg.slartus.forpdaplus.core.interfaces.IOnBackPressed
import org.softeg.slartus.forpdaplus.core.interfaces.SearchSettingsListener
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.FingerprintAdapter
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.feature_forum.R
import org.softeg.slartus.forpdaplus.feature_forum.databinding.ForumFragmentBinding
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumDataItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderCurrentItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderNoTopicsItemFingerprint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ForumFragment : BaseFragment<ForumFragmentBinding>(ForumFragmentBinding::inflate),
    IOnBackPressed, SearchSettingsListener {
    @Inject
    lateinit var linkManager: LinkManager

    private val viewModel: ForumViewModel by lazy {
        val viewModel: ForumViewModel by viewModels()
        viewModel.setArguments(arguments)
        viewModel
    }

    private var mAdapter: FingerprintAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = createAdapter().apply {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.reload()
        }
        subscribeToViewModel()

        setListViewAdapter()
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

    private fun onUiState(state: ForumViewModel.UiState) {
        when (state) {
            ForumViewModel.UiState.Initialize -> {
                // nothing
            }
            is ForumViewModel.UiState.Items -> {
                mAdapter?.submitList(state.items) {
                    if (state.scrollToTop)
                        binding.list.scrollToPosition(0)
                }
            }
        }
    }

    private fun onEvent(event: ForumViewModel.Event) {
        viewModel.onEventReceived()
        when (event) {
            is ForumViewModel.Event.Error -> {
                Timber.e(event.exception)
            }
            is ForumViewModel.Event.ShowToast -> {
                Toast.makeText(requireContext(), event.resId, event.duration).show()
            }
            ForumViewModel.Event.MarkAsReadConfirmDialog -> showMarkAsReadConfirmDialog()
            is ForumViewModel.Event.ShowUrlMenu -> linkManager.showUrlActions(
                requireContext(),
                R.string.link,
                event.url
            )
            ForumViewModel.Event.Empty -> {
                // ignore
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_forum, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mark_forum_as_read -> {
                viewModel.onMarkAsReadClick()
                true
            }
            R.id.set_forum_starting -> {
                viewModel.onSetForumStartingClick()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun createAdapter() = FingerprintAdapter(
        listOf(
            ForumHeaderItemFingerprint(
                { _, item -> viewModel.onCrumbClick(item.id) },
                { _, item ->
                    viewModel.onCrumbLongClick(item.id)
                    true
                }),
            ForumHeaderCurrentItemFingerprint(
                { _, item -> viewModel.onCrumbClick(item.id) },
                { _, item ->
                    viewModel.onCrumbLongClick(item.id)
                    true
                }),
            ForumHeaderNoTopicsItemFingerprint(
                { _, item -> viewModel.onCrumbClick(item.id) },
                { _, item ->
                    viewModel.onCrumbLongClick(item.id)
                    true
                }),
            ForumDataItemFingerprint(
                viewModel.showImages,
                { _, item -> viewModel.onForumClick(item.id) },
                { _, item ->
                    viewModel.onForumLongClick(item.id)
                    true
                })
        )
    )

    private fun showMarkAsReadConfirmDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.confirm_action)
            .setMessage(getString(R.string.mark_forum_as_read) + "?")
            .setPositiveButton(
                R.string.yes
            ) { _, _ -> viewModel.onMarkAsReadConfirmClick() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        try {
            if (activity == null) return
            binding.swipeToRefresh.isRefreshing = loading
        } catch (ignore: Throwable) {
            Timber.w("TAG", ignore.toString())
        }
    }

    private fun setListViewAdapter() {
        binding.list.adapter = mAdapter
    }

    override fun onBackPressed(): Boolean {
        return viewModel.onBack()
    }

    override fun getSearchSettings() = viewModel.getSearchSettings()

    companion object {
        const val FORUM_ID_KEY = "FORUM_ID_KEY"
    }
}
