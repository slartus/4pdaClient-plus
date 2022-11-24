package org.softeg.slartus.forpdaplus.topic.data.screens.post

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdacommon.FilePath
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdacommon.UrlExtensions
import ru.softeg.slartus.forum.api.TopicPostRepository
import ru.softeg.slartus.forum.api.UploadFileState
import ru.softeg.slartus.forum.api.UploadState
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class TopicPostRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteTopicPostDataSource: RemoteTopicPostDataSource,
    private val topicAttachParser: TopicAttachParser
) : TopicPostRepository {
    override suspend fun uploadPostAttachesFlow(
        postId: String,
        uris: List<Uri>
    ): Flow<UploadState> = channelFlow {
        send(UploadState.Init)
        runCatching {
            uris.forEachIndexed { index, uri ->
                runCatching {
                    uploadPostAttach(
                        postId = postId,
                        uri = uri,
                        uriIndex = index,
                        context = context,
                        remoteTopicPostDataSource = remoteTopicPostDataSource,
                        topicAttachParser = topicAttachParser
                    )
                }.onFailure {
                    if (it !is CancellationException)
                        send(UploadState.AttachError(uri, it))
                }
            }
        }.onFailure {
            if (it !is CancellationException)
                send(UploadState.Error(it))
        }.onSuccess {
            send(UploadState.Completed)
        }
    }
}

private suspend fun ProducerScope<UploadState>.uploadPostAttach(
    postId: String,
    uri: Uri,
    uriIndex: Int,
    context: Context,
    remoteTopicPostDataSource: RemoteTopicPostDataSource,
    topicAttachParser: TopicAttachParser
) {
    send(
        UploadState.Uploading(
            index = uriIndex,
            currentUploadFile = UploadFileState(uri, 0)
        )
    )
    val filePath = getTempFilePath(uri = uri, context = context)
    val fileName = runCatching { UrlExtensions.getFileNameFromUrl(filePath) }
        .getOrNull() ?: filePath.substringAfterLast("/")

    val editAttach = withContext(Dispatchers.IO) {
        val scope = this
        val attachedPage = remoteTopicPostDataSource.uploadPostAttach(
            postId = postId,
            fileName = fileName,
            filePath = filePath,
            onProgressChange = { percents ->
                scope.launch {
                    send(
                        UploadState.Uploading(
                            index = uriIndex,
                            currentUploadFile = UploadFileState(uri, percents = percents)
                        )
                    )
                }
            }
        )
        topicAttachParser.parse(attachedPage)
    }
    send(UploadState.AttachUploaded(uri = uri, postAttach = editAttach))
}


private suspend fun getTempFilePath(context: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        FilePath.getPath(context, uri) ?: copyFileToTemp(context, uri)
    }
}

private suspend fun copyFileToTemp(context: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val fileName = FilePath.getFileName(context, uri)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.buffered().use { inputStream ->
                FileOutputStream(tempFile, false).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        tempFile.absolutePath
    }
}