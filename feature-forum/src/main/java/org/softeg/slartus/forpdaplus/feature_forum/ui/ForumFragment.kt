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
import org.softeg.slartus.forpdaplus.core.interfaces.IOnBackPressed
import org.softeg.slartus.forpdaplus.core_lib.ui.adapter.FingerprintAdapter
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.feature_forum.R
import org.softeg.slartus.forpdaplus.feature_forum.databinding.ForumFragmentBinding
import org.softeg.slartus.forpdaplus.feature_forum.di.ForumDependencies
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumDataItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderCurrentItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderItemFingerprint
import org.softeg.slartus.forpdaplus.feature_forum.ui.fingerprints.ForumHeaderNoTopicsItemFingerprint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ForumFragment : BaseFragment<ForumFragmentBinding>(ForumFragmentBinding::inflate),
    IOnBackPressed {

    @Inject
    lateinit var forumDependencies: ForumDependencies

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
            reloadData()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            ForumViewModel.ViewState.Initialize -> {
                                setLoading(true)
                            }
                            is ForumViewModel.ViewState.Success -> {
                                mAdapter?.submitList(uiState.items) {
                                    if (uiState.scrollToTop)
                                        binding.list.scrollToPosition(0)
                                }
                            }
                            is ForumViewModel.ViewState.Error -> {
                                Timber.e(uiState.exception)
                            }
                        }
                    }
                }
                launch {
                    viewModel.loading.collect {
                        setLoading(it)
                    }
                }
            }
        }

        setListViewAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_forum, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mark_forum_as_read -> {
                markAsRead()
                true
            }
            R.id.set_forum_starting -> {

                viewModel.getCurrentForum()?.let { f ->
                    viewModel.setStartForum(f.id, f.title)
                    Toast.makeText(
                        requireContext(),
                        R.string.forum_setted_to_start,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun createAdapter() = FingerprintAdapter(

        listOf(
            ForumHeaderItemFingerprint(
                { _, item -> loadForum(item.id) },
                { _, item ->
                    show(item.id)
                    true
                }),
            ForumHeaderCurrentItemFingerprint(
                { _, item -> forumDependencies.showForumTopicsList(item.id, item.title) },
                { _, item ->
                    show(item.id)
                    true
                }),
            ForumHeaderNoTopicsItemFingerprint(
                { _, item -> loadForum(item.id) },
                { _, item ->
                    show(item.id)
                    true
                }),
            ForumDataItemFingerprint(
                viewModel.showImages,
                { _, item ->
                    if (item.isHasForums) {
                        loadForum(item.id)
//                        val searchSettings = SearchSettings()
//                        searchSettings.source = "all"
//                        searchSettings.forumsIds.add(item.id + "")
//                        mSearchSetting = searchSettings
//                        MainActivity.searchSettings = mSearchSetting
                    } else {
                        forumDependencies.showForumTopicsList(item.id, item.title)
                    }
                },
                { _, item ->
                    show(item.id)
                    true
                })
        )
    )

    private fun markAsRead() {
        if (!viewModel.isLogined()) {
            Toast.makeText(activity, R.string.need_login, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.confirm_action)
            .setMessage(getString(R.string.mark_forum_as_read) + "?")
            .setPositiveButton(
                R.string.yes
            ) { _, _ -> markForumRead() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun markForumRead() {
        Toast.makeText(activity, R.string.request_sent, Toast.LENGTH_SHORT).show()

        try {
            viewModel.getCurrentForum()?.let { f ->
                viewModel.markForumRead(f.id ?: "-1")
            }
            Toast.makeText(
                activity,
                R.string.forum_setted_read,
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Throwable) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
            Timber.e(e)
        }

    }

    private fun reloadData() {
        viewModel.reload()
    }

    private fun loadForum(forumId: String?) {
        viewModel.refreshDataState(forumId)
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

    private fun show(id: String?) {
//        ExtUrl.showSelectActionDialog(
//            mainActivity,
//            getString(R.string.link),
//            "https://${HostHelper.host}/forum/index.php?showforum=$id"
//        )

    }

    override fun onBackPressed(): Boolean {
        return viewModel.onBack()
    }

    companion object {
        const val FORUM_ID_KEY = "FORUM_ID_KEY"
    }
}
