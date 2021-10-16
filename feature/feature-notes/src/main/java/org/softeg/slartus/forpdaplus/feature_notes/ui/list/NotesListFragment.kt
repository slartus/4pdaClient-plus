package org.softeg.slartus.forpdaplus.feature_notes.ui.list

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.uiMessage
import org.softeg.slartus.forpdaplus.core.di.GenericSavedStateViewModelFactory
import org.softeg.slartus.forpdaplus.core.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.getUrls
import org.softeg.slartus.forpdaplus.feature_notes.databinding.FragmentNotesListBinding
import org.softeg.slartus.forpdaplus.feature_notes.di.UrlManager
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NotesListFragment :
    BaseFragment<FragmentNotesListBinding>(FragmentNotesListBinding::inflate) {
    @Inject
    internal lateinit var notesListViewModelFactory: NotesListViewModelFactory

    private val viewModel: NotesListViewModel by viewModels {
        GenericSavedStateViewModelFactory(notesListViewModelFactory, this, arguments)
    }

    @Inject
    lateinit var urlManager: UrlManager

    @Inject
    lateinit var router: AppRouter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.list)

        val adapter = NotesListAdapter {
            onNoteClick(it.note)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is NotesListState.Initialize -> {
                                setLoading(true)
                            }
                            is NotesListState.Success -> {
                                setLoading(false)
                                adapter.submitList(uiState.items)
                                refreshUi(uiState.items)
                            }
                            is NotesListState.Error -> {
                                setLoading(false)
                                Timber.e(uiState.exception)
                                showError(uiState.exception)
                            }
                        }
                    }
                }
            }
        }

        binding.list.adapter = adapter
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (v is RecyclerView) {
            val adapter = binding.list.adapter as NotesListAdapter
            adapter.lastLongClickItem?.let {
                val note = it.note

                val urls = note.getUrls(requireContext())
                if (urls.any()) {
                    val linksMenu = menu.addSubMenu(R.string.links)
                    urls
                        .forEach { (title, url) ->
                            linksMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, title).apply {
                                this.setOnMenuItemClickListener {
                                    urlManager.openUrl(url)
                                    true
                                }
                            }
                        }
                }
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.delete).apply {
                    this.setOnMenuItemClickListener {
                        showDeleteNoteDialog(note)
                        true
                    }
                }
            }
        }
    }

    private fun onNoteClick(note: Note) {
        router.navigateTo(AppScreen.Note(note.id!!))
    }

    private fun showDeleteNoteDialog(note: Note) {
        MaterialDialog.Builder(requireContext())
            .title(R.string.confirm_action)
            .content(R.string.ask_delete_note)
            .cancelable(true)
            .negativeText(R.string.cancel)
            .positiveText(R.string.delete)
            .onPositive { _: MaterialDialog?, _: DialogAction? ->
                try {
                    viewModel.delete(note.id.toString())
                } catch (ex: Throwable) {
                    Timber.e(ex)
                }
            }
            .show()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressView.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun refreshUi(items: List<NoteListItem>) {
        binding.emptyTextView.visibility = if (items.any()) View.GONE else View.VISIBLE
    }

    private fun showError(exception: Throwable) {
        Toast.makeText(requireContext(), exception.uiMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState()
    }

    companion object {
        internal const val ARG_TOPIC_ID = "NotesListFragment.TOPIC_ID"
        fun newInstance(topicId: String?) = NotesListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TOPIC_ID, topicId)
            }
        }
    }
}



