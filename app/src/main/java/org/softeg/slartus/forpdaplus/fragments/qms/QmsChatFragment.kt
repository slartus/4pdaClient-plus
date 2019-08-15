package org.softeg.slartus.forpdaplus.fragments.qms

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
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
import org.softeg.slartus.forpdaapi.ProgressState
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
import org.softeg.slartus.forpdaplus.fragments.qms.tasks.DeleteAttachTask
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
    internal val uiHandler = Handler()
    private val mHandler = Handler()
    private var wvChat: AdvWebView? = null
    private var m_Id: String? = null
    private var m_TId: String? = null
    private var m_Nick: String? = ""
    private var m_ThemeTitle: String? = ""
    private var m_LastBodyLength: Long = 0
    private var edMessage: EditText? = null
    private var m_UpdateTimeout: Long = 15000
    private var m_UpdateTimer = Timer()
    private var m_HtmlPreferences: HtmlPreferences? = null
    private var mPopupPanelView: PopupPanelView? = null
    private var m_MessageText: String? = null
    private var m_SendTask: AsyncTask<ArrayList<String>, Void, Boolean>? = null
    private var btnAttachments: Button? = null

    internal var mMode: ActionMode? = null
    private var DeleteMode: Boolean? = false


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
        return m_ThemeTitle
    }

    override fun getUrl(): String {
        return ""
    }

    override fun reload() {
        Thread(Runnable { this.reLoadChatSafe() }).start()

    }

    override fun getAsyncTask(): AsyncTask<*, *, *>? {
        return m_SendTask
    }

    override fun closeTab(): Boolean {
        return false
    }

    @SuppressLint("AddJavascriptInterface")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.qms_chat, container, false)
        assert(view != null)

        m_HtmlPreferences = HtmlPreferences()
        m_HtmlPreferences!!.load(context)

        edMessage = findViewById(R.id.edMessage) as EditText
        if (mPopupPanelView == null)
            mPopupPanelView = PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS or PopupPanelView.VIEW_FLAG_BBCODES)
        mPopupPanelView!!.createView(LayoutInflater.from(context), findViewById(R.id.advanced_button) as ImageButton, edMessage)
        mPopupPanelView!!.activityCreated(mainActivity, view)

        val send_button = findViewById(R.id.btnSend) as ImageButton

        send_button.setOnClickListener { view12 -> startSendMessage() }
        edMessage!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    if (!emptyText) {
                        send_button.clearColorFilter()
                        emptyText = true
                    }
                } else {
                    if (emptyText) {
                        send_button.setColorFilter(ContextCompat.getColor(App.getContext(), R.color.selectedItemText), PorterDuff.Mode.SRC_ATOP)
                        emptyText = false
                    }
                }
            }
        })


        wvChat = findViewById(R.id.wvChat) as AdvWebView
        registerForContextMenu(wvChat!!)


        wvChat!!.settings.domStorageEnabled = true
        wvChat!!.settings.setAppCacheMaxSize((1024 * 1024 * 8).toLong())
        val appCachePath = mainActivity.applicationContext.cacheDir.absolutePath
        wvChat!!.settings.setAppCachePath(appCachePath)
        wvChat!!.settings.setAppCacheEnabled(true)

        wvChat!!.settings.allowFileAccess = true

        wvChat!!.settings.cacheMode = WebSettings.LOAD_DEFAULT

        wvChat!!.addJavascriptInterface(this, "HTMLOUT")
        wvChat!!.settings.defaultFontSize = Preferences.Topic.getFontSize()
        val m_WebViewExternals = WebViewExternals(this)
        m_WebViewExternals.loadPreferences(App.getInstance().preferences)

        m_WebViewExternals.setWebViewSettings(true)

        wvChat!!.webViewClient = MyWebViewClient()
        val extras = arguments!!

        m_Id = extras.getString(MID_KEY)
        m_Nick = extras.getString(NICK_KEY)
        m_TId = extras.getString(TID_KEY)
        m_ThemeTitle = extras.getString(THEME_TITLE_KEY)


        val m_PageBody = arrayOf(extras.getString(PAGE_BODY_KEY))
        if (TextUtils.isEmpty(m_Nick))
            title = "QMS"
        else
            title = m_ThemeTitle
        if (supportActionBar != null)
            setSubtitle(m_Nick)
        if (!TextUtils.isEmpty(m_PageBody[0])) {
            m_LastBodyLength = m_PageBody[0].length.toLong()
            Thread {
                val body = transformChatBody(m_PageBody[0])

                mHandler.post { wvChat!!.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null) }
            }.start()


        }
        hideKeyboard()

        btnAttachments = findViewById(R.id.btnAttachments) as Button
        btnAttachments!!.setOnClickListener { view1 -> showAttachesListDialog() }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK) {

                if (null == data) return
                val path = FilePath.getPath(App.getInstance(), data.data)
                if (path != null) {
                    if (path.matches("(?i)(.*)(7z|zip|rar|tar.gz|exe|cab|xap|txt|log|jpeg|jpg|png|gif|mp3|mp4|apk|ipa|img|.mtz)$".toRegex())) {
                        UpdateTask(mainActivity, path).execute()
                    } else {
                        Toast.makeText(activity, R.string.file_not_support_forum, Toast.LENGTH_SHORT).show()
                    }
                } else
                    Toast.makeText(activity, "Не могу прикрепить файл", Toast.LENGTH_SHORT).show()

            } else if (requestCode == FILECHOOSER_RESULTCODE) {
                val attachFilePath = org.softeg.slartus.forpdacommon.FileUtils.getRealPathFromURI(context, data!!.data!!)
                val cssData = org.softeg.slartus.forpdacommon.FileUtils.readFileText(attachFilePath)
                        .replace("\\", "\\\\")
                        .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
                if (Build.VERSION.SDK_INT < 19)
                    wvChat!!.loadUrl("javascript:window['HtmlInParseLessContent']('$cssData');")
                else
                    wvChat!!.evaluateJavascript("window['HtmlInParseLessContent']('$cssData')"
                    ) { s ->

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
        mainActivity.runOnUiThread { Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show() }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun deleteMessages(checkBoxNames: Array<String>?) {
        mainActivity.runOnUiThread {
            if (checkBoxNames == null) {
                Toast.makeText(mainActivity, R.string.no_messages_for_delete, Toast.LENGTH_LONG).show()
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
                Toast.makeText(mainActivity, R.string.no_messages_for_delete, Toast.LENGTH_LONG).show()
                return@runOnUiThread
            }

            MaterialDialog.Builder(mainActivity)
                    .title(R.string.confirm_action)
                    .cancelable(true)
                    .content(String.format(App.getContext().getString(R.string.ask_delete_messages), ids.size))
                    .positiveText(R.string.delete)
                    .onPositive { dialog, which ->
                        m_SendTask = DeleteTask(mainActivity)
                        m_SendTask!!.execute(ids)
                    }
                    .negativeText(R.string.cancel)
                    .show()
        }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun startDeleteModeJs(count: String) {
        mainActivity.runOnUiThread { startDeleteMode(count) }

    }

    @Suppress("unused")
    @JavascriptInterface
    fun stopDeleteModeJs() {
        if (DeleteMode != true)
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
        if (DeleteMode != true)
            mMode = mainActivity.startActionMode(AnActionModeOfEpicProportions())
        if (mMode != null)
            mMode!!.title = "Сообщений:$count"

        DeleteMode = true
    }

    private fun stopDeleteMode(finishActionMode: Boolean?) {
        if (finishActionMode!! && mMode != null)
            mMode!!.finish()
        DeleteMode = false
    }

    fun deleteDialog() {

        m_TId?.let {
            MaterialDialog.Builder(mainActivity)
                    .title(R.string.confirm_action)
                    .cancelable(true)
                    .content(R.string.ask_delete_dialog)
                    .positiveText(R.string.delete)
                    .onPositive { dialog, which ->
                        val ids = ArrayList<String>()
                        ids.add(it)
                        m_SendTask = DeleteDialogTask(mainActivity, ids)
                        m_SendTask!!.execute()
                    }
                    .negativeText(R.string.cancel)
                    .show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(MID_KEY, m_Id)
        outState.putString(NICK_KEY, m_Nick)
        outState.putString(TID_KEY, m_TId)
        outState.putString(THEME_TITLE_KEY, m_ThemeTitle)
        outState.putString(POST_TEXT_KEY, edMessage!!.text.toString())
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
        m_UpdateTimer.cancel()
        m_UpdateTimer.purge()
        if (mPopupPanelView != null)
            mPopupPanelView!!.pause()
    }

    override fun onStop() {
        super.onStop()
        m_UpdateTimer.cancel()
        m_UpdateTimer.purge()
    }

    override fun onDestroy() {
        m_UpdateTimer.cancel()
        m_UpdateTimer.purge()
        if (mPopupPanelView != null) {
            mPopupPanelView!!.destroy()
            mPopupPanelView = null
        }
        super.onDestroy()

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return true
    }

    private fun clearNotifTimer() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        val editor = preferences.edit()
        editor.putFloat("qms.service.timeout.restart", 1f)
        editor.apply()
    }

    private fun loadPrefs() {
        m_UpdateTimeout = (ExtPreferences.parseInt(App.getInstance().preferences, "qms.chat.update_timer", 15) * 1000).toLong()
    }

    private fun checkNewQms() {
        try {
            Client.getInstance().setQmsCount(QmsApi.getNewQmsCount(Client.getInstance()))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun transformChatBody(chatBody: String): String {
        var chatBody = chatBody
        checkNewQms()
        if ((m_ThemeTitle == null) or (m_Nick == null)) {
            val m = Pattern.compile("<span id=\"chatInfo\"[^>]*>([^>]*?)\\|:\\|([^<]*)</span>").matcher(chatBody)
            if (m.find()) {
                m_Nick = m.group(1)
                m_ThemeTitle = m.group(2)
            }
        }
        val htmlBuilder = HtmlBuilder()
        htmlBuilder.beginHtml("QMS")
        htmlBuilder.beginBody("qms", "", Preferences.Topic.isShowAvatars())
        //        htmlBuilder.beginBody("qms", "onload=\"scrollToElement('bottom_element')\"", Preferences.Topic.isShowAvatars());

        if (!Preferences.Topic.isShowAvatars())
            chatBody = chatBody.replace("<img[^>]*?class=\"avatar\"[^>]*>".toRegex(), "")
        if (m_HtmlPreferences!!.isSpoilerByButton!!)
            chatBody = HtmlPreferences.modifySpoiler(chatBody)
        chatBody = HtmlPreferences.modifyBody(chatBody, Smiles.getSmilesDict())
        chatBody = chatBody.replace("(<a[^>]*?href=\"([^\"]*?savepice[^\"]*-)[\\w]*(\\.[^\"]*)\"[^>]*?>)[^<]*?(</a>)".toRegex(), "$1<img src=\"$2prev$3\">$4")
        htmlBuilder.append(chatBody)
        htmlBuilder.append("<div id=\"bottom_element\" name=\"bottom_element\"></div>")
        htmlBuilder.endBody()
        htmlBuilder.endHtml()

        return htmlBuilder.html.toString()
    }

    private fun reLoadChatSafe() {
        uiHandler.post { setSubtitle(App.getContext().getString(R.string.refreshing)) }

        var chatBody: String? = null
        var ex: Throwable? = null
        var updateTitle = false
        try {
            val body: String

            if (TextUtils.isEmpty(m_Nick)) {
                updateTitle = true
                val additionalHeaders = HashMap<String, String>()
                body = QmsApi.getChat(Client.getInstance(), m_Id!!, m_TId!!, additionalHeaders)
                if (additionalHeaders.containsKey("Nick"))
                    m_Nick = additionalHeaders["Nick"]
                if (additionalHeaders.containsKey("ThemeTitle"))
                    m_ThemeTitle = additionalHeaders["ThemeTitle"]
            } else {
                body = QmsApi.getChat(Client.getInstance(), m_Id!!, m_TId!!)
            }
            if (body.length.toLong() == m_LastBodyLength) {
                checkNewQms()
                uiHandler.post {
                    //                        setLoading(false);
                    setSubtitle("")
                }
                return
            }
            m_LastBodyLength = body.length.toLong()
            chatBody = transformChatBody(body)
        } catch (e: Throwable) {
            ex = e
        }

        val finalEx = ex
        val finalChatBody = chatBody
        val finalUpdateTitle = updateTitle
        uiHandler.post {
            if (finalEx == null) {
                if (finalUpdateTitle)
                    setTitle(m_ThemeTitle)
                setSubtitle(m_Nick)
                wvChat!!.loadDataWithBaseURL("http://4pda.ru/forum/", finalChatBody, "text/html", "UTF-8", null)
            } else {
                if ("Такого диалога не существует." == finalEx.message) {
                    MaterialDialog.Builder(mainActivity)
                            .title(R.string.error)
                            .content(finalEx.message ?: "неизвестная ошибка")
                            .positiveText(R.string.ok)
                            .show()
                    m_UpdateTimer.cancel()
                    m_UpdateTimer.purge()

                } else {
                    Toast.makeText(mainActivity, AppLog.getLocalizedMessage(finalEx, finalEx.localizedMessage),
                            Toast.LENGTH_SHORT).show()
                }

            }
            //                setLoading(false);
            setSubtitle("")
        }

    }

    private fun onPostChat(chatBody: String, success: Boolean, ex: Throwable?) {
        if (success) {
            edMessage!!.text.clear()

            wvChat!!.loadDataWithBaseURL("http://4pda.ru/forum/", chatBody, "text/html", "UTF-8", null)
        } else {
            if (ex != null)
                AppLog.e(mainActivity, ex) {
                    m_SendTask = SendTask(mainActivity)
                    m_SendTask!!.execute()
                }
            else
                Toast.makeText(mainActivity, R.string.unknown_error,
                        Toast.LENGTH_SHORT).show()
        }
    }

    private fun startUpdateTimer() {
        m_UpdateTimer.cancel()
        m_UpdateTimer.purge()
        m_UpdateTimer = Timer()
        m_UpdateTimer.schedule(object : TimerTask() { // Определяем задачу
            override fun run() {
                try {
                    if (m_SendTask != null && m_SendTask!!.status != AsyncTask.Status.FINISHED)
                        return
                    reLoadChatSafe()
                } catch (ex: Throwable) {
                    AppLog.e(mainActivity, ex)
                }

            }
        }, 0L, m_UpdateTimeout)

    }

    private fun startSendMessage() {
        if (emptyText) {
            val toast = Toast.makeText(context, R.string.EnterMessage_, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, App.getInstance().resources.displayMetrics).toInt())
            toast.show()
            return
        }
        m_MessageText = edMessage!!.text.toString()
        m_SendTask = SendTask(mainActivity)
        m_SendTask!!.execute()
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
        //ProfileWebViewActivity.startActivity(this, m_Id, m_Nick);
        ProfileFragment.showProfile(m_Id, m_Nick)
    }

    override fun showLinkMenu(link: String) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link == "#"
                || link.startsWith("file:///"))
            return
        ExtUrl.showSelectActionDialog(mHandler, context!!, m_ThemeTitle, "", link, "", "", "", m_Id, m_Nick)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu!!.add(R.string.refresh)
                .setIcon(R.drawable.refresh)
                .setOnMenuItemClickListener { menuItem ->
                    reload()
                    true
                }.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        menu.add(R.string.setting)
                .setIcon(R.drawable.settings_white)
                .setOnMenuItemClickListener { menuItem ->
                    val intent = Intent(mainActivity, QmsChatPreferencesActivity::class.java)
                    mainActivity.startActivity(intent)
                    true
                }.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add(R.string.delete_dialog)
                .setIcon(R.drawable.delete)
                .setOnMenuItemClickListener { menuItem ->
                    deleteDialog()
                    true
                }.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)

        menu.add(R.string.font_size)
                .setOnMenuItemClickListener { menuItem ->
                    showFontSizeDialog()
                    true
                }

        menu.add(R.string.profile_interlocutor)
                .setOnMenuItemClickListener { menuItem ->
                    showCompanionProfile()
                    true
                }.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    /*
    private void showThread() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(BaseFragmentActivity.SENDER_ACTIVITY)) {
            if ("class org.softeg.slartus.forpdaplus.qms_2_0.QmsContactThemesActivity".equals(getIntent().getExtras().get(BaseFragmentActivity.SENDER_ACTIVITY))) {
                finish();
                return;
            }
        }

        QmsContactThemesActivity.showThemes(getMainActivity(), m_Id, m_Nick);
        //finish();
    }
    */

    private inner class SendTask internal constructor(context: Context) : AsyncTask<ArrayList<String>, Void, Boolean>() {


        private val dialog = MaterialDialog.Builder(context)
                .progress(true, 0)
                .content(getString(R.string.sending_message))
                .build()
        internal var m_ChatBody: String = ""
        private var ex: Throwable? = null

        override fun doInBackground(vararg params: ArrayList<String>): Boolean? {
            try {

                m_ChatBody = transformChatBody(QmsApi.sendMessage(Client.getInstance(), m_Id!!, m_TId!!, m_MessageText!!,
                        encoding, attachList))

                return true
            } catch (e: Throwable) {
                ex = e
                return false
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
            //            setLoading(false);

            onPostChat(m_ChatBody, success!!, ex)
            attachList.clear()
            refreshAttachmentsInfo()
        }


    }

    private inner class DeleteTask internal constructor(context: Context) : AsyncTask<ArrayList<String>, Void, Boolean>() {


        private val dialog: MaterialDialog
        internal var m_ChatBody: String = ""
        private var ex: Throwable? = null

        init {

            dialog = MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.deleting_messages)
                    .build()
        }

        override fun doInBackground(vararg params: ArrayList<String>): Boolean? {
            try {

                m_ChatBody = transformChatBody(QmsApi.deleteMessages(Client.getInstance(),
                        m_Id!!, m_TId!!, params[0], encoding))

                return true
            } catch (e: Throwable) {
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

            onPostChat(m_ChatBody, success!!, ex)
            stopDeleteMode(true)
        }
    }

    private inner class DeleteDialogTask internal constructor(context: Context, internal var m_Ids: ArrayList<String>) : AsyncTask<ArrayList<String>, Void, Boolean>() {


        private val dialog: MaterialDialog
        private var ex: Throwable? = null

        init {
            dialog = MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.deleting_dialogs)
                    .build()
        }

        override fun doInBackground(vararg params: ArrayList<String>): Boolean? {
            try {

                QmsApi.deleteDialogs(Client.getInstance(), m_Id!!, m_Ids)

                return true
            } catch (e: Throwable) {
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

            if (success != true) {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error,
                            Toast.LENGTH_SHORT).show()
            }
            stopDeleteMode(true)
            //showThread();

        }
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
                .itemsCallback { dialog, itemView, which, text ->
                    val item = attachList[which]
                    MaterialDialog.Builder(mainActivity)
                            .title(R.string.ConfirmTheAction)
                            .cancelable(true)
                            .content(R.string.SureDeleteFile)
                            .positiveText(R.string.delete)
                            .onPositive { dialog1, which1 -> DeleteAttachTask(this@QmsChatFragment, item.id).execute() }
                            .negativeText(R.string.cancel)
                            .show()
                }
                .positiveText(R.string.do_download)
                .onPositive { dialog, which -> startAddAttachment() }
                .negativeText(R.string.ok)
                .show()
    }


    private fun startAddAttachment() {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
            Toast.makeText(mainActivity, R.string.no_app_for_get_image_file, Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            AppLog.e(mainActivity, ex)
        }

    }

    private inner class UpdateTask internal constructor(context: Context, private val attachFilePaths: List<String>) : AsyncTask<String, Pair<String, Long>, Boolean>() {
        private val dialog = MaterialDialog.Builder(context)
                //                    .progress(false, 100, false)
                .progress(true, 0)
                .content(R.string.sending_file)
                .show()
        private var m_ProgressState: ProgressState? = null

        private val attaches = ArrayList<EditAttach>()

        private var ex: Throwable? = null

        internal constructor(context: Context, newAttachFilePath: String) : this(context, ArrayList<String>(listOf<String>(newAttachFilePath)))

        override fun doInBackground(vararg params: String): Boolean? {
            try {
                m_ProgressState = object : ProgressState() {
                    override fun update(message: String, percents: Long) {
                        publishProgress(Pair("", percents))
                    }

                }
                for (newAttachFilePath in attachFilePaths) {
                    val editAttach = QmsApi.attachFile(newAttachFilePath, m_ProgressState!!)
                    attaches.add(editAttach)
                }

                return true
            } catch (e: Throwable) {
                ex = e
                return false
            }

        }

        //        @Override
        //        protected void onProgressUpdate(Pair<String, Integer>... values) {
        //            super.onProgressUpdate(values);
        //            if (!TextUtils.isEmpty(values[0].first))
        //                dialog.setContent(values[0].first);
        //            dialog.setProgress(values[0].second);
        //        }

        // can use UI thread here
        override fun onPreExecute() {
            this.dialog.setCancelable(true)
            this.dialog.setCanceledOnTouchOutside(false)
            //            this.dialog.setOnCancelListener(dialogInterface -> {
            //                if (m_ProgressState != null)
            //                    m_ProgressState.cancel();
            //                cancel(false);
            //            });
            //            this.dialog.setProgress(0);
            this.dialog.isIndeterminateProgress

            this.dialog.show()
        }

        // can use UI thread here
        override fun onPostExecute(success: Boolean?) {
            if (this.dialog.isShowing) {
                this.dialog.dismiss()
            }

            if (success!! || isCancelled && attaches.size > 0) {
                attachList.addAll(attaches)
                refreshAttachmentsInfo()
            } else {

                if (ex != null) {
                    ex!!.printStackTrace()
                    Log.e("TEST", "Error " + ex!!.message)
                    AppLog.e(mainActivity, ex)
                } else
                    Toast.makeText(mainActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()

            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        override fun onCancelled(success: Boolean?) {
            super.onCancelled(success)
            if (success!! || isCancelled && attaches.size > 0) {
                attachList.addAll(attaches)
                refreshAttachmentsInfo()
            } else {
                if (ex != null)
                    AppLog.e(mainActivity, ex)
                else
                    Toast.makeText(mainActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun refreshAttachmentsInfo() {
        btnAttachments!!.text = attachList.size.toString() + ""
    }

    companion object {
        private val MID_KEY = "mid"
        private val TID_KEY = "tid"
        private val THEME_TITLE_KEY = "theme_title"
        private val NICK_KEY = "nick"
        private val PAGE_BODY_KEY = "page_body"
        private val POST_TEXT_KEY = "PostText"
        private val FILECHOOSER_RESULTCODE = 1

        fun openChat(userId: String, userNick: String, tid: String, themeTitle: String, pageBody: String) {
            MainActivity.addTab(themeTitle, themeTitle + userId, newInstance(userId, userNick, tid, themeTitle, pageBody))
        }

        fun openChat(userId: String, userNick: String, tid: String, themeTitle: String) {
            MainActivity.addTab(themeTitle, themeTitle + userId, newInstance(userId, userNick, tid, themeTitle))
        }

        fun newInstance(userId: String, userNick: String, tid: String, themeTitle: String, pageBody: String): QmsChatFragment {
            val args = Bundle()
            args.putString(MID_KEY, userId)
            args.putString(NICK_KEY, userNick)
            args.putString(TID_KEY, tid)
            args.putString(THEME_TITLE_KEY, themeTitle)
            args.putString(PAGE_BODY_KEY, pageBody)

            val fragment = QmsChatFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(userId: String, userNick: String, tid: String, themeTitle: String): QmsChatFragment {
            val args = Bundle()
            args.putString(MID_KEY, userId)
            args.putString(NICK_KEY, userNick)
            args.putString(TID_KEY, tid)
            args.putString(THEME_TITLE_KEY, themeTitle)

            val fragment = QmsChatFragment()
            fragment.arguments = args
            return fragment
        }

        val encoding: String
            get() {
                val prefs = App.getInstance().preferences

                return prefs.getString("qms.chat.encoding", "UTF-8")

            }
        private const val MY_INTENT_CLICK = 302
    }

}
