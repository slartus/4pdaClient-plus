package org.softeg.slartus.forpdaplus.topic.data.screens.users

import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.hosthelper.HostHelper.Companion.host
import javax.inject.Inject

class RemoteTopicUsersDataSource @Inject constructor(private val httpClient: AppHttpClient) {
    suspend fun loadTopicReaders(topicId: String): String =
        httpClient.performGetDesktopVersion("https://$host/forum/index.php?showtopic=$topicId")

}