package ru.slartus.http

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File


fun File.asRequestBody(contentType: MediaType? = null): RequestBody = RequestBody.create(contentType, this)

fun String.toMediaTypeOrNull(): MediaType? {
    return try {
        toMediaType()
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun String.toMediaType(): MediaType = this.toMediaType()

var Response.body: ResponseBody?
    get() = this.body
    set(value) {}

var Response.request: Request
    get() = this.request
    set(value) {}

var Response.headers: Headers
    get() = this.headers
    set(value) {}

var Request.url: HttpUrl
    get() = this.url
    set(value) {}

var Request.body: RequestBody?
    get() = this.body
    set(value) {}

var Request.headers: Headers
    get() = this.headers
    set(value) {}