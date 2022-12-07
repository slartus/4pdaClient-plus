package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.models.BbCodesAction
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.models.BbCodesEvent
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.models.BbCodesState

import org.softeg.slartus.forpdaplus.core_lib.viewmodel.BaseViewModel
import ru.softeg.slartus.common.api.AppStyleType
import ru.softeg.slartus.common.api.AppTheme
import ru.softeg.slartus.common.api.htmlBackgroundColor
import ru.softeg.slartus.common.api.models.BbCode
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BbCodesViewModel @Inject constructor(
    private val appTheme: AppTheme
) :
    BaseViewModel<BbCodesState, BbCodesAction, BbCodesEvent>(BbCodesState()) {
    private val lines = mutableListOf<String>()

    init {
        fetchData()
    }

    override fun obtainEvent(viewEvent: BbCodesEvent) = when (viewEvent) {
        BbCodesEvent.ActionInvoked -> viewAction = null
        is BbCodesEvent.OnUrlClicked -> handleOnUrlClicked(viewEvent.url, viewEvent.textInfo)
        is BbCodesEvent.OnListInput -> handleOnListInput(viewEvent.bbCode, viewEvent.text)
        is BbCodesEvent.OnListInputFinished -> handleOnListInputFinished(
            viewEvent.bbCode,
            viewEvent.text
        )
        is BbCodesEvent.OnListInputCanceled -> handleOnListInputCanceled()
        is BbCodesEvent.OnUrlInput -> handleOnUrlInput(
            bbCode = viewEvent.bbCode,
            text = viewEvent.urlText,
            url = viewEvent.url
        )
        is BbCodesEvent.OnUrlTextInput -> handleOnUrlTextInput(
            bbCode = viewEvent.bbCode,
            text = viewEvent.urlText,
            url = viewEvent.url
        )
        is BbCodesEvent.OnSpoilerTitle -> handleOnSpoilerTitle(
            viewEvent.bbCode,
            viewEvent.title,
            viewEvent.text
        )
    }

    private fun handleOnSpoilerTitle(bbCode: BbCode, title: String, text: String) {
        val tagPostfix = if (title.isEmpty()) "" else "=$title"
        val sendText = buildString {
            append(bbCode.openTag(tagPostfix))
            if (text.isNotEmpty()) {
                append(text)
                append(bbCode.closeTag)
            }
        }
        viewAction = BbCodesAction.SendText(sendText)
    }

    private fun fetchData() {
        viewModelScope.launch {
            viewState = viewState.copy(bbcodesHtml = createHtmlPage())
        }
    }

    private suspend fun createHtmlPage(): String =
        withContext(Dispatchers.Default) {
            val style = appTheme.getStyle()
            buildString {
                append("<html><body bgcolor=\"${style.htmlBackgroundColor}\"")
                val path = when (style.type) {
                    AppStyleType.Light -> "file:///android_asset/forum/style_images/1/folder_editor_buttons_white/"
                    AppStyleType.Dark, AppStyleType.Black -> "file:///android_asset/forum/style_images/1/folder_editor_buttons_black/"
                }
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
            bbCode.simple -> handleSimpleBbCode(bbCode, textInfo)
            bbCode in setOf(BbCode.List, BbCode.NumList) -> handleListBbCode(bbCode, textInfo)
            bbCode == BbCode.Url -> handleUrlBbCode(bbCode, textInfo)
            bbCode == BbCode.Spoiler -> handleSpoilerBbCode(bbCode, textInfo)
            else -> TODO()
        }
    }

    private fun handleSpoilerBbCode(bbCode: BbCode, textInfo: TextInfo) {
        viewAction = if (canCloseBbCode(bbCode, textInfo)) {
            BbCodesAction.SendText(bbCode.closeTag)
        } else {
            BbCodesAction.ShowSpoilerInputDialog(bbCode, textInfo.selectedText)
        }
    }

    private fun handleUrlBbCode(bbCode: BbCode, textInfo: TextInfo) {
        viewAction = BbCodesAction.ShowUrlInputDialog(bbCode, textInfo.selectedText)
    }

    private fun handleOnUrlInput(bbCode: BbCode, text: String, url: String) {
        if (text.isEmpty()) {
            viewAction = BbCodesAction.ShowUrlTextInputDialog(bbCode = bbCode, url = url)
        } else {
            sendUrl(bbCode, text, url)
        }
    }

    private fun handleOnUrlTextInput(bbCode: BbCode, text: String, url: String) {
        sendUrl(bbCode = bbCode, urlText = text, url = url)
    }

    private fun sendUrl(bbCode: BbCode, urlText: String, url: String) {
        val sendText = "${bbCode.openTag("=$url")}$urlText${bbCode.closeTag}"
        viewAction = BbCodesAction.SendText(sendText)
    }

    private fun handleSimpleBbCode(bbCode: BbCode, textInfo: TextInfo) {
        val selectedText = textInfo.selectedText.trim()
        val sendText = when (selectedText.length) {
            0 -> {
                if (canCloseBbCode(bbCode, textInfo)) {
                    bbCode.closeTag
                } else {
                    bbCode.openTag()
                }
            }
            else -> {
                "${bbCode.openTag()}$selectedText${bbCode.closeTag}"
            }
        }
        viewAction = BbCodesAction.SendText(sendText)
    }

    private fun handleListBbCode(bbCode: BbCode, textInfo: TextInfo) {
        val selectedText = textInfo.selectedText.trim()
        when (selectedText.length) {
            0 -> startCollectListItems(bbCode)
            else -> {
                sendList(bbCode, selectedText.split("\n"))
            }
        }
    }

    private fun sendList(bbCode: BbCode, lines: List<String>) {
        val isNumList = bbCode == BbCode.NumList
        val tagPostfix = if (isNumList) "=1" else ""
        val sendText = buildString {
            appendLine()
            appendLine(BbCode.List.openTag(tagPostfix))
            lines
                .filter { it.isNotEmpty() }
                .forEach { line ->
                    appendLine("[*] $line")
                }
            appendLine(BbCode.List.closeTag)
        }
        viewAction = BbCodesAction.SendText(sendText)
    }

    private fun startCollectListItems(bbCode: BbCode) {
        lines.clear()
        requestNextLine(bbCode)
    }

    private fun requestNextLine(bbCode: BbCode) {
        viewAction = BbCodesAction.ShowListInputTextDialog(bbCode, lines.size + 1)
    }

    private fun handleOnListInput(bbCode: BbCode, text: String) {
        lines += text
        requestNextLine(bbCode)
    }

    private fun handleOnListInputFinished(bbCode: BbCode, text: String) {
        lines += text
        sendList(bbCode, lines)
        lines.clear()
    }

    private fun handleOnListInputCanceled() {
        lines.clear()
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
            BbCode.Subscript,
            BbCode.Superscript,
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
        private fun BbCode.openTag(tagPostfix: String = "") = "[$code$tagPostfix]"


        private fun canCloseBbCode(bbCode: BbCode, textInfo: TextInfo): Boolean {
            val selectedText = textInfo.selectedText.trim()
            if (selectedText.isNotEmpty()) return false
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
                return true
            }
            return false
        }


    }
}

