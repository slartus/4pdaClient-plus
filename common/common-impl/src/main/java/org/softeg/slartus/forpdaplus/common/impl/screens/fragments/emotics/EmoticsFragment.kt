package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.emotics

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.getListener
import org.softeg.slartus.forpdaplus.common.impl.databinding.FragmentEmoticsBinding
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment

@AndroidEntryPoint
class EmoticsFragment : BaseFragment<FragmentEmoticsBinding>(FragmentEmoticsBinding::inflate) {
    private val viewModel: EmoticsViewModel by viewModels()

    private var emoticsListener: EmoticsListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        binding.webView.webViewClient = EmoticsWebViewClient { url ->
            viewModel.obtainEvent(EmoticsEvent.OnUrlClicked(url))
        }
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

    private fun onAction(action: EmoticsAction?) {
        when (action) {
            is EmoticsAction.SendText -> {
                viewModel.obtainEvent(EmoticsEvent.ActionInvoked)
                emoticsListener?.onEmoticSelected(action.text)
            }
            null -> {
                // ignore
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        emoticsListener = getListener<EmoticsListener>(context)
    }

    override fun onDetach() {
        emoticsListener = null
        super.onDetach()
    }

    private fun onState(state: EmoticsState) {
        state.emoticsHtml?.let { page ->
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

interface EmoticsListener {
    fun onEmoticSelected(text: String)
}

private class EmoticsWebViewClient(private val onImageClick: (url: String) -> Unit) :
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
}