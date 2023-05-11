package ru.slartus.http

import ru.softeg.slartus.common.api.exceptions.checkHumanityError

data class AppResponse(val requestUrl: String?,
                       val redirectUrl: String?,
                       var responseBody: String) {
    init {
        if(responseBody.contains("<p id=\"cf-spinner-please-wait\">Please stand by, while we are checking your browser...</p>"))
            checkHumanityError()
    }
    fun redirectUrlElseRequestUrl() = if (redirectUrl.isNullOrEmpty()) requestUrl else redirectUrl
}