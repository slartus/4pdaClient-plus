package org.softeg.slartus.forpdaplus.di

import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import ru.slartus.http.Http
import javax.inject.Inject

class AppHttpClientImpl @Inject constructor() : AppHttpClient {
    override suspend fun performGet(url: String): String {
        return Http.instance.performGet(url).responseBody
    }
}