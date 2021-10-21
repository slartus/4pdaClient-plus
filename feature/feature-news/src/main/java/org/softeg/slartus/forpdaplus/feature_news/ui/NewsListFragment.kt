package org.softeg.slartus.forpdaplus.feature_news.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.getDrawableCompat
import org.softeg.slartus.forpdacommon.uiMessage
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.feature_news.R
import org.softeg.slartus.forpdaplus.feature_news.databinding.FragmentNewsListBinding
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NewsListFragment :
    BaseFragment<FragmentNewsListBinding>(FragmentNewsListBinding::inflate) {

    @Inject
    lateinit var viewModel: NewsListViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = NewsListAdapter()
        adapter
            .withLoadStateHeaderAndFooter(
                header = AppLoadStateAdapter(adapter::retry),
                footer = AppLoadStateAdapter(adapter::retry)
            )
        binding.list.adapter = adapter
        binding.list.addItemDecoration(createDivider(DividerItemDecoration.VERTICAL))

        val columnsCount = resources.getInteger(R.integer.news_list_columns_count)
        if (columnsCount > 1) {
            binding.list.layoutManager = GridLayoutManager(context, columnsCount)
            binding.list.addItemDecoration(createDivider(DividerItemDecoration.HORIZONTAL))
        }
        binding.swipeLayout.setOnRefreshListener {
            adapter.refresh()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    adapter.loadStateFlow.collectLatest { loadState ->
                        val refresh = loadState.refresh
                        val append = loadState.append
                        setLoading(refresh is LoadState.Loading || append is LoadState.Loading)

                        ((refresh as? LoadState.Error)?.error
                            ?: (append as? LoadState.Error)?.error)?.let {
                            showError(it)
                        }
                    }
                }
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is UiState.Initialize -> {
                            }
                            is UiState.Success -> {
                                adapter.submitData(uiState.items)
                                refreshUi(uiState.items)
                            }
                            is UiState.Error -> {
                                Timber.e(uiState.exception)
                                showError(uiState.exception)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createDivider(orientation: Int): DividerItemDecoration {
        return DividerItemDecoration(context, orientation).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context?.getDrawableCompat(R.drawable.news_list_divider)?.let {
                    setDrawable(it)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.swipeLayout.isRefreshing = loading
    }

    private fun refreshUi(items: List<Any>) {
        binding.emptyTextView.isVisible = items.isEmpty()
    }

    private fun showError(exception: Throwable) {
        Toast.makeText(requireContext(), exception.uiMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        super.onDestroyView()
    }

}
