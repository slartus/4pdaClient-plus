package org.softeg.slartus.forpdaplus.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import ru.slartus.http.Http
import javax.inject.Inject

class AppHttpClientImpl @Inject constructor() : AppHttpClient {
    override suspend fun performGet(url: String): String = withContext(Dispatchers.IO) {
        return@withContext Http.instance.performGet(url).responseBody
    }

    override suspend fun performGetDesktopVersion(url: String): String =
        withContext(Dispatchers.IO) {
            return@withContext Http.instance.performGetFull(url).responseBody
        }

    override suspend fun performPost(url: String, headers: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            return@withContext Http.instance.performPost(
                url,
                headers.map { Pair(it.key, it.value) }
            ).responseBody
        }

    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fileName: String,
        onProgressChange: (percents: Int) -> Unit
    ): String =
        withContext(Dispatchers.IO)
        {
            return@withContext Http.instance.uploadFile(
                url = url,
                filePath = filePath,
                fileNameO = fileName,
                formDataParts = emptyList(),
                progressListener = {
                    onProgressChange(it.toInt())
                }
            ).responseBody
        }
}