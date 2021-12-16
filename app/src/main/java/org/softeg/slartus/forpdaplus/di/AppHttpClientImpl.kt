package org.softeg.slartus.forpdaplus.di

import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import ru.slartus.http.Http
import javax.inject.Inject

class AppHttpClientImpl @Inject constructor() : AppHttpClient {
    override suspend fun performGet(url: String): String {
        return Http.instance.performGet(url).responseBody
    }

    override suspend fun performPost(url: String, headers: Map<String, String>) {
        Http.instance.performPost(
            url,
            headers.map { androidx.core.util.Pair(it.key, it.value) }
        )
    }
}