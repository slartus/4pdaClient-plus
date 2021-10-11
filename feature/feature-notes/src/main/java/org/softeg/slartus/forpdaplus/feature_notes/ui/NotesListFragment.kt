package org.softeg.slartus.forpdaplus.feature_notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.feature_notes.databinding.FragmentNotesListBinding
import timber.log.Timber

@AndroidEntryPoint
class NotesListFragment : Fragment() {
    private val viewModel: NotesListViewModel by viewModels()
    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NotesListAdapter(
            {
            },
            {
            }
        )

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

    private fun setLoading(loading: Boolean) {
        binding.progressView.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun refreshUi(items: List<NoteListItem>) {
        binding.emptyTextView.visibility = if (items.any()) View.GONE else View.VISIBLE
    }

    private fun showError(exception: Throwable) {
        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = NotesListFragment()
    }
}



