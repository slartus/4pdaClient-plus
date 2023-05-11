package ru.softeg.slartus.common.api.exceptions

class CheckHumanityException : Exception()

inline fun checkHumanityError(): Nothing = throw CheckHumanityException()