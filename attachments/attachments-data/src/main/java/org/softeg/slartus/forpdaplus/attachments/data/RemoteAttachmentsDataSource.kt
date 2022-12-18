package org.softeg.slartus.forpdaplus.attachments.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.notReportError
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.hosthelper.HostHelper.Companion.host
import ru.slartus.http.FileUtils
import ru.softeg.slartus.attachments.api.models.Attachment
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject

class RemoteAttachmentsDataSource @Inject constructor(private val httpClient: AppHttpClient) {
    suspend fun attachQmsFile(
        fileName: String,
        filePath: String,
        onProgressChange: (percents: Int) -> Unit
    ): Attachment {
        val file = File(filePath)
        val totalSize = file.length()
        val params = buildList {
            add("name" to fileName)
            add("relType" to "MSG")
            add("index" to "1")
            add("size" to totalSize.toString())
            add("md5" to FileUtils.calculateMD5(file))
        }

        val checkPage = httpClient.performPost(
            url = "https://$host/forum/index.php?act=attach",
            (params + Pair("code", "check")).toMap()
        )

        val attachment = parseAttachResponse(checkPage)
        if (attachment != null) return attachment

        val page = httpClient.uploadFile(
            url = "https://$host/forum/index.php?act=attach",
            fileName = fileName,
            filePath = filePath,
            formDataParts = params + Pair("code", "upload"),
            onProgressChange = onProgressChange
        )
        return parseAttachResponse(page) ?: notReportError("Unknown upload error")
    }

    suspend fun attachTopicFile(
        topicId: String,
        attachedFileIds: List<String>,
        fileName: String,
        filePath: String,
        onProgressChange: (percents: Int) -> Unit
    ): Attachment {
        val file = File(filePath)
        val totalSize = file.length()
        val params = buildList {
            add("name" to fileName)
            add("index" to "1")
            add("relId" to "0")
            add("size" to totalSize.toString())
            add("md5" to FileUtils.calculateMD5(file))
            add("topic_id" to topicId)
            add("forum-attach-files" to attachedFileIds.joinToString(separator = ","))
        }

        val checkPage = httpClient.performPost(
            url = "https://$host/forum/index.php?act=attach",
            (params + Pair("code", "check")).toMap()
        )

        val attachment = parseAttachResponse(checkPage)
        if (attachment != null) return attachment

        val page = httpClient.uploadFile(
            url = "https://$host/forum/index.php?act=attach",
            fileName = fileName,
            filePath = filePath,
            formDataParts = params + Pair("code", "upload"),
            onProgressChange = onProgressChange
        )
        return parseAttachResponse(page) ?: notReportError("Unknown upload error")
    }

    suspend fun attachPostFile(
        postId: String,
        fileName: String,
        filePath: String,
        onProgressChange: (percents: Int) -> Unit
    ): Attachment {
        val file = File(filePath)
        val totalSize = file.length()
        val params = buildList {
            add("name" to fileName)
            add("size" to totalSize.toString())
            add("md5" to FileUtils.calculateMD5(file))
        }

        val page = httpClient.uploadFile(
            url = "https://$host/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=$postId",
            filePath = filePath,
            fileName = fileName,
            onProgressChange = onProgressChange,
            formDataParts = params
        )
        return parsePostAttachResponse(page)
    }

    suspend fun deleteAttach(attachId: String) {
        val params = buildMap {
            put("code", "remove")
            put("relType", "MSG")
            put("relId", "0")
            put("index", "1")
            put("id", attachId)
        }
        httpClient.performPost("https://$host/forum/index.php?act=attach", params)
    }

    private suspend fun parseAttachResponse(page: String): Attachment? =
        withContext(Dispatchers.Default) {
            val body = page.replace("(^\\x03|\\x03$)".toRegex(), "")
            val parts = body.split("\u0002")
            val code = parts.first().toIntOrNull() ?: 0
            when {
                code == 0 -> return@withContext null
                code == -1 -> throw NotReportException(
                    "Нет доступа"
                )
                code == -2 -> throw NotReportException(
                    "Слишком большой размер"
                )
                code == -3 -> throw NotReportException(
                    "Неподдерживаемый тип файла"
                )
                code == -4 -> throw NotReportException(
                    "Запрещен на сервере"
                )
                code < 0 -> throw NotReportException(
                    "Ошибка загрузки: $code"
                )
            }
            val name = parts.getOrNull(1) ?: throw NotReportException(
                "Name of upload not found"
            )
            val ext = parts.getOrNull(2) ?: throw NotReportException(
                "Ext of upload not found"
            )
            return@withContext  Attachment(code.toString(), "$name.$ext")
        }

    private fun parsePostAttachResponse(page: String): Attachment {
        val errorPattern = Pattern
            .compile(
                "pipsatt.status_msg = '([^']*)';\\s*pipsatt.status_is_error = parseInt\\('(\\d+)'\\);",
                Pattern.CASE_INSENSITIVE
            )
            .matcher(page)
        if (errorPattern.find()) {
            if ("1" == errorPattern.group(2))
                throw NotReportException(
                    getStatusMessage(errorPattern.group(1)?: "Unknown error")
                )
        }
        val m = Pattern
            .compile(
                "add_current_item\\(\\s*'(\\d+)',\\s*'([^']*)',\\s*'([^']*)',\\s*'([^']*)'\\s*\\);",
                Pattern.CASE_INSENSITIVE
            )
            .matcher(page)
        if (m.find()) {
            val id = m.group(1) ?: error("Attachment `id` not found in response")
            val name = m.group(2) ?: error("Attachment `name` not found in response")
            return Attachment(id, name)
        }
        error("attachment add_current_item pattern is not matched")
    }

    private fun getStatusMessage(status: String?): String? {
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
}