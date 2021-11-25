package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Pair
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.FilePath
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragmentListener
import java.io.File
import java.io.FileOutputStream

class UpdateTask internal constructor(
    listener: EditPostFragmentListener,
    private val postId: String,
    private val attachFilePaths: List<Uri>
) : BaseTask<String, Pair<String, Long>>(listener, R.string.sending_file) {

    internal constructor(
        listener: EditPostFragmentListener,
        postId: String,
        newAttachFilePath: Uri
    ) : this(listener, postId, arrayListOf<Uri>(newAttachFilePath))

    override fun createProgressDialolg(
        context: Context,
        progressMessageResId: Int
    ): MaterialDialog = MaterialDialog.Builder(context)
        .progress(false, 100, false)
        .content(R.string.sending_file)
        .show()

    private fun copyFileToTemp(uri: Uri): String {
        val context = App.getContext()
        val fileName = FilePath.getFileName(context, uri)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()
        context.contentResolver.openInputStream(uri)?.buffered()?.use { inputStream ->
            FileOutputStream(tempFile, false).use { outputStream ->
                FileUtils.CopyStream(inputStream, outputStream)
            }
        }
        return tempFile.absolutePath
    }

    override fun work(params: Array<out String>) {
        progressState = object : ProgressState() {
            override fun update(message: String, percents: Long) {
                publishProgress(Pair("", percents))
            }
        }

        var i = 1
        for (newAttachFilePath in attachFilePaths) {
            val tempFilePath =
                FilePath.getPath(App.getContext(), newAttachFilePath) ?: copyFileToTemp(
                    newAttachFilePath
                )
            publishProgress(
                Pair(
                    String.format(
                        App.getContext().getString(R.string.format_sending_file),
                        i++,
                        attachFilePaths.size
                    ), 0
                )
            )
            editAttach = PostApi.attachFile(
                Client.getInstance(),
                postId, tempFilePath, progressState!!
            )
        }
    }

    override fun onSuccess() {
        editPostFragmentListener.get()?.onUpdateTaskSuccess(editAttach)
    }

    private var progressState: ProgressState? = null

    private var editAttach: EditAttach? = null

    override fun onProgressUpdate(vararg values: Pair<String, Long>) {
        super.onProgressUpdate(*values)
        if (!TextUtils.isEmpty(values[0].first))
            dialog?.setContent(values[0].first)
        dialog?.setProgress(values[0].second.toInt())
    }

    // can use UI thread here
    override fun onPreExecute() {
        this.dialog?.apply {
            setCancelable(true)
            setCanceledOnTouchOutside(false)
            setOnCancelListener {
                if (progressState != null)
                    progressState!!.cancel()
                cancel(false)
            }
            setProgress(0)
            show()
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCancelled(success: Boolean) {
        super.onCancelled(success)
        if (success || isCancelled && editAttach != null) {
            onSuccess()
        } else {
            showError()
        }
    }

}