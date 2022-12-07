package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.models

import ru.softeg.slartus.common.api.models.BbCode

sealed class BbCodesAction {
    class SendText(val text: String) : BbCodesAction()
    class ShowListInputTextDialog(val bbCode: BbCode, val lineNumber: Int) : BbCodesAction()
    class ShowUrlInputDialog(val bbCode: BbCode, val urlText: String) : BbCodesAction()
    class ShowUrlTextInputDialog(val bbCode: BbCode, val url: String) : BbCodesAction()
    class ShowSpoilerInputDialog(val bbCode: BbCode, val selectedText: String) : BbCodesAction()
    class ShowSizeChooseDialog(val bbCode: BbCode, val selectedText: String, val items: List<String>) : BbCodesAction()
}