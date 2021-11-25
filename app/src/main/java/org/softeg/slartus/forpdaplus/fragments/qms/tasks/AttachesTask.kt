package org.softeg.slartus.forpdaplus.fragments.qms.tasks

import android.annotation.TargetApi
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.util.Pair
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.qms.QmsApi
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.FilePath
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.util.*

class AttachesTask internal constructor(qmsChatFragment: QmsChatFragment,
                                        private val attachFilePaths: List<Uri>) : AsyncTask<String, Pair<String, Long>, Boolean>() {
    private val dialog: MaterialDialog = MaterialDialog.Builder(qmsChatFragment.context!!)
            .progress(true, 0)
            .content(R.string.sending_file)
            .build()
    private val qmsChatFragment = WeakReference(qmsChatFragment)
    private var progressState: ProgressState = object : ProgressState() {
        override fun update(message: String, percents: Long) {
            publishProgress(Pair("", percents))
        }

    }

    private val attaches = ArrayList<EditAttach>()

    private var ex: Throwable? = null

    constructor(qmsChatFragment: QmsChatFragment, newAttachFilePath: Uri) : this(qmsChatFragment, arrayListOf<Uri>(newAttachFilePath))

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

    override fun doInBackground(vararg params: String): Boolean? {
        return try {
            for (newAttachFilePath in attachFilePaths) {
                val tempFilePath =
                    FilePath.getPath(App.getContext(), newAttachFilePath) ?: copyFileToTemp(
                        newAttachFilePath
                    )

                val editAttach = QmsApi.attachFile(tempFilePath, progressState)
                attaches.add(editAttach)
            }
            true
        } catch (e: Throwable) {
            ex = e
            false
        }

    }

    //        @Override
    //        protected void onProgressUpdate(Pair<String, Integer>... values) {
    //            super.onProgressUpdate(values);
    //            if (!TextUtils.isEmpty(values[0].first))
    //                dialog.setContent(values[0].first);
    //            dialog.setProgress(values[0].second);
    //        }

    // can use UI thread here
    override fun onPreExecute() {
        this.dialog.setCancelable(true)
        this.dialog.setCanceledOnTouchOutside(false)
        //            this.dialog.setOnCancelListener(dialogInterface -> {
        //                if (progressState != null)
        //                    progressState.cancel();
        //                cancel(false);
        //            });
        //            this.dialog.setProgress(0);
        this.dialog.isIndeterminateProgress

        this.dialog.show()
    }

    // can use UI thread here
    override fun onPostExecute(success: Boolean?) {
        if (this.dialog.isShowing) {
            this.dialog.dismiss()
        }

        if (success!! || isCancelled && attaches.size > 0) {
            qmsChatFragment.get()?.addAttachesToList(attaches)
        } else {

            if (ex != null) {
                ex!!.printStackTrace()
                Log.e("TEST", "Error " + ex!!.message)
                AppLog.e(qmsChatFragment.get()?.context?: App.getInstance(), ex)
            } else
                Toast.makeText(qmsChatFragment.get()?.context?: App.getInstance(), R.string.unknown_error, Toast.LENGTH_SHORT).show()

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCancelled(success: Boolean?) {
        super.onCancelled(success)
        if (success!! || isCancelled && attaches.size > 0) {
            qmsChatFragment.get()?.addAttachesToList(attaches)
        } else {
            if (ex != null)
                AppLog.e(qmsChatFragment.get()?.context?: App.getInstance(), ex)
            else
                Toast.makeText(qmsChatFragment.get()?.context?: App.getInstance(), R.string.unknown_error, Toast.LENGTH_SHORT).show()

        }
    }

}