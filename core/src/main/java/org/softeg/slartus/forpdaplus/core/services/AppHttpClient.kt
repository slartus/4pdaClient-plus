package org.softeg.slartus.forpdaplus.core.services

import kotlin.Pair

interface AppHttpClient {
    suspend fun performGet(url: String): String
    suspend fun performGetDesktopVersion(url: String): String
    suspend fun performPost(url: String, headers: Map<String, String>): String
    suspend fun uploadFile(
        url: String,
        filePath: String,
        fileName: String,
        formDataParts: List<Pair<String, String>> = emptyList(),
        onProgressChange: (percents: Int) -> Unit = {}
    ): String
}