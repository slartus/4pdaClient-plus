package ru.slartus.http

import okhttp3.*
import java.io.File


fun File.asRequestBody(contentType: MediaType? = null): RequestBody = RequestBody.create(contentType, this)

fun String.toMediaTypeOrNull(): MediaType? {
    return try {
        toMediaType()
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun String.toMediaType(): MediaType = MediaType.get(this)

val Response.body: ResponseBody?
    get() = this.body()

val Response.request: Request
    get() = this.request()

val Response.headers: Headers
    get() = this.headers()

val Request.url: HttpUrl
    get() = this.url()

val Request.body: RequestBody?
    get() = this.body()

val Request.headers: Headers
    get() = this.headers()