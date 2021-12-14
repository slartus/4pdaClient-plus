package org.softeg.slartus.forpdaplus.fragments.topic.editpost

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.*
import android.text.style.BackgroundColorSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import io.paperdb.Paper
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.SingleSubject
import kotlinx.android.synthetic.main.edit_post_plus.*
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.post.EditPost
import org.softeg.slartus.forpdaapi.post.PostApi
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.FilePath
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.common.TrueQueue
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment
import org.softeg.slartus.forpdaplus.fragments.topic.PostPreviewFragment
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.tasks.*
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.tabs.TabsManager
import org.softeg.slartus.hosthelper.HostHelper
import java.util.*

class EditPostFragment : GeneralFragment(), EditPostFragmentListener {

    private var txtPost: EditText? = null
    private var txtPostEditReason: EditText? = null
    private var btnAttachments: Button? = null
    private var progressSearch: ProgressBar? = null
    private var mEditpost: EditPost? = null

    private var mAttachfilepaths: ArrayList<Uri> = ArrayList()
    private var lastSelectDirPath: String? = Environment.getExternalStorageDirectory().path

    internal val uiHandler = Handler(Looper.getMainLooper())

    private var parentTag: String? = ""
    private var emptyText = true

    private var mBottompanel: View? = null
    private var mPopupPanelView: PopupPanelView? = null

    private val isNewPost: Boolean
        get() = PostApi.NEW_POST_ID == mEditpost!!.id

    private var mAttachesListDialog: Dialog? = null

    private val postText: String
        get() = if (txtPost!!.text == null) "" else txtPost!!.text.toString()

    private val editReasonText: String
        get() = if (txtPostEditReason!!.text == null) "" else txtPostEditReason!!.text.toString()

    private var mSearchTimer: Timer? = null

    var searchEditText: EditText? = null

    override fun hidePopupWindows() {
        super.hidePopupWindows()
        mPopupPanelView!!.hidePopupWindow()
    }

    override fun getSupportActionBar(): ActionBar? {
        return mainActivity.supportActionBar
    }

