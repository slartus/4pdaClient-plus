package org.softeg.slartus.forpdaplus.feature_notes.ui.note

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
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
import org.softeg.slartus.forpdaplus.core.di.GenericSavedStateViewModelFactory
import org.softeg.slartus.forpdaplus.core.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.core_ui.html.HtmlBuilder
import org.softeg.slartus.forpdaplus.core_ui.html.HtmlStylePreferences
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.R
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

        binding.infoTable.removeAllViews()
        val rowparams = TableLayout.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val textviewparams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        if (!TextUtils.isEmpty(note.title)) {
            addRow(getString(R.string.theme), note.title, null, rowparams, textviewparams)
        }


        if (!TextUtils.isEmpty(note.topicTitle)) {
            addLinkRow(
                getString(R.string.topic),
                note.topicUrl,
                rowparams,
                textviewparams
            )
        }

        if (!TextUtils.isEmpty(note.userName)) {
            addLinkRow(
                getString(R.string.user),
                note.userUrl,
                rowparams,
                textviewparams
            )
        }

        if (!TextUtils.isEmpty(note.url)) {
            addLinkRow(getString(R.string.link), note.url, rowparams, textviewparams)
        }

        binding.webView.loadDataWithBaseURL(
            "https://" + HostHelper.host + "/forum/",
            transformChatBody(note.body ?: ""),
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun addLinkRow(
        title: String?, url: String?,
        rowparams: TableLayout.LayoutParams, textviewparams: TableRow.LayoutParams
    ) {
        addRow(title, UrlExtensions.urlToHref(url, title), url, rowparams, textviewparams)
    }

    private fun addRow(
        title: String?, text: String?, url: String?,
        rowparams: TableLayout.LayoutParams, textviewparams: TableRow.LayoutParams
    ) {
        var row = TableRow(context)
        val textView: TextView = createFirstTextView()
        textView.text = title
        row.addView(textView, textviewparams)
        binding.infoTable.addView(row, rowparams)
        row = TableRow(context)
        val textView2: TextView = createSecondTextView()
        textView2.text = text.fromHtml()
        textView2.ellipsize = null
        textView2.setOnClickListener {
            if (!url.isNullOrEmpty())
                router.openUrl(url)
        }
        // TODO: вернуть меню для ссылки
//        textView2.setOnLongClickListener { view: View? ->
//            if (!url.isNullOrEmpty())
//                router.openUrl(url)
//
//            true
//        }
        row.addView(textView2, textviewparams)
        binding.infoTable.addView(row, rowparams)
    }

    private fun createFirstTextView(): TextView {
        return layoutInflater.inflate(R.layout.note_first_textview, null) as TextView
    }

    private fun createSecondTextView(): TextView {
        return layoutInflater.inflate(R.layout.note_second_textview, null) as TextView
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