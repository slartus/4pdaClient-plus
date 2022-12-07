package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.models

import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.TextInfo
import ru.softeg.slartus.common.api.models.BbCode

sealed class BbCodesEvent {
    class OnUrlClicked(val url: String, val textInfo: TextInfo) : BbCodesEvent()
    class OnListInput(val bbCode: BbCode, val text: String) : BbCodesEvent()
    class OnListInputFinished(val bbCode: BbCode, val text: String) : BbCodesEvent()
    class OnListInputCanceled(val bbCode: BbCode) : BbCodesEvent()
    object ActionInvoked : BbCodesEvent()
    class OnUrlInput(val bbCode: BbCode, val urlText: String, val url: String) : BbCodesEvent()
    class OnUrlTextInput(val bbCode: BbCode, val urlText: String, val url: String) : BbCodesEvent()
    class OnSpoilerTitle(val bbCode: BbCode, val title: String, val text: String) : BbCodesEvent()
    class OnSizeSelected(val bbCode: BbCode, val item: String, val text: String) : BbCodesEvent()
}