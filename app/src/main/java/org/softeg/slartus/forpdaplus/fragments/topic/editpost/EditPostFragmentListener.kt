package org.softeg.slartus.forpdaplus.fragments.topic.editpost

import android.content.Context
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.post.EditPost

interface EditPostFragmentListener {
    fun getContext(): Context?
    fun onLoadTaskSuccess(editPost: EditPost?)
    fun onUpdateTaskSuccess(editAttach: EditAttach?)
    fun onDeleteAttachTaskSuccess(attachId:String)
    fun onAcceptEditTaskSuccess(editPost:EditPost?)
    fun onPostTaskSuccess(editPost:EditPost?, error:String?)
}