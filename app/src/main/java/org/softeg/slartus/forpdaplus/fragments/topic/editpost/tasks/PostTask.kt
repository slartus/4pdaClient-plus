package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import org.softeg.slartus.forpdaapi.post.EditPost
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragmentListener

class PostTask internal constructor(listener: EditPostFragmentListener,
                                    private val editPost: EditPost?,
                                    private val postBody: String,
                                    private val postEditReason: String,
                                    private val enableEmo: Boolean?,
                                    private val enableSign: Boolean?) : BaseTask<String, Void>(listener, R.string.sending_message) {
    private var mPostResult: String? = null// при удачной отправке страница топика
    private var mError: String? = null
    override fun work(params: Array<out String>) {
        mPostResult = PostApi.sendPost(Client.getInstance(), editPost!!.params, postBody,
                postEditReason, enableSign, enableEmo)

        mPostResult?.let {
            mError = PostApi.checkPostErrors(it)
        }
    }

    override fun onSuccess() {
        editPostFragmentListener.get()?.onPostTaskSuccess(editPost, mError)
    }
}