    override fun closeTab(): Boolean {
        return if (!TextUtils.isEmpty(txtPost!!.text)) {
            MaterialDialog.Builder(mainActivity)
                .title(R.string.confirm_action)
                .content(R.string.text_not_empty)
                .positiveText(R.string.ok)
                .onPositive { _, _ -> mainActivity.tryRemoveTab(tag) }
                .negativeText(R.string.cancel)
                .show()
            true
        } else {
            false
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.edit_post_plus, container, false)

        progressSearch = findViewById(R.id.progress_search) as ProgressBar
        lastSelectDirPath =
            App.getInstance().preferences.getString("EditPost.AttachDirPath", lastSelectDirPath)

        mBottompanel = findViewById(R.id.bottomPanel)

        val sendButton = view.findViewById<Button>(R.id.btnSendPost)
        sendButton.setOnClickListener { sendMail() }

        txtPost = findViewById(R.id.txtPost) as EditText?

        txtPostEditReason = findViewById(R.id.txtpost_edit_reason) as EditText?
        txtPost?.setOnEditorActionListener { _, _, _ -> false }
        txtPost?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    if (!emptyText) {
                        sendButton.setTextColor(
                            ContextCompat.getColor(
                                App.getContext(),
                                R.color.accentGray
                            )
                        )
                        emptyText = true
                    }
                } else {
                    if (emptyText) {
                        sendButton.setTextColor(
                            ContextCompat.getColor(
                                App.getContext(),
                                R.color.accent
                            )
                        )
                        emptyText = false
                    }
                }
            }
        })


        btnAttachments = findViewById(R.id.btnAttachments) as Button
        btnAttachments?.setOnClickListener { showAttachesListDialog() }

        val btnUpload = findViewById(R.id.btnUpload) as ImageButton
        btnUpload.setOnClickListener { startAddAttachment() }

        if (mPopupPanelView == null)
            mPopupPanelView =
                PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS or PopupPanelView.VIEW_FLAG_BBCODES)
        mPopupPanelView!!.createView(
            LayoutInflater.from(context),
            findViewById(R.id.advanced_button) as ImageButton,
            txtPost
        )
        mPopupPanelView!!.activityCreated(mainActivity, view)


        try {
            val args = arguments!!
            val forumId = args.getString("forumId")!!
            val topicId = args.getString("themeId")!!
            val postId = args.getString("postId")!!
            val authKey = args.getString("authKey")!!
            parentTag = args.getString("parentTag", null)
            mEditpost = EditPost().apply {
                this.id = postId
                this.forumId = forumId
                this.topicId = topicId
                this.authKey = authKey
            }
            mPopupPanelView!!.setTopic(forumId, topicId, authKey)

            if (isNewPost) {
                if (args.getString("body") != null) {
                    txtPost!!.setText(args.getString("body"))
                    txtPost!!.setSelection(txtPost!!.text.length)
                }
            }
            layout_edit_reason?.visibility = if (isNewPost) View.GONE else View.VISIBLE
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
        return if (!TextUtils.isEmpty(txtPost!!.text)) {
            MaterialDialog.Builder(mainActivity)
                .title(R.string.confirm_action)
                .content(getString(R.string.text_not_empty))
                .positiveText(R.string.ok)
                .onPositive { _, _ -> mainActivity.tryRemoveTab(tag) }
                .negativeText(R.string.cancel)
                .show()
            true
        } else {
            false
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    private fun checkMail(): Single<Boolean> {
        if (emptyText) {
            val toast = Toast.makeText(context, R.string.enter_message, Toast.LENGTH_SHORT)
            toast.setGravity(
                Gravity.TOP,
                0,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    64f,
                    App.getInstance().resources.displayMetrics
                ).toInt()
            )
            toast.show()
            return Single.just(false)
        }
        return Single.just(true)
    }

    private fun confirmSendMail(): Single<Boolean> {
        val result = SingleSubject.create<Boolean>()
        if (Preferences.Topic.confirmSend) {
            val dialog = MaterialDialog.Builder(context!!)
                .title(R.string.is_sure)
                .content(R.string.confirm_sending)
                .positiveText(R.string.ok)
                .onPositive { _, _ -> result.onSuccess(true) }
                .negativeText(R.string.cancel)
                .show()
            dialog.setOnDismissListener {
                result.onSuccess(false)
            }
        } else {
            result.onSuccess(true)
        }
        return result
    }

    private fun sendMail() {
        val body = postText
        addToDisposable(
            TrueQueue(
                { d: Disposable? -> addToDisposable(d) },
                listOf({ checkMail() }, { confirmSendMail() })
            )
                .subscribe(
                    {
                        if (it)
                            sendPost(body, editReasonText)
                    },
                    {
                        AppLog.e(it)
                    }
                ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mEditpost != null)
            Paper.book().write("EditPost", mEditpost)
        if (mAttachfilepaths.any())
            outState.putParcelableArray("AttachFilePaths", mAttachfilepaths.toTypedArray())
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
                val uri = extras.get(Intent.EXTRA_STREAM) as? Uri?

                if (uri != null)
                    mAttachfilepaths = arrayListOf(uri)
                else
                    Toast.makeText(context, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()
            } else if (attachesObject is ArrayList<*>) {
                mAttachfilepaths = ArrayList()
                for (item in attachesObject) {
                    (item as? Uri?)?.let {
                        mAttachfilepaths.add(it)
                    }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_reason_item -> {
                toggleEditReasonDialog()
                return true
            }
            R.id.preview_item -> {
                val tabItem = TabsManager.instance.getTabByUrl("preview_" + tag!!)
                if (tabItem == null) {
                    PostPreviewFragment.showSpecial(postText, tag)
                } else {
                    (tabItem.fragment as PostPreviewFragment).load(postText)
                    mainActivity.selectTab(tabItem)
                    mainActivity.hidePopupWindows()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_post, menu)
        menu.findItem(R.id.find_in_text_item)?.setActionView(R.layout.action_collapsible_search)
        searchEditText =
            menu.findItem(R.id.find_in_text_item)?.actionView?.findViewById(R.id.editText)
        searchEditText?.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val text = if (searchEditText?.text == null) "" else searchEditText?.text.toString()
                    .trim { it <= ' ' }
                startSearch(text, true)
                searchEditText?.requestFocus()
                return@setOnKeyListener true
            }

            false
        }
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(mEdit: Editable) {
                val text = mEdit.toString().trim { it <= ' ' }
                startSearch(text, false)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.edit_reason_item)?.isVisible = !isNewPost

        if (!supportActionBar!!.isShowing) {
            supportActionBar!!.show()
            mBottompanel!!.visibility = View.VISIBLE
        }
    }

    private fun showAttachesListDialog() {
        if (mEditpost!!.attaches.size == 0) {
            MaterialDialog.Builder(mainActivity)
                .content(R.string.no_attachments)
                .positiveText(R.string.do_download)
                .negativeText(R.string.cancel)
                .onPositive { _, _ -> startAddAttachment() }
                .show()
            return
        }
        val adapter = AttachesAdapter(mEditpost!!.attaches)
        mAttachesListDialog = MaterialDialog.Builder(mainActivity)
            .cancelable(true)
            .title(R.string.attachments)
            //.setSingleChoiceItems(adapter, -1, null)
            .adapter(adapter, LinearLayoutManager(activity))
            .neutralText(R.string.in_spoiler)

            .onNeutral { _, _ ->
                val listItems = ArrayList<String>()
                var i = 0
                while (i <= mEditpost!!.attaches.size - 1) {
                    listItems.add(mEditpost!!.attaches[i].name)
                    i++
                }
                val items = listItems.toTypedArray<CharSequence>()
                val str = StringBuilder()
                MaterialDialog.Builder(context!!)
                    .title(R.string.add_in_spoiler)
                    .positiveText(R.string.add)
                    .negativeText(R.string.cancel)
                    .onPositive { _, _ ->
                        var selectionStart = txtPost!!.selectionStart
                        if (selectionStart == -1)
                            selectionStart = 0
                        if (txtPost!!.text != null)
                        //txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getTitle() + "]");
                            txtPost!!.text.insert(selectionStart, "[spoiler]$str[/spoiler]")
                    }
                    .items(*items)
                    .itemsCallbackMultiChoice(null) { _, which12, _ ->
                        str.setLength(0)
                        for (which1 in which12) {
                            str.append("[attachment=")
                                .append(mEditpost!!.attaches[which1!!].id)
                                .append(":")
                                .append(mEditpost!!.attaches[which1].name)
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
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(activity, R.string.no_permission, Toast.LENGTH_SHORT).show()
            return
        }
        val items = arrayOf<CharSequence>(getString(R.string.file), getString(R.string.image))
        MaterialDialog.Builder(context!!)
            .items(*items)
            .itemsCallback { _, _, i, _ ->
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
                        Toast.makeText(
                            mainActivity,
                            R.string.no_app_for_get_file,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (ex: Exception) {
                        AppLog.e(mainActivity, ex)
                    }

                    1// Изображение
                    ->

                        try {
                            val imageintent = Intent(
                                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                imageintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            startActivityForResult(imageintent, MY_INTENT_CLICK_I)
                        } catch (ex: ActivityNotFoundException) {
                            Toast.makeText(
                                mainActivity,
                                R.string.no_app_for_get_image_file,
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (ex: Exception) {
                            AppLog.e(mainActivity, ex)
                        }

                }
            }
            .show()
    }

    private fun helperTask(uri: Uri) {

        UpdateTask(this, mEditpost?.id ?: "", uri).execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK_I) {
                data?.data?.let {
                    uploadFile(it)
                }

            } else if (requestCode == MY_INTENT_CLICK_F) {
                data?.data?.let {
                    uploadFile(it)
                }
            }
        }
    }

    private fun uploadFile(uri: Uri) {
        val fileName = FilePath.getFileName(requireContext(), uri)
        if (fileName != null) {
            val imageExt = "jpeg|jpg|png|gif"
            val fileExt = "7z|zip|rar|tar.gz|exe|cab|xap|txt|log|mp3|mp4|apk|ipa|img|mtz"
            if (fileName.matches("(?i)(.*)($imageExt|$fileExt)$".toRegex())) {
                helperTask(uri)
            } else {
                Toast.makeText(
                    mainActivity,
                    R.string.file_not_support_forum,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else
            Toast.makeText(context, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()
    }

    override fun onLoadTaskSuccess(editPost: EditPost?) {
        setEditPost(editPost)

        if (mAttachfilepaths.any())
            UpdateTask(this, mEditpost?.id ?: "", mAttachfilepaths)
                .execute()
        mAttachfilepaths = ArrayList()
    }

    override fun onUpdateTaskSuccess(editAttach: EditAttach?) {
        mEditpost?.addAttach(editAttach)
        refreshAttachmentsInfo()
    }

    override fun onDeleteAttachTaskSuccess(attachId: String) {
        mEditpost?.deleteAttach(attachId)
        refreshAttachmentsInfo()
    }

    override fun onAcceptEditTaskSuccess(editPost: EditPost?) {
        if (TabsManager.instance.isContainsByTag(parentTag)) {
            (TabsManager.instance.getTabByTag(parentTag)?.fragment as ThemeFragment?)
                ?.showTheme(
                    ThemeFragment.getThemeUrl(
                        editPost?.topicId,
                        "view=findpost&p=${editPost?.id}"
                    ), true
                )
        }
        mainActivity.tryRemoveTab(tag)
    }

    override fun onPostTaskSuccess(editPost: EditPost?, error: String?) {

        if (!TextUtils.isEmpty(error)) {
            Toast.makeText(
                mainActivity,
                App.getContext().getString(R.string.error) + ": " + error,
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (TabsManager.instance.isContainsByTag(parentTag)) {
            (TabsManager.instance.getTabByTag(parentTag)?.fragment as? ThemeFragment?)
                ?.showTheme(
                    String.format(
                        "https://${HostHelper.host}/forum/index.php?showtopic=%s&%s",
                        editPost?.topicId,
                        if (isNewPost) "view=getlastpost" else ("view=findpost&p=" + editPost?.id)
                    ), true
                )
        }
        mainActivity.tryRemoveTab(tag)
    }

    private fun startLoadPost(
        forumId: String,
        topicId: String,
        postId: String,
        authKey: String
    ) {
        LoadTask(this, forumId, topicId, postId, authKey).execute()
    }

    private fun sendPost(text: String, editPostReason: String) {

        if (isNewPost) {
            PostTask(
                this,
                mEditpost,
                text,
                editPostReason,
                Preferences.Topic.Post.enableEmotics,
                Preferences.Topic.Post.enableSign
            )
                .execute()
        } else {
            AcceptEditTask(
                this,
                mEditpost,
                text,
                editPostReason,
                Preferences.Topic.Post.enableEmotics,
                Preferences.Topic.Post.enableSign
            )
                .execute()
        }
    }

    private fun toggleEditReasonDialog() {
        layout_edit_reason?.visibility =
            if (layout_edit_reason?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        if (mPopupPanelView != null) {
            mPopupPanelView!!.destroy()
            mPopupPanelView = null
        }
        val tabItem = TabsManager.instance.getTabByUrl("preview_" + tag!!)
        if (tabItem != null) mainActivity.tryRemoveTab(tabItem.tag)
        super.onDestroy()
    }

    private fun setEditPost(editPost: EditPost?) {
        mEditpost = editPost
        if (PostApi.NEW_POST_ID != mEditpost!!.id)
            txtPost!!.setText(mEditpost!!.body)
        txtPostEditReason?.setText(mEditpost!!.postEditReason)
        refreshAttachmentsInfo()
    }

    private fun refreshAttachmentsInfo() {
        btnAttachments?.text = (mEditpost?.attaches?.size ?: 0).toString()
    }

    inner class AttachesAdapter internal constructor(private val content: List<EditAttach>) :
        RecyclerView.Adapter<AttachesAdapter.AttachViewHolder>() {

        fun getItem(i: Int): EditAttach {
            return content[i]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachViewHolder {
            val group = LayoutInflater.from(parent.context)
                .inflate(R.layout.attachment_spinner_item, parent, false) as ViewGroup
            return AttachViewHolder(group)
        }

        override fun onBindViewHolder(holder: AttachViewHolder, position: Int) {
            val attach = content[position]
            holder.btnSpoiler.tag = attach
            holder.txtFile.text = attach.name
            holder.txtFile.tag = attach

            holder.btnDelete.setOnClickListener {
                mAttachesListDialog!!.dismiss()
                DeleteAttachTask(
                    this@EditPostFragment,
                    mEditpost?.id ?: "",
                    attach.id
                )
                    .execute()
            }

            holder.btnSpoiler.setOnClickListener {
                mAttachesListDialog!!.dismiss()

                var selectionStart = txtPost!!.selectionStart
                if (selectionStart == -1)
                    selectionStart = 0
                if (txtPost!!.text != null)
                    txtPost!!.text.insert(
                        selectionStart,
                        "[spoiler][attachment=" + attach.id + ":" + attach.name + "][/spoiler]"
                    )
            }

            holder.txtFile.setOnClickListener {
                mAttachesListDialog!!.dismiss()
                var selectionStart = txtPost!!.selectionStart
                if (selectionStart == -1)
                    selectionStart = 0
                if (txtPost!!.text != null)
                    txtPost!!.text.insert(
                        selectionStart,
                        "[attachment=" + attach.id + ":" + attach.name + "]"
                    )
            }
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getItemCount(): Int {
            return content.size
        }

        inner class AttachViewHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {
            var btnSpoiler: ImageButton = convertView.findViewById(R.id.btnSpoiler)
            var btnDelete: ImageButton = convertView.findViewById(R.id.btnDelete)
            var txtFile: TextView = convertView.findViewById(R.id.txtFile)
        }
    }

    private fun clearPostHighlight(): Spannable {
        val startSearchSelection = txtPost!!.selectionStart
        val raw = SpannableString(if (txtPost!!.text == null) "" else txtPost!!.text)
        val spans = raw.getSpans(
            0,
            raw.length,
            BackgroundColorSpan::class.java
        )

        for (span in spans) {
            raw.removeSpan(span)
        }
        txtPost!!.setSelection(startSearchSelection)
        txtPost!!.isCursorVisible = true
        return raw
    }

    fun startSearch(searchText: String, fromSelection: Boolean) {

        if (mSearchTimer != null) {
            mSearchTimer!!.cancel()
            mSearchTimer!!.purge()
        }
        mSearchTimer = Timer()
        mSearchTimer!!.schedule(object : TimerTask() {
            override fun run() {
                uiHandler.post {
                    searchEditText?.error =
                        if (search(searchText, fromSelection) == SEARCH_RESULT_NOTFOUND)
                            getString(R.string.no_matches_found)
                        else
                            null

                }
                mSearchTimer!!.cancel()
                mSearchTimer!!.purge()
            }
        }, 1000, 5000)

    }

    fun search(searchTextO: String, fromSelection: Boolean): Int {
        var searchText = searchTextO
        if (TextUtils.isEmpty(searchText)) return SEARCH_RESULT_EMPTYTEXT
        try {
            progressSearch!!.visibility = View.VISIBLE

            searchText = searchText.toLowerCase(Locale.getDefault())
            val raw = clearPostHighlight()

            var startSearchSelection = 0
            if (fromSelection)
                startSearchSelection = txtPost!!.selectionStart + 1
            val text = raw.toString().toLowerCase(Locale.getDefault())

            var findedStartSelection = TextUtils.indexOf(text, searchText, startSearchSelection)
            if (findedStartSelection == -1 && startSearchSelection != 0)
                findedStartSelection = TextUtils.indexOf(text, searchText)

            if (findedStartSelection == -1)
                return SEARCH_RESULT_NOTFOUND

            raw.setSpan(
                BackgroundColorSpan(-0x74ff75),
                findedStartSelection,
                findedStartSelection + searchText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            txtPost?.setText(raw)
            txtPost?.setSelection(findedStartSelection)
            txtPost?.isCursorVisible = true
            return SEARCH_RESULT_FOUND
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        } finally {
            if ((!fromSelection))
                searchEditText?.requestFocus()
            progressSearch?.visibility = View.GONE
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
        private const val SEARCH_RESULT_FOUND = 1
        private const val SEARCH_RESULT_NOTFOUND = 0
        private const val SEARCH_RESULT_EMPTYTEXT = -1
        val NEW_EDIT_POST_REQUEST_CODE = App.getInstance().uniqueIntValue
        const val TOPIC_BODY_KEY = "EditPostActivity.TOPIC_BODY_KEY"
        const val POST_URL_KEY = "EditPostActivity.POST_URL_KEY"

        private const val thisFragmentUrl = "EditPostFragment"

        fun newInstance(args: Bundle): EditPostFragment {
            val fragment = EditPostFragment()

            fragment.arguments = args
            return fragment
        }

        fun editPost(
            context: Activity,
            forumId: String,
            topicId: String,
            postId: String,
            authKey: String,
            tag: String?
        ) {
            val url = thisFragmentUrl + forumId + topicId + postId
            val args = Bundle()
            args.putString("forumId", forumId)
            args.putString("themeId", topicId)
            args.putString("postId", postId)
            args.putString("authKey", authKey)
            args.putString("parentTag", tag)
            MainActivity.addTab(
                context.getString(R.string.edit_post_combined) + context.getString(R.string.combined_in) + TabsManager.instance.getTabByTag(
                    tag
                )!!.title, url, newInstance(args)
            )
        }

        fun newPost(
            context: Activity, forumId: String, topicId: String, authKey: String,
            body: String, tag: String
        ) {
            val url = thisFragmentUrl + forumId + topicId + PostApi.NEW_POST_ID
            val args = Bundle()
            args.putString("forumId", forumId)
            args.putString("themeId", topicId)
            args.putString("postId", PostApi.NEW_POST_ID)
            args.putString("body", body)
            args.putString("authKey", authKey)
            args.putString("parentTag", tag)
            MainActivity.addTab(
                context.getString(R.string.answer) + context.getString(R.string.combined_in) + TabsManager.instance.getTabByTag(
                    tag
                )!!.title, url, newInstance(args)
            )
        }

        fun newPostWithAttach(
            context: Context, forumId: String?, topicId: String, authKey: String,
            extras: Bundle
        ) {
            val url = thisFragmentUrl + forumId + topicId + PostApi.NEW_POST_ID
            val args = Bundle()
            args.putString("forumId", forumId)
            args.putString("themeId", topicId)
            args.putString("postId", PostApi.NEW_POST_ID)
            args.putBundle("extras", extras)
            args.putString("authKey", authKey)
            MainActivity.addTab(
                context.getString(R.string.edit_post_combined),
                url,
                newInstance(args)
            )
        }

        private const val MY_INTENT_CLICK_I = 302
        private const val MY_INTENT_CLICK_F = 303
    }
}