package ru.slartus.http

data class AppResponse(val requestUrl: String?,
                       val redirectUrl: String?,
                       val responseBody: String?) {
    fun redirectUrlElseRequestUrl() = if (redirectUrl.isNullOrEmpty()) requestUrl else redirectUrl
}