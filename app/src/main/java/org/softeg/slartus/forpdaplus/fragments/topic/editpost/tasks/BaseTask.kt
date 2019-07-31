package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragmentListener

import java.lang.ref.WeakReference


abstract class BaseTask<Params, Progress>(
        editPostFragmentListener: EditPostFragmentListener,
        progressMessageResId: Int)
    : AsyncTask<Params, Progress, Boolean>() {
    protected val editPostFragmentListener: WeakReference<EditPostFragmentListener> = WeakReference(editPostFragmentListener)

    init {
        this.editPostFragmentListener.get()?.getContext()?.let {
            dialog = createProgressDialolg(it, progressMessageResId)
        }
    }

    protected open fun createProgressDialolg(context: Context, progressMessageResId: Int): MaterialDialog = MaterialDialog.Builder(context)
            .progress(true, 0)
            .cancelListener { cancel(true) }
            .content(progressMessageResId)
            .build()

    protected var dialog: MaterialDialog? = null

    protected var ex: Exception? = null

    abstract fun work(params: Array<out Params>)

    override fun doInBackground(vararg params: Params): Boolean {
        return try {
            work(params)
            true
        } catch (e: Exception) {
            ex = e
            false
        }

    }

    override fun onPreExecute() {
        this.dialog?.show()
    }

    override fun onCancelled() {
        Toast.makeText(this.editPostFragmentListener.get()?.getContext()
                ?: App.getContext(), R.string.canceled, Toast.LENGTH_SHORT).show()
        //finish();
    }

    abstract fun onSuccess()

    fun showError() {
        if (ex != null)
            AppLog.e(this.editPostFragmentListener.get()?.getContext(), ex)
        else
            Toast.makeText(this.editPostFragmentListener.get()?.getContext()
                    ?: App.getContext(), R.string.unknown_error,
                    Toast.LENGTH_SHORT).show()
    }

    override fun onPostExecute(success: Boolean) {
        if (this.dialog?.isShowing == true) {
            this.dialog?.dismiss()
        }

        if (success) {
            onSuccess()
        } else {
            showError()
        }
    }
}