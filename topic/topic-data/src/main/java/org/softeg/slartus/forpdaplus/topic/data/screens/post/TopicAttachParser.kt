package org.softeg.slartus.forpdaplus.topic.data.screens.post

import org.softeg.slartus.forpdacommon.NotReportException
import ru.softeg.slartus.forum.api.PostAttach
import java.util.regex.Pattern
import javax.inject.Inject

class TopicAttachParser @Inject constructor() {
    fun parse(page: String): PostAttach {
        val errorPattern = errorPattern.matcher(page)
        if (errorPattern.find()) {
            throw NotReportException(getStatusMessage(errorPattern.group(1)?.toString().orEmpty()))
        }
        val m = successPattern.matcher(page)
        if (m.find()) {
            val id = m.group(1) ?: throw PostAttachException()
            val name = m.group(2) ?: throw PostAttachException()
            return PostAttach(id, name)
        }
        throw PostAttachException()
    }
    companion object{
        private val errorPattern = Pattern
            .compile(
                "pipsatt.status_msg = [\"']([^']*)['\"];\\s*pipsatt.status_is_error = 1;",
                Pattern.CASE_INSENSITIVE
            )
        private val successPattern = Pattern
            .compile(
                "add_current_item\\(\\s*'(\\d+)',\\s*'([^']*)',\\s*'([^']*)',\\s*'([^']*)'\\s*\\);",
                Pattern.CASE_INSENSITIVE
            )
    }
}

class PostAttachException : Exception()

private fun getStatusMessage(status: String): String {
    when (status) {
        "no_items" -> return "Ни одного файла не загружено"
        "uploading_file" -> return "Загрузка файла..."
        "init_progress" -> return "Инициализация системы..."
        "upload_ok" -> return "Файл успешно загружен и доступен в меню «Управление текущими файлами»"
        "upload_failed" -> return "Неудачная загрузка. Необходимо проверить настройки и права доступа. Пожалуйста, сообщите об этом администрации."
        "upload_too_big" -> return "Неудачная загрузка. Файл имеет размер больше допустимого"
        "invalid_mime_type" -> return "Неудачная загрузка. Вам запрещено загружать такой тип файлов"
        "no_upload_dir" -> return "Неудачная загрузка. Директория загрузок файлов не доступна. Пожалуйста, сообщите об этом администрации."
        "no_upload_dir_perms" -> return "Неудачная загрузка. Невозможно произвести запись файла в директорию загрузок. Пожалуйста, сообщите об этом администрации."
        "upload_no_file" -> return "Вы не выбрали файл для загрузки"
        "upload_banned_file" -> return "Неудачная загрузка. Вам запрещено загружать этот файл"
        "ready" -> return "Система готова для загрузки файлов"
        "attach_remove" -> return "Удалить файл"
        "attach_insert" -> return "Вставить файл в текстовый редактор"
        "remove_warn" -> return "Продолжить удаление файла?"
        "attach_removed" -> return "Файл успешно удален"
        "attach_removal" -> return "Удаление файла..."
        else -> return status
    }
}