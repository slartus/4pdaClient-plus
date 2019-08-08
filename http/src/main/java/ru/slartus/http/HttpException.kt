package ru.slartus.http

class HttpException : Exception {
    constructor(message: String) : super(message)

    constructor(ex: Throwable) : super(ex)
}
