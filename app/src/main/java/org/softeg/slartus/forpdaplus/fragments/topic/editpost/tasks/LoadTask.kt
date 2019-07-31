package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import org.softeg.slartus.forpdaapi.post.EditPost
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragmentListener


class LoadTask internal constructor(editPostFragmentListener: EditPostFragmentListener,
                                    private val forumId: String,
                                    private val topicId: String,
                                    private val postId: String,
                                    private val authKey: String) : BaseTask<String, Void>(editPostFragmentListener, R.string.loading_message) {
    private var editPost: EditPost? = null
    override fun work(params: Array<out String>) {
        editPost = PostApi.editPost(Client.getInstance(), forumId, topicId, postId, authKey)
    }

    override fun onSuccess() {
        editPostFragmentListener.get()?.onLoadTaskSuccess(editPost)
    }
}