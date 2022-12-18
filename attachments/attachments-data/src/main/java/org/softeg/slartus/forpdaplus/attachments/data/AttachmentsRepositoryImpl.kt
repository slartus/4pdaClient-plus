package org.softeg.slartus.forpdaplus.attachments.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.softeg.slartus.forpdacommon.FilePath
import org.softeg.slartus.forpdacommon.UrlExtensions
import ru.softeg.slartus.attachments.api.models.UploadFileState
import ru.softeg.slartus.attachments.api.models.UploadState
import ru.softeg.slartus.attachments.api.AttachmentsRepository
import ru.softeg.slartus.attachments.api.models.Attachment
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class AttachmentsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteAttachmentsDataSource: RemoteAttachmentsDataSource
) : AttachmentsRepository {
    override suspend fun uploadQmsAttachesFlow(uris: List<Uri>): Flow<UploadState> =
        uploadFlow(uris, remoteAttachmentsDataSource::attachQmsFile)

    override suspend fun uploadTopicAttachesFlow(
        topicId: String,
        attachedFileIds: List<String>,
        uris: List<Uri>
    ): Flow<UploadState> =
        uploadFlow(uris, uploadMethod = { fileName, filePath, onProgressChange ->
            remoteAttachmentsDataSource.attachTopicFile(
                topicId,
                attachedFileIds,
                fileName,
                filePath,
                onProgressChange
            )
        })

    override suspend fun uploadPostAttachesFlow(
        postId: String,
        uris: List<Uri>
    ): Flow<UploadState> =
        uploadFlow(uris, uploadMethod = { fileName, filePath, onProgressChange ->
            remoteAttachmentsDataSource.attachPostFile(
                postId,
                fileName,
                filePath,
                onProgressChange
            )
        })

    private suspend fun uploadFlow(
        uris: List<Uri>,
        uploadMethod: suspend (fileName: String, filePath: String, onProgressChange: (percents: Int) -> Unit) -> Attachment
    ): Flow<UploadState> =
        channelFlow {
            send(UploadState.Init)
            runCatching {
                uris.forEachIndexed { index, uri ->
                    uploadPostAttach(
                        uri = uri,
                        uriIndex = index,
                        uploadMethod = uploadMethod
                    )
                    delay(1000)
                }
            }.onFailure {
                if (it !is CancellationException)
                    send(UploadState.Error(it))
            }.onSuccess {
                send(UploadState.Completed)
            }
        }

    private suspend fun ProducerScope<UploadState>.uploadPostAttach(
        uri: Uri,
        uriIndex: Int,
        uploadMethod: suspend (fileName: String, filePath: String, onProgressChange: (percents: Int) -> Unit) -> Attachment
    ) {
        runCatching {
            send(
                UploadState.Uploading(
                    index = uriIndex,
                    currentUploadFile = UploadFileState(uri, 0)
                )
            )
            val filePath = getTempFilePath(uri = uri, context = context)
            val fileName = runCatching { UrlExtensions.getFileNameFromUrl(filePath) }
                .getOrNull() ?: filePath.substringAfterLast("/")


            val editAttach = uploadMethod(fileName, filePath) { percents ->
                launch {
                    send(
                        UploadState.Uploading(
                            index = uriIndex,
                            currentUploadFile = UploadFileState(uri, percents = percents)
                        )
                    )
                }
            }

            send(UploadState.AttachUploaded(uri = uri, postAttach = editAttach))
        }.onFailure {
            if (it !is CancellationException)
                send(UploadState.AttachError(uri, it))
        }
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

    override suspend fun deleteAttach(attachId: String) {
        remoteAttachmentsDataSource.deleteAttach(attachId)
    }
}

