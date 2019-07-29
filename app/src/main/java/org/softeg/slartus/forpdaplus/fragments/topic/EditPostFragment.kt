package org.softeg.slartus.forpdaplus.fragments.topic

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.util.Pair
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.post.EditPost
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.ImageFilePath
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.tabs.TabItem

import java.util.ArrayList
import java.util.Arrays
import java.util.Timer
import java.util.TimerTask

/**
 * Created by radiationx on 30.10.15.
 */
class EditPostFragment : GeneralFragment() {
    private var txtPost: EditText? = null
    private var txtpost_edit_reason: EditText? = null
    private var btnAttachments: Button? = null
    private var progress_search: ProgressBar? = null
    private var m_EditPost: EditPost? = null

    private var m_AttachFilePaths: ArrayList<String>? = ArrayList()
    private var lastSelectDirPath: String? = Environment.getExternalStorageDirectory().path

    internal val uiHandler = Handler()

    private var parentTag: String? = ""
    private var emptyText = true

    private var m_BottomPanel: View? = null
    private var mPopupPanelView: PopupPanelView? = null


    private val isNewPost: Boolean
        get() = PostApi.NEW_POST_ID == m_EditPost!!.id

    private var mAttachesListDialog: Dialog? = null


    val postText: String
        get() = if (txtPost!!.text == null) "" else txtPost!!.text.toString()

    val editReasonText: String
        get() = if (txtpost_edit_reason!!.text == null) "" else txtpost_edit_reason!!.text.toString()

    private val SEARCH_RESULT_FOUND = 1
    private val SEARCH_RESULT_NOTFOUND = 0
    private val SEARCH_RESULT_EMPTYTEXT = -1

    private var m_SearchTimer: Timer? = null

    var searchEditText: EditText

    override fun hidePopupWindows() {
        super.hidePopupWindows()
        mPopupPanelView!!.hidePopupWindow()
    }

    override fun getSupportActionBar(): ActionBar? {
        return mainActivity.supportActionBar
    }

