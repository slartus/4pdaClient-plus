package ru.slartus.http

/*
 * Created by slinkin on 01.07.13.
 */
object AppHttpStatus {
    fun getReasonPhrase(code: Int, defaultPhrase: String): String {
        when (code) {
            500 -> return "Внутренняя ошибка сервера"
            501 -> return "Не реализовано"
            502 -> return "Плохой шлюз"
            503 -> return "Сервис недоступен"
            504 -> return "Шлюз не отвечает"
            505 -> return "Версия HTTP не поддерживается"
            506 -> return "Вариант тоже согласован"
            507 -> return "Переполнение хранилища"
            509 -> return "Исчерпана пропускная ширина канала"
            510 -> return "Не расширено"
        }
        return defaultPhrase
    }
}
