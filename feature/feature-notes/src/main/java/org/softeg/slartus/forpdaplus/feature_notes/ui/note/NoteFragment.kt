package org.softeg.slartus.forpdaplus.feature_notes.ui.note

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.UrlExtensions
import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdaplus.core_ui.di.GenericSavedStateViewModelFactory
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.core_ui.AppTheme.Companion.themeStyleWebViewBackground
import org.softeg.slartus.forpdaplus.core_ui.html.HtmlBuilder
import org.softeg.slartus.forpdaplus.core_ui.html.HtmlStylePreferences
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.postUrl
import org.softeg.slartus.forpdaplus.feature_notes.data.topicUrl
import org.softeg.slartus.forpdaplus.feature_notes.data.userUrl
import org.softeg.slartus.forpdaplus.feature_notes.databinding.FragmentNoteBinding
import org.softeg.slartus.hosthelper.HostHelper
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NoteFragment : BaseFragment<FragmentNoteBinding>(FragmentNoteBinding::inflate) {
    @Inject
    internal lateinit var viewModelFactory: NoteViewModelFactory

    @Inject
    internal lateinit var router: AppRouter

    @Inject
    internal lateinit var htmlStylePreferences: HtmlStylePreferences

    private val viewModel: NoteViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webView.setBackgroundColor(themeStyleWebViewBackground)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is NoteUIState.Initialize -> {
                                setLoading(true)
                            }
                            is NoteUIState.Success -> {
                                setLoading(false)
                                updateUi(uiState.item)
                            }
                            is NoteUIState.Error -> {
                                setLoading(false)
                                Timber.e(uiState.exception)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(note: Note) {
        binding.titleTextView.text = note.title
        binding.titleTextView.visibility =
            if (note.title.isNullOrEmpty() || note.title == note.topicTitle) View.GONE else View.VISIBLE

        setTextViewUrl(
            binding.topicTextView,
            note.topicUrl,
            note.title ?: getString(R.string.topic)
        )

        setTextViewUrl(
            binding.postTextView,
            note.postUrl,
            getString(R.string.post) + "#${note.postId}"
        )

        setTextViewUrl(
            binding.userTextView,
            note.userUrl,
            note.userName ?: getString(R.string.user)
        )

        setTextViewUrl(binding.urlTextView, note.url, getString(R.string.link))

        binding.webView.loadDataWithBaseURL(
            "https://" + HostHelper.host + "/forum/",
            transformChatBody(note.body ?: ""),
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun setTextViewUrl(textView: TextView, url: String?, title: String?) {
        textView.text = UrlExtensions.urlToHref(url, title).fromHtml()
        textView.visibility = if (url.isNullOrEmpty()) View.GONE else View.VISIBLE
        textView.setOnClickListener {
            url?.let {
                router.openUrl(it)
            }
        }
    }

    private fun transformChatBody(body: String): String {

        val htmlBuilder = HtmlBuilder(htmlStylePreferences)
        htmlBuilder.beginHtml(getString(R.string.note))
        htmlBuilder.append("<div class=\"emoticons\">")
        htmlBuilder.append(body)
        htmlBuilder.append("</div>")
        htmlBuilder.endBody()
        htmlBuilder.endHtml()
        return htmlBuilder.getHtml()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressView.visibility = if (loading) View.VISIBLE else View.GONE
    }

    companion object {
        internal const val ARG_NOTE_ID = "NoteFragment.NOTE_ID"
        fun newInstance(noteId: Int) = NoteFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_NOTE_ID, noteId)
            }
        }
    }
}