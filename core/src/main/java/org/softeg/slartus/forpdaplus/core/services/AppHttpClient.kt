package org.softeg.slartus.forpdaplus.core.services

interface AppHttpClient {
    suspend fun performGet(url: String): String
    suspend fun performGetDesktopVersion(url: String): String
    suspend fun performPost(url: String, headers: Map<String, String>): String
    suspend fun uploadFile(
        url: String,
        filePath: String,
        fileName: String,
        onProgressChange: (percents: Int) -> Unit
    ): String
}