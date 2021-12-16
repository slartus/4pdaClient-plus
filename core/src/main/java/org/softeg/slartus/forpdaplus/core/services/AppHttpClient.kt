package org.softeg.slartus.forpdaplus.core.services

interface AppHttpClient {
    suspend fun performGet(url: String): String
}