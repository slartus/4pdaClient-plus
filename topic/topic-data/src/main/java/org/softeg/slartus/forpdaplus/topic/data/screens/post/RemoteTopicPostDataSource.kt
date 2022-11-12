package org.softeg.slartus.forpdaplus.topic.data.screens.post

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

class RemoteTopicPostDataSource @Inject constructor(
    private val httpClient: AppHttpClient
) {
    suspend fun uploadPostAttach(
        postId: String,
        fileName: String,
        filePath: String,
        onProgressChange: (percents: Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        return@withContext httpClient.uploadFile(
            url = "https://${HostHelper.host}/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=$postId",
            filePath = filePath,
            fileName = fileName,
            onProgressChange = onProgressChange
        )
    }
}