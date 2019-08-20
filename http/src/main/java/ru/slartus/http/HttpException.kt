package ru.slartus.http

import okhttp3.*
import java.io.File

class HttpException : Exception {
    constructor(message: String) : super(message)

    constructor(ex: Throwable) : super(ex)
}
