package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.emotics

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import ru.softeg.slartus.common.api.models.Emotic
import ru.softeg.slartus.common.api.repositories.EmoticsRepository
import javax.inject.Inject

@HiltViewModel
class EmoticsViewModel @Inject constructor(
    private val emoticsRepository: EmoticsRepository
) : BaseViewModel<EmoticsState, EmoticsAction, EmoticsEvent>(EmoticsState()) {
    init {
        fetchData()
    }

    override fun obtainEvent(viewEvent: EmoticsEvent) = when (viewEvent) {
        is EmoticsEvent.OnUrlClicked -> handleOnUrlClicked(viewEvent.url)
        EmoticsEvent.ActionInvoked -> viewAction = null
    }

    private fun fetchData() {
        viewModelScope.launch {
            val emotics = emoticsRepository.loadEmotics()
            val favorite = emoticsRepository.loadFavoriteEmotics()
            viewState = viewState.copy(emoticsHtml = createEmoticsHtmlPage(emotics, favorite))
        }
    }

    private suspend fun createEmoticsHtmlPage(
        emotics: List<Emotic>,
        favorite: List<Emotic>
    ): String =
        withContext(Dispatchers.Default) {
            buildString {
                append("<html><body>")
                //        sb.append("<html><body bgcolor=\"").append(AppTheme.currentBackgroundColorHtml)
                if (favorite.isNotEmpty()) {
                    appendEmotics(favorite)
                    append("<hr/>")
                }
                appendEmotics(emotics)
                append("</body></html>")
            }
        }

    private fun handleOnUrlClicked(url: String) {
        val emoticCode = url.substringAfterLast('/')
        viewAction = EmoticsAction.SendText(" $emoticCode ")
        viewModelScope.launch {
            emoticsRepository.loadEmotics().find { it.code == emoticCode }?.let { emotic ->
                emoticsRepository.addFavoriteEmotic(emotic)
            }
        }
    }

    companion object {
        private fun StringBuilder.appendEmotics(emotics: List<Emotic>) {
            emotics.forEach { emotic ->
                append("<a style=\"text-decoration: none;\" href=\"${emotic.code}\">")
                append("<img style=\"padding:5px;\" src=\"file:///android_asset/forum/style_emoticons/default/${emotic.image}\" />")
                append("</a>")
            }
        }
    }
}

data class EmoticsState(
    val emoticsHtml: String? = null
)

sealed class EmoticsAction {
    class SendText(val text: String) : EmoticsAction()
}

sealed class EmoticsEvent {
    object ActionInvoked : EmoticsEvent()
    class OnUrlClicked(val url: String) : EmoticsEvent()
}