    override fun closeTab(): Boolean {
        if (!TextUtils.isEmpty(txtPost!!.text)) {
            MaterialDialog.Builder(mainActivity)
                    .title(R.string.confirm_action)
                    .content(R.string.text_not_empty)
                    .positiveText(R.string.ok)
                    .onPositive { dialog, which -> mainActivity.tryRemoveTab(tag) }
                    .negativeText(R.string.cancel)
                    .show()
            return true
        } else {
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    override fun onResume() {
        super.onResume()
        setArrow()
        if (mPopupPanelView != null)
            mPopupPanelView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        if (mPopupPanelView != null)
            mPopupPanelView!!.pause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.edit_post_plus, container, false)

        progress_search = findViewById(R.id.progress_search) as ProgressBar
        lastSelectDirPath = App.getInstance().preferences.getString("EditPost.AttachDirPath", lastSelectDirPath)

        m_BottomPanel = findViewById(R.id.bottomPanel)

        val send_button = view.findViewById<Button>(R.id.btnSendPost)
        send_button.setOnClickListener { view -> sendMail() }

        txtPost = findViewById(R.id.txtPost) as EditText

        txtpost_edit_reason = findViewById(R.id.txtpost_edit_reason) as EditText
        txtPost!!.setOnEditorActionListener { v, actionId, event -> false }
        txtPost!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    if (!emptyText) {
                        send_button.setTextColor(ContextCompat.getColor(App.getContext(), R.color.accentGray))
                        emptyText = true
                    }
                } else {
                    if (emptyText) {
                        send_button.setTextColor(ContextCompat.getColor(App.getContext(), R.color.accent))
                        emptyText = false
                    }
                }
            }
        })


        btnAttachments = findViewById(R.id.btnAttachments) as Button
        btnAttachments!!.setOnClickListener { view -> showAttachesListDialog() }

        val btnUpload = findViewById(R.id.btnUpload) as ImageButton
        btnUpload.setOnClickListener { view -> startAddAttachment() }

        if (mPopupPanelView == null)
            mPopupPanelView = PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS or PopupPanelView.VIEW_FLAG_BBCODES)
        mPopupPanelView!!.createView(LayoutInflater.from(context), findViewById(R.id.advanced_button) as ImageButton, txtPost)
        mPopupPanelView!!.activityCreated(mainActivity, view)


        try {
            val args = arguments!!
            val forumId = args.getString("forumId")
            val topicId = args.getString("themeId")
            val postId = args.getString("postId")
            val authKey = args.getString("authKey")
            parentTag = args.getString("parentTag")
            m_EditPost = EditPost()
            m_EditPost!!.id = postId
            m_EditPost!!.forumId = forumId
            m_EditPost!!.topicId = topicId
            m_EditPost!!.authKey = authKey
            mPopupPanelView!!.setTopic(forumId, topicId, authKey)

            if (isNewPost) {
                if (args.getString("body") != null) {
                    txtPost!!.setText(args.getString("body"))
                    txtPost!!.setSelection(txtPost!!.text.length)
                }
            }
            setDataFromExtras(args.getBundle("extras"))

            startLoadPost(forumId, topicId, postId, authKey)
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
            mainActivity.tryRemoveTab(tag)
        }

        //createActionMenu();
        return view
    }


    override fun onBackPressed(): Boolean {
        if (!TextUtils.isEmpty(txtPost!!.text)) {
            MaterialDialog.Builder(mainActivity)
                    .title(R.string.confirm_action)
                    .content(getString(R.string.text_not_empty))
                    .positiveText(R.string.ok)
                    .onPositive { dialog, which -> mainActivity.tryRemoveTab(tag) }
                    .negativeText(R.string.cancel)
                    .show()
            return true
        } else {
            return false
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    private fun sendMail() {
        if (emptyText) {
            val toast = Toast.makeText(context, R.string.enter_message, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, App.getInstance().resources.displayMetrics).toInt())
            toast.show()
            return
        }
        val body = postText
        if (Preferences.Topic.getConfirmSend()) {
            MaterialDialog.Builder(context!!)
                    .title(R.string.is_sure)
                    .content(R.string.confirm_sending)
                    .positiveText(R.string.ok)
                    .onPositive { dialog, which -> sendPost(body, editReasonText) }
                    .negativeText(R.string.cancel)
                    .show()
        } else {
            sendPost(body, editReasonText)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (m_EditPost != null)
            outState.putSerializable("EditPost", m_EditPost)
        if (m_AttachFilePaths != null)
            outState.putStringArray("AttachFilePaths", m_AttachFilePaths!!.toTypedArray())
        outState.putString("lastSelectDirPath", lastSelectDirPath)
        outState.putString("postText", postText)
        outState.putString("txtpost_edit_reason", editReasonText)


        super.onSaveInstanceState(outState)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun setDataFromExtras(extras: Bundle?) {
        if (extras == null) return
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            val attachesObject = extras.get(Intent.EXTRA_STREAM)
            if (attachesObject is Uri) {
                val uri = extras.get(Intent.EXTRA_STREAM) as Uri
                val path = ImageFilePath.getPath(mainActivity.applicationContext, uri)
                if (path != null)
                    m_AttachFilePaths = ArrayList(Arrays.asList(path))
                else
                    Toast.makeText(context, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()
            } else if (attachesObject is ArrayList<*>) {
                m_AttachFilePaths = ArrayList()
                for (item in attachesObject) {
                    val uri = item as Uri
                    val path = ImageFilePath.getPath(mainActivity.applicationContext, uri)
                    if (path != null)
                        m_AttachFilePaths!!.add(ImageFilePath.getPath(mainActivity.applicationContext, uri))
                    else
                        Toast.makeText(context, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()

                }
            }
        }

        if (extras.containsKey(Intent.EXTRA_TEXT))
            txtPost!!.setText(extras.get(Intent.EXTRA_TEXT)!!.toString())
        if (extras.containsKey(Intent.EXTRA_HTML_TEXT))
            txtPost!!.setText(extras.get(Intent.EXTRA_HTML_TEXT)!!.toString())
        if (isNewPost) {
            if (extras.containsKey("body"))
                txtPost!!.setText(extras.get("body")!!.toString())
        }
        txtPost!!.setSelection(txtPost!!.text.length)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        var item: MenuItem

        if (!isNewPost) {
            item = menu!!.add(R.string.reason_for_editing).setIcon(R.drawable.pencil)
            item.setOnMenuItemClickListener { menuItem ->
                toggleEditReasonDialog()
                true
            }
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        menu!!.add(R.string.preview).setOnMenuItemClickListener { item1 ->
            val tabItem = App.getInstance().getTabByUrl("preview_" + tag!!)
            if (tabItem == null) {
                PostPreviewFragment.showSpecial(postText, tag)
            } else {
                (tabItem.fragment as PostPreviewFragment).load(postText)
                mainActivity.selectTab(tabItem)
                mainActivity.hidePopupWindows()
            }
            true
        }
        item = menu.add(R.string.find_in_text)
        item.setActionView(R.layout.action_collapsible_search)
        searchEditText = item.actionView.findViewById(R.id.editText)
        searchEditText.setOnKeyListener { view, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val text = if (searchEditText.text == null) "" else searchEditText.text.toString().trim { it <= ' ' }
                startSearch(text, true)
                searchEditText.requestFocus()
                return@searchEditText.setOnKeyListener true
            }

            false
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(mEdit: Editable) {
                val text = mEdit.toString().trim { it <= ' ' }
                startSearch(text, false)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    }

    private fun showAttachesListDialog() {
        if (m_EditPost!!.attaches.size == 0) {
            MaterialDialog.Builder(mainActivity)
                    .content(R.string.no_attachments)
                    .positiveText(R.string.do_download)
                    .negativeText(R.string.cancel)
                    .onPositive { dialog, which -> startAddAttachment() }
                    .show()
            return
        }
        val adapter = AttachesAdapter(m_EditPost!!.attaches)
        mAttachesListDialog = MaterialDialog.Builder(mainActivity)
                .cancelable(true)
                .title(R.string.attachments)
                //.setSingleChoiceItems(adapter, -1, null)
                .adapter(adapter, LinearLayoutManager(activity))
                .neutralText(R.string.in_spoiler)
                .onNeutral { dialog, which ->
                    val listItems = ArrayList<String>()
                    var i = 0
                    while (i <= m_EditPost!!.attaches.size - 1) {
                        listItems.add(m_EditPost!!.attaches[i].name)
                        i++
                    }
                    val items = listItems.toTypedArray<CharSequence>()
                    val str = StringBuilder()
                    MaterialDialog.Builder(context!!)
                            .title(R.string.add_in_spoiler)
                            .positiveText(R.string.add)
                            .negativeText(R.string.cancel)
                            .onPositive { dialog1, which1 ->
                                var selectionStart = txtPost!!.selectionStart
                                if (selectionStart == -1)
                                    selectionStart = 0
                                if (txtPost!!.text != null)
                                //txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getTitle() + "]");
                                    txtPost!!.text.insert(selectionStart, "[spoiler]$str[/spoiler]")
                            }
                            .items(*items)
                            .itemsCallbackMultiChoice(null) { dialog12, which12, text ->
                                str.setLength(0)
                                for (which1 in which12) {
                                    str.append("[attachment=")
                                            .append(m_EditPost!!.attaches[which1!!].id)
                                            .append(":")
                                            .append(m_EditPost!!.attaches[which1].name)
                                            .append("]")
                                }
                                true // allow selection
                            }
                            .alwaysCallMultiChoiceCallback()
                            .show()
                }
                .negativeText(R.string.cancel)
                .build()
        mAttachesListDialog!!.show()
    }

    private fun startAddAttachment() {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(activity, R.string.no_permission, Toast.LENGTH_SHORT).show()
            return
        }
        val items = arrayOf<CharSequence>(getString(R.string.file), getString(R.string.image))
        MaterialDialog.Builder(context!!)
                .items(*items)
                .itemsCallback { dialog, view, i, items1 ->
                    when (i) {
                        0//файл
                        -> try {
                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = "*/*"
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            intent.addCategory(Intent.CATEGORY_OPENABLE)
                            startActivityForResult(intent, MY_INTENT_CLICK_F)

                        } catch (ex: ActivityNotFoundException) {
                            Toast.makeText(mainActivity, R.string.no_app_for_get_file, Toast.LENGTH_LONG).show()
                        } catch (ex: Exception) {
                            AppLog.e(mainActivity, ex)
                        }

                        1// Изображение
                        ->

                            try {
                                val imageintent = Intent(
                                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                    imageintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                startActivityForResult(imageintent, MY_INTENT_CLICK_I)
                            } catch (ex: ActivityNotFoundException) {
                                Toast.makeText(mainActivity, R.string.no_app_for_get_image_file, Toast.LENGTH_LONG).show()
                            } catch (ex: Exception) {
                                AppLog.e(mainActivity, ex)
                            }

                    }
                }
                .show()
    }

    private fun saveAttachDirPath(attachFilePath: String) {
        lastSelectDirPath = FileUtils.getDirPath(attachFilePath)
        App.getInstance().preferences.edit().putString("EditPost.AttachDirPath", lastSelectDirPath).apply()
    }

    private fun helperTask(path: String) {
        saveAttachDirPath(path)
        UpdateTask(mainActivity, path).execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK_I) {
                if (null == data) return
                val selectedImageUri = data.data
                val selectedImagePath = ImageFilePath.getPath(mainActivity.applicationContext, selectedImageUri)
                if (selectedImagePath != null)
                    helperTask(selectedImagePath)
                else
                    Toast.makeText(context, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()


            } else if (requestCode == MY_INTENT_CLICK_F) {
                if (null == data) return
                val path = ImageFilePath.getPath(mainActivity.applicationContext, data.data)
                if (path != null) {
                    if (path.matches("(?i)(.*)(7z|zip|rar|tar.gz|exe|cab|xap|txt|log|jpeg|jpg|png|gif|mp3|mp4|apk|ipa|img|.mtz)$".toRegex())) {
                        helperTask(path)
                    } else {
                        Toast.makeText(mainActivity, R.string.file_not_support_forum, Toast.LENGTH_SHORT).show()
                    }
                } else
                    Toast.makeText(context, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()


            }
        }
    }

    private fun startLoadPost(forumId: String?, topicId: String?, postId: String?, authKey: String?) {
        LoadTask(mainActivity, forumId, topicId, postId, authKey).execute()
    }

    private fun sendPost(text: String, editPostReason: String) {

        if (isNewPost) {
            PostTask(mainActivity, text, editPostReason,
                    Preferences.Topic.Post.getEnableEmotics(), Preferences.Topic.Post.getEnableSign())
                    .execute()
        } else {
            AcceptEditTask(mainActivity, text, editPostReason,
                    Preferences.Topic.Post.getEnableEmotics(), Preferences.Topic.Post.getEnableSign())
                    .execute()
        }
    }

    fun toggleEditReasonDialog() {
        txtpost_edit_reason!!.visibility = if (txtpost_edit_reason!!.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }


    private inner class UpdateTask internal constructor(context: Context, private val attachFilePaths: List<String>) : AsyncTask<String, Pair<String, Int>, Boolean>() {
        private val dialog: MaterialDialog
        private var m_ProgressState: ProgressState? = null

        private var editAttach: EditAttach? = null

        private var ex: Throwable? = null

        init {
            dialog = MaterialDialog.Builder(context)
                    .progress(false, 100, false)
                    .content(R.string.sending_file)
                    .show()
        }

        internal constructor(context: Context, newAttachFilePath: String) : this(context, ArrayList<String>(Arrays.asList<String>(newAttachFilePath))) {}

        override fun doInBackground(vararg params: String): Boolean? {
            try {
                m_ProgressState = object : ProgressState() {
                    override fun update(message: String, percents: Int) {
                        publishProgress(Pair("", percents))
                    }
                }

                var i = 1
                for (newAttachFilePath in attachFilePaths) {
                    publishProgress(Pair(String.format(App.getContext().getString(R.string.format_sending_file), i++, attachFilePaths.size), 0))
                    editAttach = PostApi.attachFile(Client.getInstance(),
                            m_EditPost!!.id, newAttachFilePath, m_ProgressState)
                }

                return true
            } catch (e: Throwable) {
                ex = e
                return false
            }

        }

        override fun onProgressUpdate(vararg values: Pair<String, Int>) {
            super.onProgressUpdate(*values)
            if (!TextUtils.isEmpty(values[0].first))
                dialog.setContent(values[0].first)
            dialog.setProgress(values[0].second)
        }

        // can use UI thread here
        override fun onPreExecute() {
            this.dialog.setCancelable(true)
            this.dialog.setCanceledOnTouchOutside(false)
            this.dialog.setOnCancelListener { dialogInterface ->
                if (m_ProgressState != null)
                    m_ProgressState!!.cancel()
                cancel(false)
            }
            this.dialog.setProgress(0)

            this.dialog.show()
        }

        // can use UI thread here
        override fun onPostExecute(success: Boolean?) {
            if (this.dialog.isShowing) {
                this.dialog.dismiss()
            }

            if (success!! || isCancelled && editAttach != null) {
                m_EditPost!!.addAttach(editAttach)
                refreshAttachmentsInfo()

                //                if (!pathToFile.isEmpty()) {
                //                    File deleteFile = new File(pathToFile);
                //                    if (deleteFile.exists()) {
                //                        deleteFile.delete();
                //                    }
                //                }

            } else {

                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()

            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        override fun onCancelled(success: Boolean?) {
            super.onCancelled(success)
            if (success!! || isCancelled && editAttach != null) {
                m_EditPost!!.addAttach(editAttach)
                refreshAttachmentsInfo()
            } else {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()

            }
        }

    }

    override fun onDestroy() {
        if (mPopupPanelView != null) {
            mPopupPanelView!!.destroy()
            mPopupPanelView = null
        }
        val tabItem = App.getInstance().getTabByUrl("preview_" + tag!!)
        if (tabItem != null) mainActivity.tryRemoveTab(tabItem.tag)
        super.onDestroy()
    }

    private inner class DeleteAttachTask internal constructor(context: Context, private val attachId: String) : AsyncTask<String, Void, Boolean>() {
        private val dialog: MaterialDialog

        private var ex: Exception? = null

        init {

            dialog = MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.deleting_file)
                    .build()
        }


        override fun doInBackground(vararg params: String): Boolean? {
            try {
                PostApi.deleteAttachedFile(Client.getInstance(), m_EditPost!!.id, attachId)
                return true
            } catch (e: Exception) {
                ex = e
                return false
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

            if (success!!) {
                m_EditPost!!.deleteAttach(attachId)
                refreshAttachmentsInfo()
            } else {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class AcceptEditTask internal constructor(context: Context,
                                                            private val postBody: String, private val postEditReason: String, private val enableEmo: Boolean?, private val enableSign: Boolean?) : AsyncTask<String, Void, Boolean>() {
        private val dialog: MaterialDialog

        private var ex: Exception? = null

        init {
            dialog = MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.edit_message)
                    .build()
        }

        override fun doInBackground(vararg params: String): Boolean? {
            try {
                PostApi.sendPost(Client.getInstance(), m_EditPost!!.params, postBody,
                        postEditReason, enableSign, enableEmo)
                return true
            } catch (e: Exception) {
                ex = e
                return false
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

            if (success!!) {
                if (App.getInstance().isContainsByTag(parentTag)) {
                    (App.getInstance().getTabByTag(parentTag)!!.fragment as ThemeFragment)
                            .showTheme(ThemeFragment.getThemeUrl(m_EditPost!!.topicId, "view=findpost&p=" + m_EditPost!!.id!!), true)
                }
                mainActivity.tryRemoveTab(tag)
            } else {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error,
                            Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun setEditPost(editPost: EditPost?) {
        m_EditPost = editPost
        if (PostApi.NEW_POST_ID != m_EditPost!!.id)
            txtPost!!.setText(m_EditPost!!.body)
        txtpost_edit_reason!!.setText(m_EditPost!!.postEditReason)
        refreshAttachmentsInfo()
    }

    private fun refreshAttachmentsInfo() {
        btnAttachments!!.text = m_EditPost!!.attaches.size.toString() + ""
    }

    private inner class LoadTask internal constructor(context: Context, private val forumId: String, private val topicId: String, private val postId: String, private val authKey: String) : AsyncTask<String, Void, Boolean>() {
        private val dialog: MaterialDialog

        private var editPost: EditPost? = null

        private var ex: Throwable? = null

        init {
            dialog = MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .cancelListener { dialog -> cancel(true) }
                    .content(R.string.loading_message)
                    .build()
        }

        override fun doInBackground(vararg params: String): Boolean? {
            try {
                editPost = PostApi.editPost(Client.getInstance(), forumId, topicId, postId, authKey)

                return true
            } catch (e: Throwable) {
                ex = e
                return false
            }

        }

        override fun onPreExecute() {
            this.dialog.show()
        }

        override fun onCancelled() {
            Toast.makeText(mainActivity, R.string.canceled, Toast.LENGTH_SHORT).show()
            //finish();
        }

        // can use UI thread here
        override fun onPostExecute(success: Boolean?) {
            if (this.dialog.isShowing) {
                this.dialog.dismiss()
            }

            if (success!!) {
                setEditPost(editPost)

                if (m_AttachFilePaths!!.size > 0)
                    UpdateTask(mainActivity, m_AttachFilePaths)
                            .execute()
                m_AttachFilePaths = ArrayList()
            } else {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class PostTask internal constructor(context: Context,
                                                      private val postBody: String, private val postEditReason: String, private val enableEmo: Boolean?, private val enableSign: Boolean?) : AsyncTask<String, Void, Boolean>() {
        private val dialog: MaterialDialog
        private var mPostResult: String? = null// при удачной отправке страница топика
        private var mError: String? = null

        private var ex: Exception? = null

        init {
            dialog = MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.sending_message)
                    .build()
        }

        override fun doInBackground(vararg params: String): Boolean? {
            try {
                mPostResult = PostApi.sendPost(Client.getInstance(), m_EditPost!!.params, postBody,
                        postEditReason, enableSign, enableEmo)

                mError = PostApi.checkPostErrors(mPostResult)
                return true
            } catch (e: Exception) {
                ex = e
                return false
            }

        }

        override fun onPreExecute() {
            this.dialog.show()
        }

        override fun onPostExecute(success: Boolean?) {
            if (this.dialog.isShowing) {
                this.dialog.dismiss()
            }

            if (success!!) {
                if (!TextUtils.isEmpty(mError)) {
                    Toast.makeText(mainActivity, App.getContext().getString(R.string.error) + ": " + mError, Toast.LENGTH_LONG).show()
                    return
                }
                if (App.getInstance().isContainsByTag(parentTag)) {
                    (App.getInstance().getTabByTag(parentTag)!!.fragment as ThemeFragment)
                            .showTheme(String.format("http://4pda.ru/forum/index.php?showtopic=%s&%s", m_EditPost!!.topicId,
                                    if (isNewPost) "view=getlastpost" else "view=findpost&p=" + m_EditPost!!.id!!), true)
                }
                mainActivity.tryRemoveTab(tag)

            } else {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error,
                            Toast.LENGTH_SHORT).show()

            }
        }

    }

    inner class AttachesAdapter internal constructor(private val content: List<EditAttach>) : RecyclerView.Adapter<AttachesAdapter.AttachViewHolder>() {

        fun getItem(i: Int): EditAttach {
            return content[i]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachViewHolder {
            val group = LayoutInflater.from(parent.context).inflate(R.layout.attachment_spinner_item, parent, false) as ViewGroup
            return AttachViewHolder(group)
        }

        override fun onBindViewHolder(holder: AttachViewHolder, position: Int) {
            val attach = content[position]
            holder.btnSpoiler.tag = attach
            holder.txtFile.text = attach.name
            holder.txtFile.tag = attach

            holder.btnDelete.setOnClickListener { view13 ->
                mAttachesListDialog!!.dismiss()
                DeleteAttachTask(mainActivity,
                        attach.id)
                        .execute()
            }

            holder.btnSpoiler.setOnClickListener { view12 ->
                mAttachesListDialog!!.dismiss()

                var selectionStart = txtPost!!.selectionStart
                if (selectionStart == -1)
                    selectionStart = 0
                if (txtPost!!.text != null)
                    txtPost!!.text.insert(selectionStart, "[spoiler][attachment=" + attach.id + ":" + attach.name + "][/spoiler]")
            }

            holder.txtFile.setOnClickListener { view1 ->
                mAttachesListDialog!!.dismiss()
                var selectionStart = txtPost!!.selectionStart
                if (selectionStart == -1)
                    selectionStart = 0
                if (txtPost!!.text != null)
                    txtPost!!.text.insert(selectionStart, "[attachment=" + attach.id + ":" + attach.name + "]")
            }
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getItemCount(): Int {
            return content.size
        }

        internal inner class AttachViewHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {

            var btnSpoiler: ImageButton
            var btnDelete: ImageButton
            var txtFile: TextView

            init {
                btnDelete = convertView.findViewById(R.id.btnDelete)
                btnSpoiler = convertView.findViewById(R.id.btnSpoiler)
                txtFile = convertView.findViewById(R.id.txtFile)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        if (!supportActionBar!!.isShowing) {
            supportActionBar!!.show()
            m_BottomPanel!!.visibility = View.VISIBLE
        }
    }

    private fun clearPostHighlight(): Spannable {
        val startSearchSelection = txtPost!!.selectionStart
        val raw = SpannableString(if (txtPost!!.text == null) "" else txtPost!!.text)
        val spans = raw.getSpans(0,
                raw.length,
                BackgroundColorSpan::class.java)

        for (span in spans) {
            raw.removeSpan(span)
        }
        txtPost!!.setSelection(startSearchSelection)
        txtPost!!.isCursorVisible = true
        return raw
    }

    fun startSearch(searchText: String, fromSelection: Boolean?) {

        if (m_SearchTimer != null) {
            m_SearchTimer!!.cancel()
            m_SearchTimer!!.purge()
        }
        m_SearchTimer = Timer()
        m_SearchTimer!!.schedule(object : TimerTask() {
            override fun run() {
                uiHandler.post {
                    if (search(searchText, fromSelection) == SEARCH_RESULT_NOTFOUND)
                        searchEditText.error = getString(R.string.no_matches_found)
                    else
                        searchEditText.error = null

                }
                m_SearchTimer!!.cancel()
                m_SearchTimer!!.purge()
            }
        }, 1000, 5000)


    }

    fun search(searchText: String, fromSelection: Boolean?): Int {
        var searchText = searchText
        if (TextUtils.isEmpty(searchText)) return SEARCH_RESULT_EMPTYTEXT
        try {
            progress_search!!.visibility = View.VISIBLE

            searchText = searchText.toLowerCase()
            val raw = clearPostHighlight()

            var startSearchSelection = 0
            if (fromSelection!!)
                startSearchSelection = txtPost!!.selectionStart + 1
            val text = raw.toString().toLowerCase()


            var findedStartSelection = TextUtils.indexOf(text, searchText, startSearchSelection)
            if (findedStartSelection == -1 && startSearchSelection != 0)
                findedStartSelection = TextUtils.indexOf(text, searchText)

            if (findedStartSelection == -1)
                return SEARCH_RESULT_NOTFOUND

            raw.setSpan(BackgroundColorSpan(-0x74ff75), findedStartSelection, findedStartSelection + searchText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


            txtPost!!.setText(raw)
            txtPost!!.setSelection(findedStartSelection)
            txtPost!!.isCursorVisible = true
            return SEARCH_RESULT_FOUND
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        } finally {
            if ((!fromSelection)!!)
                searchEditText.requestFocus()
            progress_search!!.visibility = View.GONE
        }
        return SEARCH_RESULT_EMPTYTEXT
    }


    override fun getListName(): String? {
        return null
    }

    override fun getListTitle(): String? {
        return null
    }

    override fun loadData(isRefresh: Boolean) {

    }

    override fun startLoad() {

    }

    companion object {

        val NEW_EDIT_POST_REQUEST_CODE = App.getInstance().uniqueIntValue
        val TOPIC_BODY_KEY = "EditPostActivity.TOPIC_BODY_KEY"
        val POST_URL_KEY = "EditPostActivity.POST_URL_KEY"


        val thisFragmentUrl = "EditPostFragment"

        fun newInstance(args: Bundle): EditPostFragment {
            val fragment = EditPostFragment()

            fragment.arguments = args
            return fragment
        }

        fun editPost(context: Activity, forumId: String, topicId: String, postId: String, authKey: String, tag: String) {
            val url = thisFragmentUrl + forumId + topicId + postId
            val args = Bundle()
            args.putString("forumId", forumId)
            args.putString("themeId", topicId)
            args.putString("postId", postId)
            args.putString("authKey", authKey)
            args.putString("parentTag", tag)
            MainActivity.addTab(context.getString(R.string.edit_post_combined) + context.getString(R.string.combined_in) + App.getInstance().getTabByTag(tag)!!.title, url, newInstance(args))
        }

        fun newPost(context: Activity, forumId: String, topicId: String, authKey: String,
                    body: String, tag: String) {
            val url = thisFragmentUrl + forumId + topicId + PostApi.NEW_POST_ID
            val args = Bundle()
            args.putString("forumId", forumId)
            args.putString("themeId", topicId)
            args.putString("postId", PostApi.NEW_POST_ID)
            args.putString("body", body)
            args.putString("authKey", authKey)
            args.putString("parentTag", tag)
            MainActivity.addTab(context.getString(R.string.answer) + context.getString(R.string.combined_in) + App.getInstance().getTabByTag(tag)!!.title, url, newInstance(args))
        }

        fun newPostWithAttach(context: Context, forumId: String, topicId: String, authKey: String,
                              extras: Bundle) {
            val url = thisFragmentUrl + forumId + topicId + PostApi.NEW_POST_ID
            val args = Bundle()
            args.putString("forumId", forumId)
            args.putString("themeId", topicId)
            args.putString("postId", PostApi.NEW_POST_ID)
            args.putBundle("extras", extras)
            args.putString("authKey", authKey)
            MainActivity.addTab(context.getString(R.string.edit_post_combined), url, newInstance(args))
        }

        private val MY_INTENT_CLICK_I = 302
        private val MY_INTENT_CLICK_F = 303
    }
}
