package org.softeg.slartus.forpdaplus.controls.quickpost

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.quickpost.PostTask.PostResult
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment.Companion.newPost
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.utils.LogUtil
import java.lang.ref.WeakReference

class QuickPostFragment : Fragment() {
    private var emptyText = true
    private var mForumId: String? = null
    private var mTopicId: String? = null
    private var mAuthKey: String? = null
    private var mPostSendListener: PostSendListener? = null
    private var mPostEditText: EditText? = null
    private var parentTag = ""
    fun setParentTag(tag: String) {
        parentTag = tag
    }

    private var mPopupPanelView: PopupPanelView? = null
    fun hidePopupWindow() {
        mPopupPanelView?.hidePopupWindow()
    }

    interface PostSendListener {
        fun onAfterSendPost(postResult: PostResult?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mPostSendListener = when {
            context is PostSendListener -> context
            parentFragment is PostSendListener -> parentFragment as PostSendListener
            else -> throw RuntimeException("$context must implement PostSendListener")
        }
    }

    fun setTopic(forumId: String?, topicId: String?, authKey: String?) {
        mForumId = forumId
        mTopicId = topicId
        mAuthKey = authKey
        mPopupPanelView?.setTopic(mForumId, mTopicId, mAuthKey)
    }

    fun clearPostBody() {
        if (mPostEditText!!.text != null) mPostEditText!!.text.clear()
    }

    val postBody: String
        get() = if (mPostEditText!!.text != null) mPostEditText!!.text.toString() else ""

    fun insertTextToPost(text: String?, cursorPosition: Int = -1) {
        val selection = mPostEditText?.selectionStart ?: -1
        mPostEditText?.text?.insert(if (selection == -1) 0 else selection, text)
        if (cursorPosition != -1) mPostEditText?.setSelection((if (selection == -1) 0 else selection) + cursorPosition)
    }

    fun hideKeyboard() {
        if (activity == null) return
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(mPostEditText?.windowToken, 0)
    }

    fun showKeyboard() {
        if (activity == null) return
        mPostEditText?.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(mPostEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            mPostEditText?.setText(savedInstanceState.getString("QuickPostFragment.Post"))
            mForumId = savedInstanceState.getString("QuickPostFragment.ForumId")
            mTopicId = savedInstanceState.getString("QuickPostFragment.TopicId")
            mAuthKey = savedInstanceState.getString("QuickPostFragment.AuthKey")
        }
        mPopupPanelView?.setTopic(mForumId, mTopicId, mAuthKey)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mPostEditText?.text != null) outState.putString("QuickPostFragment.Post", mPostEditText?.text.toString())
        outState.putString("QuickPostFragment.ForumId", mForumId)
        outState.putString("QuickPostFragment.TopicId", mTopicId)
        outState.putString("QuickPostFragment.AuthKey", mAuthKey)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.quick_post_fragment, null as ViewGroup?)!!
        val sendButton = v.findViewById<ImageButton>(R.id.send_button)
        sendButton.setOnClickListener { startPost() }
        sendButton.setOnLongClickListener {
            hideKeyboard()
            newPost(activity!!, mForumId!!, mTopicId!!, mAuthKey!!,
                    postBody, parentTag)
            LogUtil.D("QUICK BOOM", "key $mAuthKey")
            true
        }
        mPostEditText = v.findViewById(R.id.post_text)
        mPostEditText?.setOnEditorActionListener { _: TextView?, _: Int, _: KeyEvent? -> false }
        mPostEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    if (!emptyText) {
                        sendButton.clearColorFilter()
                        emptyText = true
                    }
                } else {
                    if (emptyText) {
                        sendButton.setColorFilter(ContextCompat.getColor(App.getContext(), R.color.selectedItemText), PorterDuff.Mode.SRC_ATOP)
                        emptyText = false
                    }
                }
            }
        })
        val advancedButton = v.findViewById<ImageButton>(R.id.advanced_button)
        mPopupPanelView = PopupPanelView(PopupPanelView.VIEW_FLAG_ALL)
        mPopupPanelView?.createView(inflater, advancedButton, mPostEditText)
        mPopupPanelView?.activityCreated(activity, v)
        return v
    }

    private fun startPost() {
        if (emptyText) {
            val toast = Toast.makeText(context, R.string.enter_message, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, App.getInstance().resources.displayMetrics).toInt())
            toast.show()
            return
        }
        if (Preferences.Topic.confirmSend==true) {
            val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.send_post_confirm_dialog, null as ViewGroup?)!!
            val checkBox = view.findViewById<CheckBox>(R.id.chkConfirmationSend)
            MaterialDialog.Builder(activity!!)
                    .title(R.string.confirm_action)
                    .customView(view, true)
                    .positiveText(R.string.send)
                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                        if (!checkBox.isChecked) Preferences.Topic.confirmSend = false
                        post()
                    }
                    .negativeText(R.string.cancel)
                    .show()
        } else {
            post()
        }
    }

    fun post() {
        try {
            hideKeyboard()
            val postTask: PostTask = InnerPostTask(
                    this,
                    activity,
                    if (mPostEditText!!.text == null) "" else mPostEditText!!.text.toString(),
                    mForumId, mTopicId, mAuthKey,
                    Preferences.Topic.Post.enableEmotics, Preferences.Topic.Post.enableSign
            )
            postTask.execute()
        } catch (ex: Throwable) {
            AppLog.e(activity, ex)
        }
    }

    override fun onDestroy() {

        mPopupPanelView?.destroy()
        mPopupPanelView = null

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mPopupPanelView?.pause()
    }

    override fun onResume() {
        super.onResume()
        mPopupPanelView?.resume()
    }

    private class InnerPostTask(
            fragment: QuickPostFragment,
            context: Context?, post: String?, forumId: String?,
            topicId: String?, authKey: String?, enableEmotics: Boolean?, enableSign: Boolean?)
        : PostTask(context, post, forumId, topicId, authKey, enableEmotics, enableSign) {
        var quickPostFragment: WeakReference<QuickPostFragment>? = null

        init {
            quickPostFragment = WeakReference(fragment)
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)
            quickPostFragment?.get()?.let { fragment ->
                mPostResult.Success = success
                if (success) fragment.mPostEditText?.text?.clear()
                fragment.mPostSendListener?.onAfterSendPost(mPostResult)
            }

        }
    }
}