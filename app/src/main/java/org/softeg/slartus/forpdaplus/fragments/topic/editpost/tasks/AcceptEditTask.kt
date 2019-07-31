package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import org.softeg.slartus.forpdaapi.post.EditPost
import org.softeg.slartus.forpdaapi.post.EditPostParams
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragmentListener

class AcceptEditTask internal constructor(listener: EditPostFragmentListener,
                                          private val editPost: EditPost?,
                                          private val postBody: String,
                                          private val postEditReason: String,
                                          private val enableEmo: Boolean?,
                                          private val enableSign: Boolean?) : BaseTask<String, Void>(listener, R.string.edit_message) {
    override fun work(params: Array<out String>) {
        PostApi.sendPost(Client.getInstance(), editPost?.params?: EditPostParams(), postBody,
                postEditReason, enableSign, enableEmo)
    }

    override fun onSuccess() {
        editPostFragmentListener.get()?.onAcceptEditTaskSuccess(editPost)
    }
}