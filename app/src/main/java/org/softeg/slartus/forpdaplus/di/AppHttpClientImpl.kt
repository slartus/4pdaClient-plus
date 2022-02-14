package org.softeg.slartus.forpdaplus.di

import androidx.core.util.Pair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import ru.slartus.http.Http
import javax.inject.Inject

class AppHttpClientImpl @Inject constructor() : AppHttpClient {
    override suspend fun performGet(url: String): String = withContext(Dispatchers.IO) {
        Http.instance.performGet(url).responseBody
    }

    override suspend fun performPost(url: String, headers: Map<String, String>): String = withContext(Dispatchers.IO){
        Http.instance.performPost(
            url,
            headers.map { Pair(it.key, it.value) }
        ).responseBody
    }
}