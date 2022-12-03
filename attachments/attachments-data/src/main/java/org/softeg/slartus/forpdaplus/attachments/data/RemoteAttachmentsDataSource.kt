package org.softeg.slartus.forpdaplus.attachments.data

import org.softeg.slartus.forpdacommon.NotReportException
import ru.softeg.slartus.attachments.api.models.Attachment
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.FileUtils
import java.io.File
import javax.inject.Inject

class RemoteAttachmentsDataSource @Inject constructor(private val httpClient: AppHttpClient) {

    suspend fun attachFile(
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
            url = "https://${HostHelper.host}/forum/index.php?act=attach",
            (params + Pair("code", "check")).toMap()
        )

        val attachment = parseAttachResponse(checkPage)
        if (attachment != null) return attachment

        val page = httpClient.uploadFile(
            url = "https://${HostHelper.host}/forum/index.php?act=attach",
            fileName = fileName,
            filePath = filePath,
            formDataParts = params + Pair("code", "upload"),
            onProgressChange = onProgressChange
        )
        return parseAttachResponse(page) ?: throw NotReportException(
            "Unknown upload error"
        )
    }

    suspend fun deleteAttach(attachId: String) {
        val params = buildMap {
            put("code", "remove")
            put("relType", "MSG")
            put("relId", "0")
            put("index", "1")
            put("id", attachId)
        }
        httpClient.performPost("https://${HostHelper.host}/forum/index.php?act=attach", params)
    }

    private fun parseAttachResponse(page: String): Attachment? {
        val body = page.replace("(^\\x03|\\x03$)".toRegex(), "")
        val parts = body.split("\u0002")
        val code = parts.first().toIntOrNull() ?: 0
        when {
            code == 0 -> return null
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
        return Attachment(code.toString(), "$name.$ext")
    }
}