package org.softeg.slartus.forpdaplus.fragments.qms.tasks

import android.os.AsyncTask
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.qms.QmsApi
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment
import java.lang.ref.WeakReference
import java.util.*

class DeleteTask internal constructor(qmsChatFragment: QmsChatFragment,
                                      private val contactId: String,
                                      private val themeId: String,
                                      private val postIds:List<String>,
                                      private val daysCount:Int?) : AsyncTask<ArrayList<String>, Void, Boolean>() {

    private val dialog: MaterialDialog = MaterialDialog.Builder(qmsChatFragment.context!!)
            .progress(true, 0)
            .content(R.string.deleting_messages)
            .build()
    private val qmsChatFragment = WeakReference(qmsChatFragment)
    private var chatBody: String? = ""
    private var ex: Throwable? = null

    override fun doInBackground(vararg params: ArrayList<String>): Boolean? {
        return try {
            chatBody = qmsChatFragment.get()?.transformChatBody(QmsApi.deleteMessages(Client.getInstance(),
                    contactId, themeId, postIds, QmsChatFragment.encoding,daysCount))
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

        qmsChatFragment.get()?.onPostChat(chatBody?:"", success?:false, ex)
        qmsChatFragment.get()?.stopDeleteMode(true)
    }
}