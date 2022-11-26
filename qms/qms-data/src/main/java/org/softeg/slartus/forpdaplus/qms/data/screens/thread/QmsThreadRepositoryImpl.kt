package org.softeg.slartus.forpdaplus.qms.data.screens.thread

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.softeg.slartus.forpdacommon.FilePath
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdacommon.UrlExtensions
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.qms.data.screens.thread.parsers.QmsThreadParser
import org.softeg.slartus.forpdaplus.qms.data.screens.thread.parsers.checkDeleteMessagesResponse
import ru.softeg.slartus.qms.api.models.QmsThreadPage
import ru.softeg.slartus.qms.api.models.UploadFileState
import ru.softeg.slartus.qms.api.models.UploadState
import ru.softeg.slartus.qms.api.repositories.QmsThreadRepository
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class QmsThreadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteQmsThreadDataSource: RemoteQmsThreadDataSource,
    private val parseFactory: ParseFactory,
    private val qmsThreadParser: QmsThreadParser
) : QmsThreadRepository {
    override suspend fun getThread(
        userId: String,
        threadId: String
    ): QmsThreadPage {
        val page = remoteQmsThreadDataSource.getThread(userId, threadId)
        parseFactory.parseAsync(page)
        return qmsThreadParser.parse(page)
    }

    override suspend fun sendMessage(
        userId: String,
        threadId: String,
        message: String,
        attachIds: List<String>
    ) {
        val page = remoteQmsThreadDataSource.sendMessage(userId, threadId, message, attachIds)
        parseFactory.parseAsync(page)
    }

    override suspend fun deleteMessages(userId: String, threadId: String, postIds: List<String>) {
        val page = remoteQmsThreadDataSource.deleteMessages(userId, threadId, postIds)
        parseFactory.parseAsync(page)
        checkDeleteMessagesResponse(page)
    }

    override suspend fun deleteAttach(attachId: String) {
        remoteQmsThreadDataSource.deleteAttach(attachId)
    }

    override suspend fun uploadAttachesFlow(
        uris: List<Uri>
    ): Flow<UploadState> = channelFlow {
        send(UploadState.Init)
        runCatching {
            uris.forEachIndexed { index, uri ->
                delay(1000)
                runCatching {
                    uploadPostAttach(
                        uri = uri,
                        uriIndex = index,
                        context = context,
                        remoteQmsThreadDataSource = remoteQmsThreadDataSource
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
    uri: Uri,
    uriIndex: Int,
    context: Context,
    remoteQmsThreadDataSource: RemoteQmsThreadDataSource
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
        remoteQmsThreadDataSource.attachFile(
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
    }
    send(UploadState.AttachUploaded(uri = uri, postAttach = editAttach))
}


private suspend fun getTempFilePath(context: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val filePath = FilePath.getPath(context, uri)
        if (filePath != null && File(filePath).canRead()) filePath
        else copyFileToTemp(context, uri)
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