package org.softeg.slartus.forpdaplus.fragments.qms

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.qms_chat.*
import org.softeg.slartus.forpdaapi.post.EditAttach
import org.softeg.slartus.forpdaapi.qms.QmsApi
import org.softeg.slartus.forpdacommon.ExtPreferences
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier
import org.softeg.slartus.forpdaplus.*
import org.softeg.slartus.forpdaplus.classes.*
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView
import org.softeg.slartus.forpdaplus.emotic.Smiles
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import org.softeg.slartus.forpdaplus.fragments.qms.tasks.*
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences
import org.softeg.slartus.forpdaplus.prefs.Preferences
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/*
 * Created by radiationx on 12.11.15.
 */
class QmsChatFragment : WebViewFragment() {
    private var emptyText = true
    private val uiHandler = Handler()
    private val mHandler = Handler()
    private var wvChat: AdvWebView? = null
    private var contactId: String? = null
    private var themeId: String? = null
    private var contactNick: String? = ""
    private var themeTitle: String? = ""
    private var lastBodyLength: Long = 0
    private var edMessage: EditText? = null
    private var updateTimeout: Long = 15000
    private var updateTimer = Timer()
    private var htmlPreferences: HtmlPreferences? = null
    private var mPopupPanelView: PopupPanelView? = null
    private var messageText: String? = null
    private var sendTask: AsyncTask<ArrayList<String>, Void, Boolean>? = null
    private var btnAttachments: Button? = null

    private var mMode: ActionMode? = null
    private var deleteMode: Boolean? = false
    private var daysCount: Int? = DAYS_PART_COUNT

    //Upload file to savepic.ru
    private val attachList = ArrayList<EditAttach>()

    override fun hidePopupWindows() {
        super.hidePopupWindows()
        mPopupPanelView!!.hidePopupWindow()
    }

    override fun getWebViewClient(): WebViewClient {
        return MyWebViewClient()
    }

    override fun getTitle(): String? {
        return themeTitle
    }

    override fun getUrl(): String {
        return ""
    }

    override fun reload() {
        Thread(Runnable { this.reLoadChatSafe() }).start()
    }

    fun reload(loadMore: Boolean = false) {
        Thread(Runnable { this.reLoadChatSafe(loadMore) }).start()
    }

    override fun getAsyncTask(): AsyncTask<*, *, *>? {
        return sendTask
    }

