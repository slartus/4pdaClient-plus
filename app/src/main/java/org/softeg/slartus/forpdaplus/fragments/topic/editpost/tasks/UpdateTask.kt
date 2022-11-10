package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.FilePath
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun uploadFilesFlow(
    postId: String,
    uris: List<Uri>
): Flow<UploadState> = channelFlow {
    send(UploadState.Init())
    runCatching {
        uris.forEach { uri ->
            if (!isActive) cancel()

            send(UploadState.Uploading(uris = uris, currentUploadFile = UploadFileState(uri, 0)))
            val tempFilePath = getTempFilePath(uri = uri)
            val editAttach = withContext(Dispatchers.IO) {
                val scope = this
                PostApi.attachFile(
                    Client.getInstance(),
                    postId, tempFilePath, object : ProgressState() {
                        override fun update(message: String, percents: Long) {
                            if (!isActive) cancel()
                            scope.launch {
                                send(
                                    UploadState.Uploading(
                                        uris = uris,
                                        currentUploadFile = UploadFileState(uri, percents)
                                    )
                                )
                            }
                        }
                    }
                )
            }
            send(UploadState.Uploaded(editAttach))
        }
    }.onFailure {
        if (it !is CancellationException)
            send(UploadState.Error(it))
    }.onSuccess {

        send(UploadState.Completed)
    }
}

private suspend fun getTempFilePath(uri: Uri): String {
    return withContext(Dispatchers.IO) {
        FilePath.getPath(App.getContext(), uri) ?: copyFileToTemp(uri)
    }
}

private suspend fun copyFileToTemp(uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val context = App.getContext()
        val fileName = FilePath.getFileName(context, uri)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()
        context.contentResolver.openInputStream(uri)?.buffered()?.use { inputStream ->
            FileOutputStream(tempFile, false).use { outputStream ->
                FileUtils.CopyStream(inputStream, outputStream)
            }
        }
        tempFile.absolutePath
    }
}

sealed class UploadState {
    class Init : UploadState() {
        val message = App.getContext().getString(R.string.sending_file)
    }

    data class Uploading(
        val uris: List<Uri>,
        val currentUploadFile: UploadFileState
    ) : UploadState() {
        val message = String.format(
            App.getContext().getString(R.string.format_sending_file),
            uris.indexOf(currentUploadFile.uri) + 1,
            uris.size
        )
        val percents = currentUploadFile.percents
    }

    class Uploaded(val editAttach: EditAttach?) : UploadState()
    object Completed : UploadState()
    class Error(val error: Throwable) : UploadState()
}


class UploadFileState(val uri: Uri, val percents: Long)