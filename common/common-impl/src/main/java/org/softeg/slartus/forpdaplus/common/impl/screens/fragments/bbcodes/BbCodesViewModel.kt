package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import ru.softeg.slartus.common.api.models.BbCode
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BbCodesViewModel @Inject constructor() :
    BaseViewModel<BbCodesState, BbCodesAction, BbCodesEvent>(BbCodesState()) {
    private val lines = mutableListOf<String>()

    init {
        fetchData()
    }

    override fun obtainEvent(viewEvent: BbCodesEvent) = when (viewEvent) {
        BbCodesEvent.ActionInvoked -> viewAction = null
        is BbCodesEvent.OnUrlClicked -> handleOnUrlClicked(viewEvent.url, viewEvent.textInfo)
    }

    private fun fetchData() {
        viewModelScope.launch {
            viewState = viewState.copy(bbcodesHtml = createHtmlPage())
        }
    }

    private suspend fun createHtmlPage(): String =
        withContext(Dispatchers.Default) {
            buildString {
                append("<html>")
                append("<body>")
                //        sb.append("<html><body bgcolor=\"").append(AppTheme.currentBackgroundColorHtml)
                val path =
                    "file:///android_asset/forum/style_images/1/folder_editor_buttons_white/"
                BbCode.values().forEach { bbCode ->
                    append("<a style=\"text-decoration: none;\" href=\"${bbCode.code}\">")
                    append("<img style=\"display: inline-block;padding: 0.75rem;width: 1.5rem;height: 1.5rem;\" src=\"${path}${bbCode.fileName}\" />")
                    append("</a> ")
                }
                append("</body></html>")
            }
        }

    private fun handleOnUrlClicked(url: String, textInfo: TextInfo) {
        val code = url.substringAfterLast('/')
        val bbCode = code.toBbCode()
        when {
            bbCode == null -> viewAction = BbCodesAction.SendText(code)
            bbCode.simple -> sendSimpleBbCode(textInfo, bbCode)
            bbCode == BbCode.List -> sendListBbCode(textInfo)
            else -> TODO()
        }
    }

    private fun sendSimpleBbCode(textInfo: TextInfo, bbCode: BbCode) {
        val selectedText = textInfo.selectedText
        val sendText = when (selectedText.length) {
            0 -> {
                val pattern = Pattern.compile("""\[(\/?)${bbCode.code}""", Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(textInfo.text.substring(0, textInfo.selectionStart))
                var unClosed = 0
                while (matcher.find()) {
                    if (matcher.group(1).isNullOrEmpty())
                        unClosed += 1
                    else
                        unClosed = (unClosed - 1).coerceAtLeast(0)
                }
                if (unClosed > 0) {
                    bbCode.closeTag
                } else {
                    bbCode.openTag
                }
            }
            else -> {
                "${bbCode.openTag}$selectedText${bbCode.closeTag}"
            }
        }
        viewAction = BbCodesAction.SendText(sendText)
    }

    private fun sendListBbCode(textInfo: TextInfo) {
        val selectedText = textInfo.selectedText
        when (selectedText.length) {
            0 -> startCollectListItems()
            else -> {
                val sendText = buildString {
                    appendLine()
                    appendLine(BbCode.List.openTag)
                    selectedText
                        .split("\n")
                        .filter { it.isNotEmpty() }
                        .forEach { line ->
                            appendLine("[*] $line")
                        }
                    appendLine(BbCode.List.closeTag)
                }
                viewAction = BbCodesAction.SendText(sendText)
            }
        }
    }

    private fun startCollectListItems() {
        lines.clear()
        requestLine(title = "LIST", hint = "Введите строку ${lines.size + 1}")
    }

    private fun requestLine(title: String, hint: String) {
        viewAction = BbCodesAction.ShowInputTextDialog(title, hint)
    }

    private companion object {
        private val BbCode.fileName: String get() = "${code.lowercase()}.svg"
        private fun String.toBbCode(): BbCode? =
            BbCode.values().find { it.code.equals(this, ignoreCase = true) }

        private val simpleBbCodes: Set<BbCode> = setOf(
            BbCode.Bold,
            BbCode.Italic,
            BbCode.Underline,
            BbCode.Strike,
            BbCode.Sub,
            BbCode.Sup,
            BbCode.Left,
            BbCode.Center,
            BbCode.Right,
            BbCode.Quote,
            BbCode.Offtop,
            BbCode.Code,
            BbCode.Hide,
            BbCode.Curator,
        )
        private val BbCode.simple: Boolean get() = simpleBbCodes.contains(this)
        private val BbCode.closeTag: String get() = "[/${code}]"
        private val BbCode.openTag: String get() = "[${code}]"
    }
}

data class BbCodesState(
    val bbcodesHtml: String? = null
)

sealed class BbCodesAction {
    class SendText(val text: String) : BbCodesAction()
    class ShowInputTextDialog(val title: String, val hint: String) : BbCodesAction()
}

sealed class BbCodesEvent {
    class OnUrlClicked(val url: String, val textInfo: TextInfo) : BbCodesEvent()

    object ActionInvoked : BbCodesEvent()
}