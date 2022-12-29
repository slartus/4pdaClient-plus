package org.softeg.slartus.forpdaplus.topic.impl.screens.attachments

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
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
import org.softeg.slartus.forpdaplus.topic.impl.R
import org.softeg.slartus.forpdaplus.topic.impl.databinding.FragmentTopicAttachmentsBinding
import org.softeg.slartus.forpdaplus.topic.impl.screens.attachments.fingerprints.TopicAttachmentFingerprint
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class TopicAttachmentsFragment :
    BaseFragment<FragmentTopicAttachmentsBinding>(FragmentTopicAttachmentsBinding::inflate) {

    @Inject
    lateinit var appActions: Provider<AppActions>

    private val viewModel: TopicAttachmentsViewModel by lazy {
        val viewModel: TopicAttachmentsViewModel by viewModels()
        val topicId = arguments?.getString(ARG_TOPIC_ID, null)
            ?: error("contactId not initialized")
        viewModel.setArguments(topicId)
        viewModel
    }

    private var adapter: TopicAttachmentsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        adapter = createContactsAdapter().apply {
            this.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.obtainEvent(TopicAttachmentsEvent.ReloadClicked)
        }
        subscribeToViewModel()

        binding.attachmentsRecyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_topic_attachments, menu)

        menu.findItem(R.id.menu_item_forum_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        val searchViewItem = menu.findItem(R.id.topic_attachments_search_item)
        val searchView: SearchView = searchViewItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.obtainEvent(TopicAttachmentsEvent.OnFilterTextChanged(newText.orEmpty()))
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.change_order_item -> {
                viewModel.obtainEvent(TopicAttachmentsEvent.OnReverseOrderClicked)
                true
            }
            R.id.topic_attachments_search_item -> {
                item.actionView.requestFocus()
                true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    private fun subscribeToViewModel() {
        viewModel.obtainEvent(TopicAttachmentsEvent.ReloadClicked)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.viewStates.collect { state ->
                        onUiState(state)
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.obtainEvent(TopicAttachmentsEvent.OnHiddenChanged(hidden))
    }

    private fun onUiState(state: TopicAttachmentsViewState) {
        setLoading(state.loading)
        adapter?.submitList(state.filteredItems)
        binding.emptyTextView.isVisible = state.attachments.isEmpty() && !state.loading
    }

    private fun setLoading(loading: Boolean) {
        try {
            if (activity == null) return
            binding.swipeToRefresh.isRefreshing = loading
        } catch (ignore: Throwable) {
            Timber.w("TAG", ignore.toString())
        }
    }

    private fun createContactsAdapter() = TopicAttachmentsAdapter(
        listOf(
            TopicAttachmentFingerprint(
                onClickListener = { _, item ->
                    showAttachmentActionDialog(item)
                },
            )
        )
    )

    private fun showAttachmentActionDialog(attachmentModel: TopicAttachmentModel) {
        val actions = listOf(
            getString(R.string.do_download) to {
                appActions.get().startDownload(attachmentModel.url)
            },
            getString(R.string.jump_to_page) to {
                appActions.get().openTopic(attachmentModel.postUrl)
            },
            getString(R.string.link) to {
                appActions.get()
                    .showUrlActions(requireContext(), attachmentModel.url, attachmentModel.url)
            }
        )
        AlertDialog.Builder(requireActivity())
            .setTitle(attachmentModel.name)
            .setItems(actions.map { it.first }
                .toTypedArray()) { _, i -> actions[i].second.invoke() }
            .setCancelable(true)
            .show()
    }

    override fun onDestroyView() {
        binding.attachmentsRecyclerView.adapter = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_TOPIC_ID = "TopicAttachmentsFragment.ARG_TOPIC_ID"
    }
}