    override fun closeTab(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = arguments ?: savedInstanceState

        contactId = extras?.getString(MID_KEY)
        contactNick = extras?.getString(NICK_KEY)
        themeId = extras?.getString(TID_KEY)
        themeTitle = extras?.getString(THEME_TITLE_KEY)
        title = if (TextUtils.isEmpty(contactNick)) "QMS" else themeTitle
        if (extras?.containsKey(KEY_DAYSCOUNT) == true)
            daysCount = extras.getInt(KEY_DAYSCOUNT)
        if (supportActionBar != null)
            setSubtitle(contactNick)
    }

    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.qms_chat, container, false)
        assert(view != null)

        htmlPreferences = HtmlPreferences()
        htmlPreferences!!.load(context)

        edMessage = findViewById(R.id.edMessage) as EditText
        if (mPopupPanelView == null)
            mPopupPanelView =
                PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS or PopupPanelView.VIEW_FLAG_BBCODES)
        mPopupPanelView!!.createView(
            LayoutInflater.from(context),
            findViewById(R.id.advanced_button) as ImageButton,
            edMessage
        )
        mPopupPanelView!!.activityCreated(mainActivity, view)

        btnSend?.setOnClickListener { startSendMessage() }
        edMessage!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    if (!emptyText) {
                        btnSend?.clearColorFilter()
                        emptyText = true
                    }
                } else {
                    if (emptyText) {
                        btnSend?.setColorFilter(
                            ContextCompat.getColor(
                                App.getContext(),
                                R.color.selectedItemText
                            ), PorterDuff.Mode.SRC_ATOP
                        )
                        emptyText = false
                    }
                }
            }
        })

        wvChat = findViewById(R.id.wvChat) as AdvWebView
        registerForContextMenu(wvChat!!)
        wvChat?.apply {
            settings.domStorageEnabled = true
            settings.setAppCachePath(mainActivity.applicationContext.cacheDir.absolutePath)
            settings.setAppCacheEnabled(true)
            settings.allowFileAccess = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.defaultFontSize = Preferences.Topic.fontSize
        }
        wvChat?.addJavascriptInterface(this, "HTMLOUT")

        WebViewExternals(this).apply {
            loadPreferences(App.getInstance().preferences)
            setWebViewSettings(true)
        }
        wvChat?.webViewClient = MyWebViewClient()

        val extras = arguments ?: savedInstanceState
        val pageBody = extras?.getString(PAGE_BODY_KEY, "") ?: ""

        if (!TextUtils.isEmpty(pageBody)) {
            lastBodyLength = pageBody.length.toLong()
            Thread {
                val body = transformChatBody(pageBody)

                mHandler.post {
                    wvChat?.loadDataWithBaseURL(
                        "https://" + App.Host + "/forum/",
                        body,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }.start()
        }
        hideKeyboard()

        btnAttachments = findViewById(R.id.btnAttachments) as Button
        btnAttachments?.setOnClickListener { showAttachesListDialog() }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.stopQmsService()
        clearNotification(1)

        loadPrefs()
        startUpdateTimer()

    }

    @Suppress("unused")
    @JavascriptInterface
    fun showChooseCssDialog() {
        mainActivity.runOnUiThread {
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "file/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, FILECHOOSER_RESULTCODE)

            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(mainActivity, R.string.no_app_for_get_file, Toast.LENGTH_LONG).show()
            } catch (ex: Exception) {
                AppLog.e(mainActivity, ex)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK) {
                val uri = data?.data
                if (uri == null) return
                val fileName = FilePath.getFileName(App.getInstance(), uri)
                if (fileName != null) {
                    if (fileName.matches("(?i)(.*)(7z|zip|rar|tar.gz|exe|cab|xap|txt|log|jpeg|jpg|png|gif|mp3|mp4|apk|ipa|img|.mtz)$".toRegex())) {
                        AttachesTask(this, uri).execute()
                    } else {
                        Toast.makeText(
                            activity,
                            R.string.file_not_support_forum,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else
                    Toast.makeText(activity, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()

            } else if (requestCode == FILECHOOSER_RESULTCODE) {
                val attachFilePath = org.softeg.slartus.forpdacommon.FileUtils.getRealPathFromURI(
                    context,
                    data!!.data!!
                )
                val cssData = org.softeg.slartus.forpdacommon.FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n")
                    .replace("\r", "")
                if (Build.VERSION.SDK_INT < 19)
                    wvChat?.loadUrl("javascript:window['HtmlInParseLessContent']('$cssData');")
                else
                    wvChat?.evaluateJavascript(
                        "window['HtmlInParseLessContent']('$cssData')"
                    ) {

                    }
            }
        }

    }

    private fun hideKeyboard() {
        val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edMessage!!.windowToken, 0)
    }

    @Suppress("unused")
    @JavascriptInterface
    fun showMessage(message: String) {
        mainActivity.runOnUiThread {
            Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun deleteMessages(checkBoxNames: Array<String>?) {
        mainActivity.runOnUiThread {
            if (checkBoxNames == null) {
                Toast.makeText(mainActivity, R.string.no_messages_for_delete, Toast.LENGTH_LONG)
                    .show()
                return@runOnUiThread
            }

            val ids = ArrayList<String>()
            val p = Pattern.compile("message-id\\[(\\d+)\\]", Pattern.CASE_INSENSITIVE)
            for (checkBoxName in checkBoxNames) {
                val m = p.matcher(checkBoxName)
                if (m.find()) {
                    ids.add(m.group(1))
                }
            }
            if (ids.size == 0) {
                Toast.makeText(mainActivity, R.string.no_messages_for_delete, Toast.LENGTH_LONG)
                    .show()
                return@runOnUiThread
            }

            MaterialDialog.Builder(mainActivity)
                .title(R.string.confirm_action)
                .cancelable(true)
                .content(
                    String.format(
                        App.getContext().getString(R.string.ask_delete_messages),
                        ids.size
                    )
                )
                .positiveText(R.string.delete)
                .onPositive { _, _ ->
                    sendTask = DeleteTask(
                        this, contactId ?: "",
                        themeId ?: "", ids, daysCount
                    )
                    sendTask?.execute()
                }
                .negativeText(R.string.cancel)
                .show()
        }
    }

    @JavascriptInterface
    fun loadMore() {
        mainActivity.runOnUiThread {
            daysCount = (daysCount ?: 0) + DAYS_PART_COUNT
            reload(true)
        }
    }

    @JavascriptInterface
    fun startDeleteModeJs(count: String) {
        mainActivity.runOnUiThread { startDeleteMode(count) }
    }

    @JavascriptInterface
    fun stopDeleteModeJs() {
        if (deleteMode != true)
            return
        mainActivity.runOnUiThread { stopDeleteMode(true) }
    }

    fun onAttachDeleted(attachId: String) {
        val attach = attachList.firstOrNull { it.id == attachId }
        if (attach != null) {
            attachList.remove(attach)
            refreshAttachmentsInfo()
        }
    }

    private inner class AnActionModeOfEpicProportions : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.add(R.string.delete)
                .setIcon(R.drawable.delete)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            webView!!.loadUrl("javascript:deleteMessages('thread_form');")
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            stopDeleteMode(false)
        }
    }

    private fun startDeleteMode(count: String) {
        if (deleteMode != true)
            mMode = mainActivity.startActionMode(AnActionModeOfEpicProportions())
        if (mMode != null)
            mMode!!.title = "Сообщений:$count"

        deleteMode = true
    }

    fun stopDeleteMode(finishActionMode: Boolean?) {
        if (finishActionMode!! && mMode != null)
            mMode!!.finish()
        deleteMode = false
    }

    private fun deleteDialog() {
        themeId?.let {
            MaterialDialog.Builder(mainActivity)
                .title(R.string.confirm_action)
                .cancelable(true)
                .content(R.string.ask_delete_dialog)
                .positiveText(R.string.delete)
                .onPositive { _, _ ->
                    val ids = ArrayList<String>()
                    ids.add(it)
                    sendTask = DeleteDialogTask(this, contactId ?: "", ids)
                    sendTask!!.execute()
                }
                .negativeText(R.string.cancel)
                .show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(MID_KEY, contactId)
        outState.putString(NICK_KEY, contactNick)
        outState.putString(TID_KEY, themeId)
        outState.putString(THEME_TITLE_KEY, themeTitle)
        outState.putString(POST_TEXT_KEY, edMessage!!.text.toString())
        daysCount?.let {
            outState.putInt(KEY_DAYSCOUNT, it)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPrefs()
        startUpdateTimer()
        if (mPopupPanelView != null)
            mPopupPanelView!!.resume()
    }

    private fun startAdaptiveTimeOutService() {
        if (!QmsNotifier.isUse(context))
            return

        App.reStartQmsService(true)
    }

    override fun onPause() {
        super.onPause()

        startAdaptiveTimeOutService()
        clearNotifTimer()
        updateTimer.cancel()
        updateTimer.purge()
        if (mPopupPanelView != null)
            mPopupPanelView!!.pause()
    }

    override fun onStop() {
        super.onStop()
        updateTimer.cancel()
        updateTimer.purge()
    }

    override fun onDestroy() {
        updateTimer.cancel()
        updateTimer.purge()
        if (mPopupPanelView != null) {
            mPopupPanelView!!.destroy()
            mPopupPanelView = null
        }
        super.onDestroy()

    }

    private fun clearNotifTimer() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        val editor = preferences.edit()
        editor.putFloat("qms.service.timeout.restart", 1f)
        editor.apply()
    }

    private fun loadPrefs() {
        updateTimeout = (ExtPreferences.parseInt(
            App.getInstance().preferences,
            "qms.chat.update_timer",
            15
        ) * 1000).toLong()
    }

    private fun checkNewQms() {
        try {
            Client.getInstance().setQmsCount(QmsApi.getNewQmsCount(Client.getInstance()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun transformChatBody(chatBody: String, loadMore: Boolean = false): String {

        checkNewQms()
        if ((themeTitle == null) or (contactNick == null)) {
            val m = Pattern.compile("<span id=\"chatInfo\"[^>]*>([^>]*?)\\|:\\|([^<]*)</span>")
                .matcher(chatBody)
            if (m.find()) {
                contactNick = m.group(1)
                themeTitle = m.group(2)
            }
        }
        val htmlBuilder = QmsHtmlBuilder()
        htmlBuilder.buildBody(loadMore, chatBody, htmlPreferences)

        return htmlBuilder.html.toString()
    }

    private fun reLoadChatSafe(loadMore: Boolean = false) {
        uiHandler.post { setSubtitle(App.getContext().getString(R.string.refreshing)) }

        var chatBody: String? = null
        var ex: Throwable? = null
        try {
            val body: String

            val qmsPage = QmsApi.getChat(Client.getInstance(), contactId!!, themeId!!, daysCount)
            body = qmsPage.body ?: ""
            if (!qmsPage.userNick.isNullOrEmpty())
                contactNick = qmsPage.userNick?.toString() ?: contactNick
            if (!qmsPage.title.isNullOrEmpty())
                themeTitle = qmsPage.title?.toString() ?: themeTitle

            if (body.length.toLong() == lastBodyLength) {
                checkNewQms()
                uiHandler.post {
                    //                        setLoading(false);
                    setSubtitle("")
                }
                return
            }
            lastBodyLength = body.length.toLong()
            chatBody = transformChatBody(body, loadMore)
        } catch (e: Throwable) {
            ex = e
        }

        val finalEx = ex
        val finalChatBody = chatBody

        uiHandler.post {
            if (finalEx == null) {
                title = themeTitle
                setSubtitle(contactNick)
                wvChat?.loadDataWithBaseURL(
                    "https://" + App.Host + "/forum/",
                    finalChatBody ?: "",
                    "text/html",
                    "UTF-8",
                    null
                )
            } else {
                if ("Такого диалога не существует." == finalEx.message) {
                    MaterialDialog.Builder(mainActivity)
                        .title(R.string.error)
                        .content(finalEx.message ?: "неизвестная ошибка")
                        .positiveText(R.string.ok)
                        .show()
                    updateTimer.cancel()
                    updateTimer.purge()

                } else {
                    Toast.makeText(
                        mainActivity, AppLog.getLocalizedMessage(finalEx, finalEx.localizedMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
            //                setLoading(false);
            setSubtitle("")
        }

    }

    fun clearAttaches() {
        attachList.clear()
        refreshAttachmentsInfo()
    }

    fun onPostChat(chatBody: String, success: Boolean, ex: Throwable?) {
        if (success) {
            edMessage!!.text.clear()

            wvChat!!.loadDataWithBaseURL(
                "https://" + App.Host + "/forum/",
                chatBody,
                "text/html",
                "UTF-8",
                null
            )
        } else {
            if (ex != null)
                AppLog.e(mainActivity, ex) {
                    sendTask = SendTask(
                        this, contactId ?: "", themeId ?: "", messageText
                            ?: "", attachList, daysCount
                    )
                    sendTask!!.execute()
                }
            else
                Toast.makeText(
                    mainActivity, R.string.unknown_error,
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun startUpdateTimer() {
        updateTimer.cancel()
        updateTimer.purge()
        updateTimer = Timer()
        updateTimer.schedule(object : TimerTask() { // Определяем задачу
            override fun run() {
                try {
                    if (sendTask != null && sendTask!!.status != AsyncTask.Status.FINISHED)
                        return
                    reLoadChatSafe()
                } catch (ex: Throwable) {
                    AppLog.e(mainActivity, ex)
                }

            }
        }, 0L, updateTimeout)

    }

    private fun startSendMessage() {
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
            return
        }
        messageText = edMessage!!.text.toString()
        sendTask = SendTask(
            this, contactId ?: "", themeId ?: "",
            messageText ?: "", attachList, daysCount
        )
        sendTask?.execute()
    }

    override fun Prefix(): String? {
        return "theme"
    }

    @JavascriptInterface
    override fun saveHtml(html: String) {
        mainActivity.runOnUiThread { SaveHtml(mainActivity, html, "qms") }
    }

    override fun getWebView(): AdvWebView? {
        return wvChat
    }

    private fun showCompanionProfile() {
        //ProfileWebViewActivity.startActivity(this, contactId, contactNick);
        ProfileFragment.showProfile(contactId, contactNick)
    }

    override fun showLinkMenu(link: String) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
            || link == "#"
            || link.startsWith("file:///")
        )
            return
        ExtUrl.showSelectActionDialog(
            mHandler,
            context!!,
            themeTitle,
            "",
            link,
            "",
            "",
            "",
            contactId,
            contactNick
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.refresh_item -> {
                reload()
                return true
            }
            R.id.setting_item -> {
                val intent = Intent(mainActivity, QmsChatPreferencesActivity::class.java)
                mainActivity.startActivity(intent)
                return true
            }
            R.id.delete_dialog_item -> {
                deleteDialog()
                return true
            }
            R.id.font_size_item -> {
                showFontSizeDialog()
                return true
            }
            R.id.profile_interlocutor_item -> {
                showCompanionProfile()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.qms_chat, menu)
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            Preferences.Notifications.Qms.readQmsDone()
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            IntentActivity.tryShowUrl(mainActivity, mHandler, url, true, false, "")

            return true
        }
    }

    private fun showAttachesListDialog() {
        if (attachList.size == 0) {
            startAddAttachment()
            return
        }
        val listItems = ArrayList<String>()
        for (attach in attachList)
            listItems.add(attach.name)
        val items = listItems.toTypedArray<CharSequence>()
        MaterialDialog.Builder(mainActivity)
            .cancelable(true)
            .title(R.string.attachments)
            .items(*items)
            .itemsCallback { _, _, which, _ ->
                val item = attachList[which]
                MaterialDialog.Builder(mainActivity)
                    .title(R.string.ConfirmTheAction)
                    .cancelable(true)
                    .content(R.string.SureDeleteFile)
                    .positiveText(R.string.delete)
                    .onPositive { _, _ ->
                        DeleteAttachTask(
                            this@QmsChatFragment,
                            item.id
                        ).execute()
                    }
                    .negativeText(R.string.cancel)
                    .show()
            }
            .positiveText(R.string.do_download)
            .onPositive { _, _ -> startAddAttachment() }
            .negativeText(R.string.ok)
            .show()
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
        try {

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            startActivityForResult(intent, MY_INTENT_CLICK)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(mainActivity, R.string.no_app_for_get_image_file, Toast.LENGTH_LONG)
                .show()
        } catch (ex: Exception) {
            AppLog.e(mainActivity, ex)
        }

    }

    fun addAttachesToList(attaches: List<EditAttach>) {
        attachList.addAll(attaches)
        refreshAttachmentsInfo()
    }

    private fun refreshAttachmentsInfo() {
        btnAttachments?.text = attachList.size.toString()
    }

    companion object {
        private const val KEY_DAYSCOUNT = "KEY_DAYSCOUNT"
        private const val MID_KEY = "mid"
        private const val TID_KEY = "tid"
        private const val THEME_TITLE_KEY = "theme_title"
        private const val NICK_KEY = "contactNick"
        private const val PAGE_BODY_KEY = "page_body"
        private const val POST_TEXT_KEY = "PostText"
        private const val FILECHOOSER_RESULTCODE = 1
        private const val DAYS_PART_COUNT = 7
        fun openChat(
            userId: String,
            userNick: String?,
            tid: String,
            themeTitle: String?,
            pageBody: String?
        ) {
            MainActivity.addTab(
                themeTitle,
                themeTitle + userId,
                newInstance(userId, userNick, tid, themeTitle, pageBody)
            )
        }

        fun openChat(userId: String, userNick: String?, tid: String, themeTitle: String?) {
            MainActivity.addTab(
                themeTitle,
                themeTitle + userId,
                newInstance(userId, userNick, tid, themeTitle)
            )
        }

        fun newInstance(
            userId: String,
            userNick: String?,
            tid: String,
            themeTitle: String?,
            pageBody: String?
        ): QmsChatFragment {
            val args = Bundle()
            args.putString(MID_KEY, userId)
            args.putString(NICK_KEY, userNick ?: userId)
            args.putString(TID_KEY, tid)
            args.putString(THEME_TITLE_KEY, themeTitle ?: "")
            args.putString(PAGE_BODY_KEY, pageBody ?: "")

            val fragment = QmsChatFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(
            userId: String,
            userNick: String?,
            tid: String,
            themeTitle: String?
        ): QmsChatFragment {
            val args = Bundle()
            args.putString(MID_KEY, userId)
            args.putString(NICK_KEY, userNick ?: userId)
            args.putString(TID_KEY, tid)
            args.putString(THEME_TITLE_KEY, themeTitle ?: "")

            val fragment = QmsChatFragment()
            fragment.arguments = args
            return fragment
        }

        val encoding: String
            get() {
                val prefs = App.getInstance().preferences

                return prefs?.getString("qms.chat.encoding", "UTF-8") ?: "UTF-8"

            }
        private const val MY_INTENT_CLICK = 302
    }

}
