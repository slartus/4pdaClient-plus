package org.softeg.slartus.forpdaplus.fragments.qms.tasks

import android.os.AsyncTask
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.qms.QmsApi
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.fragments.qms.QmsChatFragment
import java.lang.ref.WeakReference
import java.util.*

class SendTask internal constructor(
    qmsChatFragment: QmsChatFragment,
    private val contactId: String,
    private val themeId: String,
    private val messageText: String,
    private val attachs: ArrayList<EditAttach>,
    private val daysCount: Int?
) : AsyncTask<ArrayList<String>, Void, Boolean>() {
    private val dialog = MaterialDialog.Builder(qmsChatFragment.requireContext())
        .progress(true, 0)
        .content(qmsChatFragment.context!!.getString(R.string.sending_message))
        .build()
    private var chatBody: String? = null
    private var ex: Throwable? = null
    private val qmsChatFragment = WeakReference(qmsChatFragment)

    override fun doInBackground(vararg params: ArrayList<String>): Boolean? {
        return try {
            val qmsPage = QmsApi.sendMessage(
                Client.getInstance(), contactId, themeId, messageText,
                QmsChatFragment.encoding, attachs, daysCount
            )
            chatBody = qmsChatFragment.get()?.transformChatBody(qmsPage.body ?: "")
            true
        } catch (e: Throwable) {
            ex = e
            false
        }
    }

    // can use UI thread here
    override fun onPreExecute() {
        this.dialog.show()
        //            setLoading(false); //
    }

    // can use UI thread here
    override fun onPostExecute(success: Boolean?) {
        if (this.dialog.isShowing) {
            this.dialog.dismiss()
        }
        if (ex != null) {
            AppLog.e(qmsChatFragment.get()?.activity, ex)
        }
        //            setLoading(false);
        chatBody?.let {
            qmsChatFragment.get()?.onPostChat(it, success ?: false, ex)
        }

        qmsChatFragment.get()?.clearAttaches()
    }

}