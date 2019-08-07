package org.softeg.slartus.forpdaplus.listfragments.next.forum

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.ForumsApi
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.classes.ForumsData
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.ForumsTable
import java.lang.ref.WeakReference

/*
 * Created by slinkin on 04.02.2019.
 */
internal class UpdateForumStructTask internal constructor(val context: WeakReference<Context>, val listener: IProgressListener) : AsyncTask<String, String, ForumsData>() {

    private val dialog: MaterialDialog = MaterialDialog.Builder(context.get()!!)
            .progress(true, 0)
            .cancelListener { cancel(true) }
            .content(R.string.refreshing_forum_struct)
            .build()

    override fun onCancelled() {
        Toast.makeText(context.get()
                ?: App.getInstance(), R.string.canceled_refreshing_forum_struct, Toast.LENGTH_SHORT).show()
    }

    override fun doInBackground(vararg forums: String): ForumsData? {

        try {

            if (isCancelled) return null

            val res = ForumsApi.loadForums(Client.getInstance(), object : ProgressState() {
                override fun update(message: String, percents: Long) {
                    publishProgress(String.format("%s %d", message, percents))
                }
            })
            publishProgress(App.getContext().getString(R.string.update_base))
            ForumsTable.updateForums(res.items)
            return res
        } catch (e: Throwable) {
            val res = ForumsData()
            res.error = e

            return res
        }

    }

    override fun onProgressUpdate(vararg progress: String) {
        listener.onProgressChange(dialog, progress[0])
    }

    override fun onPreExecute() {
        try {
            this.dialog.show()
        } catch (ex: Exception) {
            AppLog.e(null, ex)
        }

    }


    override fun onPostExecute(data: ForumsData?) {
        try {
            if (this.dialog.isShowing) {
                this.dialog.dismiss()
            }
        } catch (ex: Exception) {
            AppLog.e(null, ex)
        }
        listener.done()


        if (data?.error != null) {
            AppLog.e(context.get() ?: App.getInstance(), data.error)
        }

    }
}


internal interface IProgressListener {
    fun onProgressChange(dialog: MaterialDialog, message: String)
    fun done()
}


