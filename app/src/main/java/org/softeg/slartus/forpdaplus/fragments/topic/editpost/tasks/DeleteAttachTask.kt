package org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks

import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragmentListener

class DeleteAttachTask internal constructor(listener: EditPostFragmentListener,
                                            private val postId: String,
                                            private val attachId: String)
    : BaseTask<String, Void>(listener, R.string.deleting_file) {
    override fun work(params: Array<out String>) {
        PostApi.deleteAttachedFile(Client.getInstance(), postId, attachId)
    }

    override fun onSuccess() {
        editPostFragmentListener.get()?.onDeleteAttachTaskSuccess(attachId)
    }
}