package org.softeg.slartus.forpdaplus.fragments.qms.tasks

import android.os.AsyncTask
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.qms.QmsApi
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment
import java.lang.ref.WeakReference
import java.util.*

class DeleteDialogTask internal constructor(qmsChatFragment: QmsChatFragment,
                                            private val contactId:String,
                                            private var ids: ArrayList<String>) : AsyncTask<ArrayList<String>, Void, Boolean>() {


    private val dialog: MaterialDialog = MaterialDialog.Builder(qmsChatFragment.context!!)
            .progress(true, 0)
            .content(R.string.deleting_dialogs)
            .build()
    private val qmsChatFragment = WeakReference(qmsChatFragment)
    private var ex: Throwable? = null

    override fun doInBackground(vararg params: ArrayList<String>): Boolean? {
        return try {
            QmsApi.deleteDialogs(Client.getInstance(), contactId, ids)
            true
        } catch (e: Throwable) {
            ex = e
            false
        }
    }

    // can use UI thread here
    override fun onPreExecute() {
        this.dialog.show()
    }

    // can use UI thread here
    override fun onPostExecute(success: Boolean?) {
        if (this.dialog.isShowing) {
            this.dialog.dismiss()
        }

        if (success != true) {
            if (ex != null)
                AppLog.e(qmsChatFragment.get()?.context, ex)
            else
                Toast.makeText(qmsChatFragment.get()?.context?: App.getInstance(), R.string.unknown_error,
                        Toast.LENGTH_SHORT).show()
        }
        qmsChatFragment.get()?.stopDeleteMode(true)
        //showThread();

    }
}