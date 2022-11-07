package org.softeg.slartus.forpdaplus.qms.data.screens.thread.parsers

import org.jsoup.Jsoup
import org.softeg.slartus.forpdacommon.NotReportException

fun checkSendMessageResponse(page:String){
    val document = Jsoup.parse(page)
    document.selectFirst("div.error")?.let { element ->
        throw NotReportException(element.text())
    }
    document.selectFirst("div.form-error")?.let { element ->
        throw NotReportException(element.text())
    }
}

fun checkDeleteMessagesResponse(page:String){
    val document = Jsoup.parse(page)
    document.selectFirst("div.error")?.let { element ->
        throw NotReportException(element.text())
    }
    document.selectFirst("div.form-error")?.let { element ->
        throw NotReportException(element.text())
    }
}