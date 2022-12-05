package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.getListener
import org.softeg.slartus.forpdacommon.showKeyboard
import org.softeg.slartus.forpdaplus.common.impl.R
import org.softeg.slartus.forpdaplus.common.impl.databinding.FragmentBbcodesBinding
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.emotics.*
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment

@AndroidEntryPoint
class BbCodesFragment : BaseFragment<FragmentBbcodesBinding>(FragmentBbcodesBinding::inflate) {
    private val viewModel: BbCodesViewModel by viewModels()
    private var bbCodesListener: BbCodesListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        binding.webView.webViewClient = BbCodesWebViewClient(
            onImageClick = { url ->
                viewModel.obtainEvent(
                    BbCodesEvent.OnUrlClicked(
                        url,
                        requireNotNull(bbCodesListener).getTextInfo()
                    )
                )
            },
            onPageFinished = {
                binding.webView.visibility = View.VISIBLE
            }
        )
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.viewStates.collect { state ->
                        onState(state)
                    }
                }

                launch {
                    viewModel.viewActions.collect { action ->
                        onAction(action)
                    }
                }
            }
        }
    }

    private fun onAction(action: BbCodesAction?) {
        if (action != null)
            viewModel.obtainEvent(BbCodesEvent.ActionInvoked)
        when (action) {
            is BbCodesAction.SendText ->
                bbCodesListener?.onBbCodeSelected(action.text)

            null -> {
                // ignore
            }

            is BbCodesAction.ShowListInputTextDialog ->
                showListInputTextDialog(action.lineNumber)

        }
    }

    private fun showListInputTextDialog(lineNumber: Int) {
        val context = requireContext()

        val input = EditText(context).apply {
            this.hint = getString(R.string.list_next_format, lineNumber)
        }

        val layout = LinearLayout(context).apply {
            setPadding(5, 5, 5, 5)
            orientation = LinearLayout.VERTICAL
            addView(input)
        }

        MaterialDialog.Builder(context)
            .cancelable(false)
            .customView(layout, true)
            .positiveText(R.string.list_more)
            .onPositive { _, _ ->
                viewModel.obtainEvent(BbCodesEvent.OnListInput(input.text.toString()))
            }
            .negativeText(android.R.string.cancel)
            .onNegative { _, _ ->
                viewModel.obtainEvent(BbCodesEvent.OnListInputCanceled)
            }
            .neutralText(R.string.list_finish)
            .onNeutral { _, _ ->
                viewModel.obtainEvent(BbCodesEvent.OnListInputFinished(input.text.toString()))
            }
            .showListener {
                lifecycleScope.launch {
                    delay(300)
                    input.showKeyboard()
                }
            }.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bbCodesListener = getListener<BbCodesListener>(context)
    }

    override fun onDetach() {
        bbCodesListener = null
        super.onDetach()
    }

    private fun onState(state: BbCodesState) {
        state.bbcodesHtml?.let { page ->
            binding.webView.loadDataWithBaseURL(
                "https://4pda.to/forum/",
                page,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    override fun onDestroyView() {
        binding.webView.webViewClient = WebViewClient()
        binding.webView.removeAllViews()
        binding.webView.loadUrl("about:blank")
        super.onDestroyView()
    }
}


interface BbCodesListener {
    fun onBbCodeSelected(text: String)
    fun getTextInfo(): TextInfo
}

class TextInfo(val text: String, val selectionStart: Int, val selectionEnd: Int) {
    val selectedText: String
        get() = when (selectionEnd - selectionStart) {
            0 -> ""
            else -> text.substring(selectionStart, selectionEnd)
        }
}

private class BbCodesWebViewClient(
    private val onImageClick: (url: String) -> Unit,
    private val onPageFinished: () -> Unit
) :
    WebViewClient() {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        onImageClick(url)
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        onImageClick(request.url.toString())
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished()
    }
}