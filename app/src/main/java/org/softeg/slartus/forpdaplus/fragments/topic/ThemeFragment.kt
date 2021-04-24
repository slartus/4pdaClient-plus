package org.softeg.slartus.forpdaplus.fragments.topic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nineoldandroids.view.ViewPropertyAnimator
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaapi.TopicApi
import org.softeg.slartus.forpdaapi.parsers.MentionsParser
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.PatternExtensions
import org.softeg.slartus.forpdaplus.*
import org.softeg.slartus.forpdaplus.AppTheme.currentBackgroundColorHtml
import org.softeg.slartus.forpdaplus.AppTheme.themeCssFileName
import org.softeg.slartus.forpdaplus.classes.*
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.classes.common.Functions
import org.softeg.slartus.forpdaplus.classes.common.StringUtils
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.common.HelpTask
import org.softeg.slartus.forpdaplus.common.HelpTask.OnMethodListener
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewDialogFragment
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer
import org.softeg.slartus.forpdaplus.controls.quickpost.PostTask.PostResult
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostFragment
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostFragment.PostSendListener
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment.Companion.NEW_EDIT_POST_REQUEST_CODE
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment.Companion.editPost
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment.IBricksListDialogCaller
import org.softeg.slartus.forpdaplus.listfragments.NotesListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment.Companion.showActivity
import org.softeg.slartus.forpdaplus.listfragments.TopicReadersListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicUtils.showSubscribeSelectTypeDialog
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment
import org.softeg.slartus.forpdaplus.listfragments.mentions.MentionsHtmlBuilder
import org.softeg.slartus.forpdaplus.listfragments.mentions.MentionsListFragment
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment.Companion.showActivity
import org.softeg.slartus.forpdaplus.listfragments.next.forum.ForumFragment.Companion.showActivity
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.TopicReadersBrickInfo
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo
import org.softeg.slartus.forpdaplus.notes.NoteDialog
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.repositories.InternetConnection
import org.softeg.slartus.forpdaplus.repositories.TabsRepository
import org.softeg.slartus.forpdaplus.utils.Utils
import ru.slartus.http.AppResponse
import ru.slartus.http.Http.Companion.instance
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min

/**
 * Created by radiationx on 28.10.15.
 */
class ThemeFragment : WebViewFragment(), IBricksListDialogCaller, PostSendListener {
    var mQuickPostPanel: LinearLayout? = null
    var fab: FloatingActionButton? = null
    var mWebView: AdvWebView? = null
    var txtSearch: EditText? = null
    var pnlSearch: LinearLayout? = null
    var buttonsPanel: FrameLayout? = null
    val handler = Handler()
    var lastUrl: String? = null
        private set
    private var lastResponse: AppResponse? = null
    private var m_History: ArrayList<SessionHistory>? = ArrayList()
    var topic: ExtTopic? = null
        private set
    private var m_SpoilFirstPost = true

    // текст редактирования сообщения при переходе по страницам
    private var m_PostBody = ""

    // id сообщения к которому скроллить
    private var m_ScrollElement: String? = null
    private var m_FromHistory = false
    private var m_ScrollY = 0
    private var mQuickPostFragment: QuickPostFragment? = null
    private var lastStyle: String? = null
    private var asyncTask: GetThemeTask? = null
    private var webViewClient: MyWebViewClient? = null
    override fun getWebViewClient(): WebViewClient {
        if (webViewClient == null) webViewClient = MyWebViewClient()
        return webViewClient!!
    }

    override fun getTitle(): String {
        return topic!!.title
    }

    override fun getUrl(): String {
        return lastUrl!!
    }

    override fun reload() {
        reloadTopic()
    }

    override fun getAsyncTask(): AsyncTask<*, *, *>? {
        return asyncTask
    }

    override fun closeTab(): Boolean {
        postBody
        return if (!TextUtils.isEmpty(m_PostBody)) {
            MaterialDialog.Builder(mainActivity)
                    .title(R.string.ConfirmTheAction)
                    .content(R.string.entered_text)
                    .positiveText(R.string.apply_yes)
                    .onPositive { dialog: MaterialDialog?, which: DialogAction? ->
                        clear()
                        mainActivity.tryRemoveTab(tag)
                    }
                    .negativeText(R.string.apply_cancel)
                    .show()
            true
        } else {
            clear()
            false
        }
    }

    override fun Prefix(): String {
        return "theme"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.theme, container, false)
        mQuickPostPanel = view.findViewById(R.id.quick_post_panel)
        fab = view.findViewById(R.id.fab)
        mWebView = view.findViewById(R.id.wvBody)
        txtSearch = view.findViewById(R.id.txtSearch)
        pnlSearch = view.findViewById(R.id.pnlSearch)
        buttonsPanel = view.findViewById(R.id.buttonsPanel)
        view.findViewById<View>(R.id.btnPrevSearch).setOnClickListener { view1: View? -> webView.findNext(false) }
        view.findViewById<View>(R.id.btnNextSearch).setOnClickListener { view1: View? -> webView.findNext(true) }
        view.findViewById<View>(R.id.btnCloseSearch).setOnClickListener { view1: View? -> closeSearch() }
        view.findViewById<View>(R.id.btnUp).setOnClickListener { view1: View? -> webView.pageUp(true) }
        view.findViewById<View>(R.id.btnDown).setOnClickListener { view1: View? -> webView.pageDown(true) }
        initSwipeRefreshLayout()
        lastStyle = themeCssFileName
        LoadsImagesAutomatically = null
        mainActivity.setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL) // чтобы поиск начинался при вводе текста
        mQuickPostFragment = childFragmentManager.findFragmentById(R.id.quick_post_fragment) as QuickPostFragment?
        mQuickPostFragment!!.setParentTag(tag!!)
        txtSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                doSearch(txtSearch?.text?.toString() ?: "")
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        hideMessagePanel()
        closeSearch()
        loadPreferences(App.getInstance().preferences)
        showTheme(IntentActivity.normalizeThemeUrl(arguments!!.getString(TOPIC_URL_KEY)))
        if (App.getInstance().preferences.getBoolean("pancilInActionBar", false)) {
            fab?.hide()
        } else {
            setHideFab(fab)
            setFabColors(fab)
            fab?.setOnClickListener({ view1: View? -> toggleMessagePanelVisibility() })
        }
        initWebView()
        return view
    }

    @SuppressLint("AddJavascriptInterface")
    private fun initWebView() {
        registerForContextMenu(webView)
        setWebViewSettings()
        webView.settings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT < 18) webView.settings.setAppCacheMaxSize((1024 * 1024 * 8).toLong())
        val appCachePath = App.getInstance().cacheDir.absolutePath
        webView.settings.setAppCachePath(appCachePath)
        webView.settings.setAppCacheEnabled(true)
        webView.settings.allowFileAccess = true
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        //webView.getSettings().setLoadWithOverviewMode(false);
        //webView.getSettings().setUseWideViewPort(true);
        webView.settings.defaultFontSize = Preferences.Topic.getFontSize()
        webView.webChromeClient = MyChromeClient()
        /*if (getSupportActionBar() != null)
            webView.setActionBarheight(getSupportActionBar().getHeight());*/setHideArrows(Preferences.isHideArrows())
        webView.addJavascriptInterface(ForPdaWebInterface(this), ForPdaWebInterface.NAME)
        webView.setActionModeListener { actionMode: ActionMode, callback: ActionMode.Callback?, type: Int ->
            val menu = actionMode.menu
            menu.add(R.string.quote)
                    .setOnMenuItemClickListener { item: MenuItem? ->
                        webView.evalJs("htmlOutSelectionPostInfo();")
                        actionMode.finish()
                        true
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        //webView.getSettings().setJavaScriptEnabled(false);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            if (m_History != null) Paper.book().write("History", m_History)
            if (topic != null) Paper.book().write("Topic", topic)
            //outState.putSerializable("History", m_History);
            //outState.putSerializable("Topic", m_Topic);
            webView.saveState(outState)
            outState.putString("LastUrl", lastUrl)
            outState.putString("ScrollElement", m_ScrollElement)
            outState.putString("LastStyle", lastStyle)
            outState.putBoolean("FromHistory", m_FromHistory)
            outState.putString("LoadsImagesAutomatically", if (LoadsImagesAutomatically == null) "null" else if (LoadsImagesAutomatically!!) "1" else "0")
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) return
        try {
            topic = Paper.book().read<ExtTopic?>("Topic", null)
            if (topic != null) mQuickPostFragment!!.setTopic(topic!!.forumId, topic!!.id, Client.getInstance().authKey)
            lastUrl = savedInstanceState.getString("LastUrl")
            if (topic != null) topic!!.setLastUrl(lastUrl)
            m_ScrollElement = savedInstanceState.getString("ScrollElement")
            m_FromHistory = savedInstanceState.getBoolean("FromHistory")
            val sLoadsImagesAutomatically = savedInstanceState.getString("LoadsImagesAutomatically")
            LoadsImagesAutomatically = if ("null" == sLoadsImagesAutomatically) null else java.lang.Boolean.parseBoolean(sLoadsImagesAutomatically)
            loadPreferences(App.getInstance().preferences)
            m_History = Paper.book().read("History", m_History)

            m_History?.let { history ->
                if (history.size > 0) {
                    val sessionHistory = history.get(history.size - 1)
                    m_ScrollY = sessionHistory.y
                    lastUrl = sessionHistory.url
                    topic = sessionHistory.topic
                    if (topic != null) topic!!.setLastUrl(lastUrl)
                    if (topic != null) mQuickPostFragment!!.setTopic(topic!!.forumId, topic!!.id, Client.getInstance().authKey)
                    if (sessionHistory.body == null) {
                        showTheme(sessionHistory.url)
                    } else {
                        val body = sessionHistory.body.replace(savedInstanceState.getString("LastStyle")!!,
                                themeCssFileName)
                        showBody(body)
                        sessionHistory.body = body
                    }
                }
            }

        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //    @Override
    //    public void onPrepareOptionsMenu(Menu menu) {
    //        super.onPrepareOptionsMenu(menu);
    //        if (!m_FirstTime)
    //            onPrepareOptionsMenu();
    //        m_FirstTime = false;
    //        if (mTopicOptionsMenu != null)
    //            configureOptionsMenu(getMainActivity(), getHandler(), mTopicOptionsMenu, true, getLastUrl());
    //        else if (getTopic() != null)
    //            mTopicOptionsMenu = addOptionsMenu(getMainActivity(), getHandler(), menu, true, getLastUrl());
    //
    //    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu()
        val pancil = App.getInstance().preferences.getBoolean("pancilInActionBar", false)
        menu.findItem(R.id.new_post_item).isVisible = pancil
        val onTopicReadersAndWriters = Preferences.Topic.getReadersAndWriters()
        menu.findItem(R.id.topic_readers_item).isVisible = !onTopicReadersAndWriters
        menu.findItem(R.id.topic_writers_item).isVisible = !onTopicReadersAndWriters
        menu.findItem(R.id.avatars_item).title = String.format(Utils.getS(R.string.avatars), App.getContext().resources.getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()])
        menu.findItem(R.id.hide_pencil_item).isVisible = !pancil
        menu.findItem(R.id.hide_pencil_item).isChecked = Preferences.isHideFab()
        menu.findItem(R.id.hide_arrows_item).isChecked = Preferences.isHideArrows()
        menu.findItem(R.id.loading_img_for_session_item).isChecked = loadsImagesAutomatically
        menu.findItem(R.id.multi_moderation_item).isVisible = Preferences.System.isCurator()
        menu.findItem(R.id.search_item).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val topic = topic
        try {
            val args = Bundle()
            when (id) {
                R.id.new_post_item -> {
                    toggleMessagePanelVisibility()
                    return true
                }
                R.id.refresh_item -> {
                    reloadTopic()
                    return true
                }
                R.id.page_attaches_item -> {
                    showPageAttaches()
                    return true
                }
                R.id.topic_attaches_item -> {
                    showTopicAttaches()
                    return true
                }
                R.id.page_search_item -> {
                    onSearchRequested()
                    return true
                }
                R.id.topic_search_item -> {
                    if (topic != null) {
                        SearchSettingsDialogFragment.showSearchSettingsDialog(mainActivity,
                                SearchSettingsDialogFragment.createTopicSearchSettings(topic.id))
                    }
                    return true
                }
                R.id.add_to_favorites_item -> {
                    try {
                        if (topic != null) {
                            showSubscribeSelectTypeDialog(context!!, handler, topic, null)
                        }
                    } catch (ex: Exception) {
                        AppLog.e(context, ex)
                    }
                    return true
                }
                R.id.del_from_favorites_item -> {
                    if (topic != null) {
                        val helpTask = HelpTask(context, context!!.getString(R.string.DeletingFromFavorites))
                        helpTask.setOnPostMethod { param: Any? ->
                            if (helpTask.Success) Toast.makeText(context, param as String?, Toast.LENGTH_SHORT).show() else AppLog.e(context, helpTask.ex)
                            null
                        }
                        helpTask.execute(OnMethodListener { param: Any? -> TopicApi.deleteFromFavorites(Client.getInstance(), topic.id) })
                    }
                    return true
                }
                R.id.open_topic_forum_item -> {
                    if (topic != null) {
                        showActivity(topic.forumId, topic.id)
                    }
                    return true
                }
                R.id.topic_notes_item -> {
                    if (topic != null) {
                        args.putString(NotesListFragment.TOPIC_ID_KEY, topic.id)
                        MainActivity.showListFragment(NotesBrickInfo().name, args)
                    }
                    return true
                }
                R.id.topic_readers_item -> {
                    if (topic != null) {
                        args.putString(TopicReadersListFragment.TOPIC_ID_KEY, topic.id)
                        MainActivity.showListFragment(topic.id, TopicReadersBrickInfo.NAME, args)
                    }
                    return true
                }
                R.id.topic_writers_item -> {
                    if (topic != null) {
                        args.putString(TopicWritersListFragment.TOPIC_ID_KEY, topic.id)
                        MainActivity.showListFragment(topic.id, TopicWritersBrickInfo.NAME, args)
                    }
                    return true
                }
                R.id.link_item -> {
                    if (topic != null) {
                        ExtUrl.showSelectActionDialog(mainActivity, Utils.getS(R.string.link),
                                if (TextUtils.isEmpty(lastUrl)) "https://4pda.ru/forum/index.php?showtopic=" + topic.id else lastUrl)
                    }
                    return true
                }
                R.id.avatars_item -> {
                    val avatars = App.getContext().resources.getStringArray(R.array.AvatarsShowTitles)
                    MaterialDialog.Builder(mainActivity)
                            .title(R.string.show_avatars)
                            .cancelable(true)
                            .items(*avatars)
                            .itemsCallbackSingleChoice(Preferences.Topic.getShowAvatarsOpt()) { dialog: MaterialDialog?, view1: View?, i: Int, avatars1: CharSequence? ->
                                Preferences.Topic.setShowAvatarsOpt(i)
                                activity!!.invalidateOptionsMenu()
                                true // allow selection
                            }
                            .show()
                    return true
                }
                R.id.hide_pencil_item -> {
                    Preferences.setHideFab(!Preferences.isHideFab())
                    setHideFab(fab)
                    activity!!.invalidateOptionsMenu()
                    return true
                }
                R.id.hide_arrows_item -> {
                    Preferences.setHideArrows(!Preferences.isHideArrows())
                    setHideArrows(Preferences.isHideArrows())
                    activity!!.invalidateOptionsMenu()
                    return true
                }
                R.id.loading_img_for_session_item -> {
                    val loadImagesAutomatically1 = loadsImagesAutomatically
                    loadsImagesAutomatically = !loadImagesAutomatically1
                    return true
                }
                R.id.font_size_item -> {
                    showFontSizeDialog()
                    return true
                }
                R.id.topic_style_item -> {
                    showStylesDialog(App.getInstance().preferences)
                    return true
                }
                R.id.multi_moderation_item -> {
                    if (topic != null) {
                        ThemeCurator.showMmodDialog(activity, this@ThemeFragment, topic.id)
                    }
                    return true
                }
            }
        } catch (ex: Exception) {
            AppLog.e(context, ex)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        //inflater.inflate(R.menu.user, menu);
        if (inflater != null) inflater.inflate(R.menu.topic, menu)
    }

    override fun onResume() {
        super.onResume()
        if (topic != null) {
            setSubtitle(topic!!.currentPage.toString() + "/" + topic!!.pagesCount)
        }
    }

    private fun hideKeyboard() {
        mQuickPostFragment!!.hideKeyboard()
    }

    override fun hidePopupWindows() {
        super.hidePopupWindows()
        mQuickPostFragment!!.hidePopupWindow()
    }

    fun onSearchRequested() {
        hideMessagePanel()
        pnlSearch!!.visibility = View.VISIBLE
    }

    protected fun showQuoteEditor(url: String?) {
        val quoteEditorDialogFragment = ThemeQuoteEditor
                .newInstance(url, tag)
        quoteEditorDialogFragment.show(childFragmentManager, "dialog")
    }

    fun checkBodyAndReload(body: String?) {
        if (TextUtils.isEmpty(body)) {
            reloadTopic()
        }
    }

    @JavascriptInterface
    fun checkBodyAndReload() {
        try {
            webView!!.evalJs("window.HTMLOUT.checkBodyAndReload(document.getElementsByTagName('body')[0].innerHTML);")
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    fun showPageAttaches() {
        try {
            webView!!.evalJs("window.HTMLOUT.showTopicAttaches('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    fun showTopicAttaches() {
        if (topic == null) return
        showActivity(topic!!.id)
    }

    private fun doSearch(query: String) {
        if (TextUtils.isEmpty(query)) return
        if (Build.VERSION.SDK_INT >= 16) {
            webView!!.findAllAsync(query)
        } else {
            webView!!.findAll(query)
        }
        try {
            val m = WebView::class.java.getMethod("setFindIsUp", java.lang.Boolean.TYPE)
            m.invoke(webView, true)
        } catch (ignored: Throwable) {
        }
        //onSearchRequested();
    }

    private fun closeSearch() {
        handler.post {
            if (Build.VERSION.SDK_INT >= 16) {
                webView!!.findAllAsync("")
            } else {
                webView!!.findAll("")
            }
            try {
                val m = WebView::class.java.getMethod("setFindIsUp", java.lang.Boolean.TYPE)
                m.invoke(webView, false)
            } catch (ignored: Throwable) {
            }
            pnlSearch!!.visibility = View.GONE
            val imm = (mainActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(pnlSearch!!.windowToken, 0)
        }
    }

    override fun loadPreferences(prefs: SharedPreferences) {
        super.loadPreferences(prefs)
        LoadsImagesAutomatically = WebViewExternals.isLoadImages("theme")
        m_SpoilFirstPost = Preferences.Topic.getSpoilFirstPost()
    }

    override fun showLinkMenu(link: String) {
        showLinkMenu(link, "")
    }

    fun showLinkMenu(link: String, postId: String?) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link == "#" || link.startsWith("file:///")) return
        ExtUrl.showSelectActionDialog(handler, mainActivity, topic!!.title, "", link, topic!!.id,
                topic!!.title, postId, "", "")
    }

    override fun onBackPressed(): Boolean {
        if (pnlSearch!!.visibility == View.VISIBLE) {
            closeSearch()
            return true
        }
        if (m_History!!.size > 1) {
            m_History!!.removeAt(m_History!!.size - 1)
            val sessionHistory = m_History!![m_History!!.size - 1]
            m_ScrollY = sessionHistory.y
            if (sessionHistory.body == null) {
                m_History!!.removeAt(m_History!!.size - 1)
                showTheme(sessionHistory.url)
            } else {
                lastUrl = sessionHistory.url
                topic = sessionHistory.topic
                if (topic != null) topic!!.setLastUrl(lastUrl)
                if (topic != null) mQuickPostFragment!!.setTopic(topic!!.forumId, topic!!.id, Client.getInstance().authKey)
                try {
                    showBody(sessionHistory.body)
                } catch (e: Exception) {
                    AppLog.e(e)
                }
            }
            return true
        }
        postBody
        return if (!TextUtils.isEmpty(m_PostBody)) {
            MaterialDialog.Builder(mainActivity)
                    .title(R.string.ConfirmTheAction)
                    .content(R.string.entered_text)
                    .positiveText(R.string.apply_yes)
                    .onPositive { dialog: MaterialDialog?, which: DialogAction? ->
                        clear()
                        mainActivity.tryRemoveTab(tag)
                    }
                    .negativeText(R.string.apply_cancel)
                    .show()
            true
        } else {
            clear()
            false
        }
    }

    @JvmOverloads
    fun clear(clearChache: Boolean = false) {
        webView!!.webViewClient = null
        webView!!.loadData("<html><head></head><body bgcolor=" + currentBackgroundColorHtml + "></body></html>", "text/html", "UTF-8")
        if (clearChache) webView!!.clearCache(true)
        if (topic != null) topic!!.dispose()
        topic = null
    }

    @set:JavascriptInterface
    var postBody: String
        get() {
            m_PostBody = mQuickPostFragment!!.postBody
            return m_PostBody
        }
        set(postBody) {
            m_PostBody = postBody
        }

    fun insertQuote(postId: CharSequence?, postDate: CharSequence?, userNick: CharSequence?, text: CharSequence?) {
        val endQuote = "\n[/quote]"
        val fullQuoteText = "[quote name=\"${userNick ?: ""}\" date=\"${postDate ?: ""}\" post=\"${postId ?: ""}\"]\n${text ?: ""}$endQuote"
        val selectedIndex = -1 //fullQuoteText.length() - endQuote.length(); //наверное , лучше курсор в конец всё же ставить
        mainActivity.runOnUiThread { Handler().post { insertTextToPost(fullQuoteText, selectedIndex) } }
    }

    @JavascriptInterface
    fun quote(forumId: String?, topicId: String?, postId: String, postDate: String?, userId: String?, userNick: String) {
        try {
            val finalPostDate = Functions.getForumDateTime(Functions.parseForumDateTime(postDate, Functions.getToday(), Functions.getYesterToday()))
            val mUserNick = userNick.replace("\"", "\\\"")
            val clipboardText = StringUtils.fromClipboard(App.getContext())
            if (TextUtils.isEmpty(clipboardText)) {
                insertQuote(postId, finalPostDate, mUserNick, "")
                return
            }
            val titles = arrayOf<CharSequence>(Utils.getS(R.string.blank_quote), Utils.getS(R.string.quote_from_buffer))
            MaterialDialog.Builder(context!!)
                    .title(R.string.quote)
                    .cancelable(true)
                    .items(*titles)
                    .itemsCallback { dialog: MaterialDialog?, view1: View?, i: Int, titles1: CharSequence? ->
                        when (i) {
                            0 -> insertQuote(postId, finalPostDate, mUserNick, "")
                            1 -> insertQuote(postId, finalPostDate, mUserNick, clipboardText)
                        }
                    }
                    .show()
        } catch (ex: Throwable) {
            AppLog.e(context, ex)
        }
    }

    fun openActionMenu(postId: String, postDate: String?,
                       userId: String?, userNick: String,
                       canEdit: Boolean, canDelete: Boolean) {
        try {
            val list: MutableList<MenuListDialog> = ArrayList()
            if (Client.getInstance().logined) {
                list.add(MenuListDialog(Utils.getS(R.string.url_post)) { showLinkMenu(Post.getLink(topic!!.id, postId), postId) })
                list.add(MenuListDialog(Utils.getS(R.string.report_msg)) { Post.claim(mainActivity, handler, topic!!.id, postId) })
                if (canEdit) {
                    list.add(MenuListDialog(Utils.getS(R.string.edit_post)) { editPost(mainActivity, topic!!.forumId, topic!!.id, postId, Client.getInstance().authKey, tag!!) })
                }
                if (canDelete) {
                    list.add(MenuListDialog(Utils.getS(R.string.delete_post)) { prepareDeleteMessage(postId) })
                }
                list.add(MenuListDialog(Utils.getS(R.string.quote_post)) { quote(topic!!.forumId, topic!!.id, postId, postDate, userId, userNick) })
            }
            list.add(MenuListDialog(Utils.getS(R.string.create_note)) {
                NoteDialog.showDialog(handler, mainActivity, topic!!.title, null,
                        "https://4pda.ru/forum/index.php?showtopic=" + topic!!.id + "&view=findpost&p=" + postId,
                        topic!!.id, topic!!.title, postId, null, null)
            })
            ExtUrl.showContextDialog(context, null, list)
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    @Throws(Exception::class)
    private fun showBody(body: String?) {
        super.showBody()
        try {
            setScrollElement()
            title = topic!!.title
            if (supportActionBar != null) setSubtitle(topic!!.currentPage.toString() + "/" + topic!!.pagesCount)

            //webView.loadDataWithBaseURL(m_LastUrl, body, "text/html", "UTF-8", null);
            webView.loadDataWithBaseURL("https://4pda.ru/forum/", body, "text/html", "UTF-8", null)
            TopicsHistoryTable.addHistory(topic, lastUrl)
            if (buttonsPanel!!.translationY != 0f) ViewPropertyAnimator.animate(buttonsPanel)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(500)
                    .translationY(0f)
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    fun showMessagePanel() {
        fab!!.setImageResource(R.drawable.close_white)
        pnlSearch!!.visibility = View.GONE
        mQuickPostPanel!!.visibility = View.VISIBLE
        mQuickPostPanel!!.isEnabled = Client.getInstance().logined
        mQuickPostFragment!!.showKeyboard()
    }

    fun hideMessagePanel() {
        fab!!.setImageResource(R.drawable.pencil)
        mQuickPostPanel!!.visibility = View.GONE
        mQuickPostFragment!!.hidePopupWindow()
        hideKeyboard()
    }

    fun toggleMessagePanelVisibility() {
        if (!Client.getInstance().logined) {
            Toast.makeText(mainActivity, R.string.NeedToLogin, Toast.LENGTH_SHORT).show()
            return
        }
        if (mQuickPostPanel!!.visibility == View.GONE) showMessagePanel() else hideMessagePanel()
    }

    var loadsImagesAutomatically: Boolean
        get() = WebViewExternals.isLoadImages("theme")
        set(loadsImagesAutomatically) {
            LoadsImagesAutomatically = loadsImagesAutomatically
            MaterialDialog.Builder(mainActivity)
                    .title(R.string.select_action)
                    .content(R.string.refresh_page)
                    .positiveText(R.string.refresh_p)
                    .onPositive { dialog: MaterialDialog?, which: DialogAction? -> reloadTopic() }
                    .negativeText(R.string.apply_no)
                    .show()
        }

    override fun getSupportFragmentManager(): FragmentManager {
        return childFragmentManager
    }

    override fun onBricksListDialogResult(dialog: DialogInterface, dialogId: String, brickInfo: BrickInfo, args: Bundle) {
        dialog.dismiss()
        MainActivity.showListFragment(brickInfo.name, args)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NEW_EDIT_POST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val url = data!!.getStringExtra(EditPostFragment.POST_URL_KEY)!!
                mQuickPostFragment!!.clearPostBody()
                closeSearch()
                asyncTask = GetThemeTask(this)
                if (data.extras != null && data.extras!!.containsKey(EditPostFragment.TOPIC_BODY_KEY)) {
                    asyncTask!!.execute(url.replace("|", ""), data.getStringExtra(EditPostFragment.TOPIC_BODY_KEY))
                } else asyncTask!!.execute(url.replace("|", ""))
            }
        }
    }

    private fun checkIsTheme(url: String): Boolean {
        var url: String? = url
        url = IntentActivity.normalizeThemeUrl(url)
        val patterns = arrayOf(
                "(/+4pda.ru/+forum/+index.php\\?.*?showtopic=[^\"]*)",
                "(/+4pda.ru/+forum/+index.php\\?.*?act=findpost&pid=\\d+[^\"]*?)$",
                "(/+4pda.ru/+index.php\\?.*?act=findpost&pid=\\d+[^\"]*?)$"
        )
        for (pattern in patterns) {
            val m = Pattern.compile(pattern).matcher(url)
            if (m.find()) {
                goToAnchorOrLoadTopic(m.group(1))
                return true
            }
        }
        return false
    }

    fun reloadTopic() {
        m_ScrollY = webView!!.scrollY
        showTheme(lastUrl)
    }

    fun goToAnchorOrLoadTopic(topicUrl: String?) {
        try {
            if (topic == null || m_History!!.size == 0) {
                showTheme(topicUrl)
                return
            }


            /*Uri uri = Uri.parseCount(postUrl.toLowerCase());
            String postId = null;
            if (!TextUtils.isEmpty(getTopic().getId()) && getTopic().getId().equals(uri.getQueryParameter("showtopic")))
                postId = uri.getQueryParameter("p");
            if (TextUtils.isEmpty(postId) && "findpost".equals(uri.getQueryParameter("act")))
                postId = uri.getQueryParameter("pid");
            String anchor = "entry" + postId;
            if (!TextUtils.isEmpty(postId)) {
                anchor = "entry" + postId;
            } else {
                Pattern p = Pattern.compile("#(\\w+\\d+)");
                Matcher m = p.matcher(postUrl);
                if (m.find()) {
                    anchor = m.group(1);
                }
            }
            if (anchor == null) {
                showTheme(postUrl);
                return;
            }
            String fragment = anchor;
            String currentBody = m_History.get(m_History.size() - 1).getBody();
            if (currentBody.contains("name=\"" + fragment + "\"")) {
                webView.scrollTo(fragment);
                return;
            }*/showTheme(topicUrl)
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    private fun lofiversionToNormal(url: String?): String? {
        if (url == null) return null
        val m = Pattern.compile("lofiversion/index.php\\?t(\\d+)(?:-(\\d+))?.html", Pattern.CASE_INSENSITIVE)
                .matcher(url)
        return if (m.find()) "https://4pda.ru/forum/index.php?showtopic=" + m.group(1) +
                if (m.group(2) != null) "&st=" + m.group(2) else "" else url
    }

    fun showTheme(url: String?, clearText: Boolean) {
        if (clearText) mQuickPostFragment!!.clearPostBody()
        showTheme(url)
    }

    fun showTheme(url: String?) {
        var url = url
        try {
            closeSearch()
            if (url == null) {
                Toast.makeText(mainActivity, R.string.blank_url, Toast.LENGTH_SHORT).show()
                return
            }
            url = lofiversionToNormal(url)
            if (m_History!!.size > 0) {
                m_History!![m_History!!.size - 1].y = webView!!.scrollY
                webView!!.evalJs("window.HTMLOUT.setHistoryBody(" + (m_History!!.size - 1) + ",'<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
            }
            webView!!.webViewClient = getWebViewClient()

            loadTheme(url!!.replace("|", ""))
//            asyncTask = GetThemeTask(this)
//            asyncTask!!.execute(url!!.replace("|", ""))
        } catch (ex: Throwable) {
            AppLog.e(mainActivity, ex)
        }
    }

    fun loadTheme(topicUrl: String, pageBody: String? = null) {
        fun prepareTopicUrl(url: CharSequence?): CharSequence {
            val uri = Uri.parse(url.toString())
            return if (uri.host == null) uri.toString() else uri.query!!
        }


        InternetConnection.instance.loadDataOnInternetConnected({
            var scrollY = 0
            try {

                if (isAdded) {
                    setLoading(true)
                    scrollY = m_ScrollY
                    hideMessagePanel()
                }
            } catch (ex: Exception) {
                AppLog.e(null, ex)
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    var pageBody: String? = null
                    var m_ThemeBody: String? = null
                    var ex: Throwable? = null
                    var success = true
                    try {
                        val client = Client.getInstance()
                        lastUrl = topicUrl
                        lastUrl = "https://4pda.ru/forum/index.php?" + prepareTopicUrl(lastUrl)
                        if (pageBody == null) {
                            lastResponse = instance.performGet("https://4pda.ru/forum/index.php?" + prepareTopicUrl(lastUrl))
                            pageBody = lastResponse!!.responseBody
                            Client.getInstance().check(pageBody)
                        }
                        if (lastResponse != null) {
                            lastUrl = lastResponse!!.redirectUrlElseRequestUrl()
                        }
                        m_SpoilFirstPost = Preferences.Topic.getSpoilFirstPost()
                        val topicBodyBuilder = client.parseTopic(pageBody, App.getInstance(), lastUrl,
                                m_SpoilFirstPost)
                        topic = topicBodyBuilder.topic
                        topic!!.setLastUrl(lastUrl)
                        m_ThemeBody = topicBodyBuilder.body
                        topicBodyBuilder.clear()

                    } catch (e: Throwable) {
                        m_ThemeBody = pageBody
                        // Log.e(ThemeActivity.getMainActivity(), e);
                        ex = e
                        success = false
                    }
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            setLoading(false)
                            val item = TabsRepository.instance.getTabByTag(tag)
                            if (item != null) {
                                val tabItem = TabsRepository.instance.getTabByTag(item.parentTag)
                                if (tabItem != null && !tabItem.tag.contains("tag")) {
                                    val fragment = mainActivity.supportFragmentManager.findFragmentByTag(item.parentTag)
                                    if (fragment is TopicsListFragment && topic != null && topic!!.id != null) fragment.topicAfterClick(topic!!.id)
                                }
                            }
                            m_ScrollY = scrollY
                            if (topic != null) mQuickPostFragment!!.setTopic(topic!!.forumId, topic!!.id, Client.getInstance().authKey)

                            if (success && topic != null) {
                                addToHistory(m_ThemeBody)
                                try {
                                    showBody(m_ThemeBody)
                                } catch (e: Exception) {
                                    AppLog.e(e)
                                }
                            } else {
                                if (topic == null) {
                                    return@withContext
                                }
                                if (ex!!.javaClass != NotReportException::class.java) {
                                    setTitle(ex!!.message)
                                    webView.loadDataWithBaseURL(lastUrl, m_ThemeBody, "text/html", "UTF-8", null)
                                    //webView.loadDataWithBaseURL("https://4pda.ru/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                                    addToHistory(m_ThemeBody)
                                }
                                AppLog.e(mainActivity, ex) { showTheme(lastUrl) }
                            }
                        }
                    }
                } catch (ex: Throwable) {
                    AppLog.e(ex)
                }
            }
        })
    }

    fun setHistoryBody(index: Int, body: String?) {
        if (index > m_History!!.size) {
            addToHistory(body)
        } else {
            m_History!![index].body = body
        }
    }

    override fun getWebView(): AdvWebView {
        return mWebView!!
    }

    private fun prepareDeleteMessage(postId: String) {
        MaterialDialog.Builder(mainActivity)
                .title(R.string.ConfirmTheAction)
                .content(R.string.want_to_delete_msg)
                .positiveText(R.string.delete_m)
                .onPositive { dialog: MaterialDialog?, which: DialogAction? -> deleteMessage(postId) }
                .negativeText(R.string.apply_cancel)
                .show()
    }

    private fun deleteMessage(postId: String) {
        val dialog = MaterialDialog.Builder(mainActivity)
                .progress(true, 0)
                .cancelable(false)
                .content(R.string.deleting_msg)
                .show()
        Thread {
            var ex: Throwable? = null
            try {
                Post.delete(postId, Client.getInstance().authKey)
            } catch (e: Throwable) {
                ex = e
            }
            val finalEx = ex
            handler.post {
                try {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                } catch (ignored: Throwable) {
                }
                if (finalEx != null) AppLog.e(mainActivity, finalEx)
                m_ScrollY = 0
                //showTheme(getLastUrl());
                getWebView().evalJs("document.querySelector('div[name*=del$postId]').remove();")
            }
        }.start()
    }

    fun showRep(userId: String?) {
        showActivity(userId!!, false)
    }

    fun insertTextToPost(text: String?, cursorPosition: Int?) {
        mQuickPostFragment!!.insertTextToPost(text, cursorPosition!!)
        showMessagePanel()
    }

    fun post() {
        mQuickPostFragment!!.post()
    }

    override fun nextPage() {
        m_ScrollY = 0
        showTheme("showtopic=" + topic!!.id + "&st=" + topic!!.currentPage * topic!!.getPostsPerPageCount(lastUrl))
    }

    override fun prevPage() {
        m_ScrollY = 0
        showTheme("showtopic=" + topic!!.id + "&st=" + (topic!!.currentPage - 2) * topic!!.getPostsPerPageCount(lastUrl))
    }

    fun firstPage() {
        m_ScrollY = 0
        showTheme("showtopic=" + topic!!.id)
    }

    fun lastPage() {
        m_ScrollY = 0
        showTheme("showtopic=" + topic!!.id + "&st=" + (topic!!.pagesCount - 1) * topic!!.getPostsPerPageCount(lastUrl))
    }

    fun openFromSt(st: Int) {
        showTheme("showtopic=" + topic!!.id + "&st=" + st)
    }

    fun showChangeRep(postId: String?, userId: String?, userNick: String?, type: String?, title: String?) {
        ForumUser.startChangeRep(mainActivity, handler, userId, userNick, postId, type, title)
    }

    private fun addToHistory(topicBody: String?) {
        val historyLimit = Preferences.Topic.getHistoryLimit()
        if (m_History!!.size >= historyLimit && m_History!!.size > 0) m_History!![m_History!!.size - historyLimit].body = null
        m_History!!.add(SessionHistory(topic, lastUrl, topicBody, 0))
    }

    private fun setScrollElement() {
        m_ScrollElement = null
        val url = lastUrl
        if (url != null) {
            val p = Pattern.compile("#(\\w+\\d+)")
            val m = p.matcher(url)
            if (m.find()) {
                m_ScrollElement = m.group(1)
            }
        }
    }

    private var calledScroll = false
    private fun tryScrollToElement() {
        if (calledScroll) return
        calledScroll = true
        handler.postDelayed({
            if (m_ScrollY != 0) {
                webView!!.scrollY = m_ScrollY
            } else if (!TextUtils.isEmpty(m_ScrollElement)) {
                webView!!.evalJs("scrollToElement('$m_ScrollElement');")
            }
            calledScroll = false
        }, 250)
    }

    override fun onAfterSendPost(postResult: PostResult?) {
        if (postResult!!.Success) {
            hideMessagePanel()
            lastUrl = postResult.Response.redirectUrlElseRequestUrl()
            topic = postResult.ExtTopic
            topic?.setLastUrl(lastUrl)
            if (postResult.TopicBody == null) Log.e("ThemeActivity", "TopicBody is null")
            addToHistory(postResult.TopicBody)
            try {
                showBody(postResult.TopicBody)
            } catch (e: Exception) {
                AppLog.e(e)
            }
        } else {
            if (postResult.Exception != null) AppLog.e(mainActivity, postResult.Exception) { mQuickPostFragment!!.post() } else if (!TextUtils.isEmpty(postResult.ForumErrorMessage)) if (context != null) MaterialDialog.Builder(context!!)
                    .title(R.string.forum_msg)
                    .content(postResult.ForumErrorMessage)
                    .show()
        }
    }

    private class MyChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            //if (newProgress >= 10 && m_ScrollElement != null && m_ScrollY == 0) ;
            //tryScrollToElement();
        }
    }

    @JvmField
    var imageAttaches: ArrayList<ArrayList<String>> = ArrayList()
    fun showImage(url: String?) {
        val tPattern = Pattern.compile("(post/\\d*?/[\\s\\S]*?\\.(?:png|jpg|jpeg|gif))", Pattern.CASE_INSENSITIVE)
        val target = tPattern.matcher(url)
        var temp: Matcher
        val id: String
        if (target.find()) {
            id = target.group(1)
            for (list in imageAttaches) {
                for (i in list.indices) {
                    temp = tPattern.matcher(list[i])
                    if (temp.find()) {
                        if (temp.group(1) == id) {
                            ImgViewer.startActivity(context, list, i)
                            return
                        }
                    }
                }
            }
            ImgViewer.startActivity(context, url)
        }


    }

    private inner class MyWebViewClient : WebViewClient() {
        private val LOADING_ERROR_TIMEOUT = TimeUnit.SECONDS.toMillis(45)

        // WebView instance is kept in WeakReference because of mPageLoadingTimeoutHandlerTask
        private var mReference: WeakReference<WebView>? = null
        private var mLoadingFinished = false
        private var mLoadingError = false
        private var mOnErrorUrl: String? = null

        // Helps to know what page is loading in the moment
        // Allows check url to prevent onReceivedError/onPageFinished calling for wrong url
        // Helps to prevent double call of onPageStarted
        // These problems cached on many devices
        private var mUrl: String? = null
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, url: String) {
            Log.d(TAG, "onReceivedError>")
            if (mUrl != null && !mLoadingError) {
                Log.e(TAG, "onReceivedError: $errorCode, $description")
                mLoadingError = true
            } else {
                mOnErrorUrl = url
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (!startsWith(url, mUrl) && !mLoadingFinished) {
                if (url.contains("HTMLOUT.ru")) {
                    val uri = Uri.parse(url)
                    try {
                        val function = uri.pathSegments[0]
                        val query = uri.query
                        var parameterTypes: Array<Class<*>?> = emptyArray()
                        var parameterValues = arrayOfNulls<String>(0)
                        if (!TextUtils.isEmpty(query)) {
                            val m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(url)
                            val objs = ArrayList<String>()
                            while (m.find()) {
                                objs.add(Uri.decode(m.group(2)))
                            }
                            parameterValues = arrayOfNulls(objs.size)
                            parameterTypes = arrayOfNulls<Class<*>?>(objs.size)
                            for (i in objs.indices) {
                                parameterTypes[i] = String::class.java
                                parameterValues[i] = objs[i]
                            }
                        }
                        ThemeFragment::class.java.getMethod(function, *parameterTypes).invoke(mainActivity, *parameterValues)
                    } catch (e: Exception) {
                        AppLog.eToast(mainActivity, e)
                    }
                    return true
                }
                mUrl = null
                onPageStarted(view, url, null)
            }
            m_ScrollY = 0
            if (checkIsImage(url)) return true
            if (checkIsTheme(url)) return true
            if (checkIsPoll(url)) return true
            if (tryDeletePost(url)) return true
            if (tryQuote(url)) return true
            IntentActivity.tryShowUrl(mainActivity, handler, url, true, false,
                    if (topic == null) null else Client.getInstance().authKey)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "onPageStarted>")
            if (startsWith(url, mOnErrorUrl)) {
                mUrl = url
                mLoadingError = true
                mLoadingFinished = false
                onPageFinished(view, url)
            }
            if (mUrl == null) {
                mUrl = url
                mLoadingError = false
                mLoadingFinished = false
                view.removeCallbacks(mPageLoadingTimeoutHandlerTask)
                view.postDelayed(mPageLoadingTimeoutHandlerTask, LOADING_ERROR_TIMEOUT)
                mReference = WeakReference(view)
            }
            Log.d(TAG, "<onPageStarted")
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            Log.d(TAG, "onPageFinished>")
            if (startsWith(url, mUrl) && !mLoadingFinished) {
                mLoadingFinished = true
                view.removeCallbacks(mPageLoadingTimeoutHandlerTask)
                mOnErrorUrl = null
                mUrl = null
            } else if (mUrl == null) {
                view.webViewClient = getWebViewClient()
                mLoadingFinished = true
            }
            view.clearHistory()
            Log.d(TAG, "<onPageFinished")
            //tryScrollToElement();
        }

        private fun startsWith(str: String?, prefix: String?): Boolean {
            return str != null && prefix != null && str.startsWith(prefix)
        }

        private val mPageLoadingTimeoutHandlerTask = Runnable {
            mUrl = null
            mLoadingFinished = true
            if (mReference != null) {
                val webView = mReference!!.get()
                webView?.stopLoading()
            }
        }

        private fun checkIsImage(url: String): Boolean {
            val imagePattern = PatternExtensions.compile("\\.(png|jpg|jpeg|gif)$")
            if (!imagePattern.matcher(url).find()) return false
            if (!Client.getInstance().logined && !Client.getInstance().hasLoginCookies()) {
                Client.getInstance().showLoginForm(context)
            } else {
                // запросим список всех изображений на странице
                if (org.softeg.slartus.forpdacommon.Functions.isWebviewAllowJavascriptInterface()) {
                    webView.evalJs("requestImageAttaches('$url');")
                } else {
                    showImage(url)
                }
            }
            return true
        }

        private fun checkIsPoll(url: String): Boolean {
            val m = Pattern.compile("4pda.ru.*?addpoll=1", Pattern.CASE_INSENSITIVE).matcher(url)
            if (m.find()) {
                var uri = Uri.parse(url)
                uri = uri.buildUpon()
                        .appendQueryParameter("showtopic", topic!!.id)
                        .appendQueryParameter("st", "" + topic!!.currentPage * topic!!.getPostsPerPageCount(lastUrl))
                        .build()
                showTheme(uri.toString())
                return true
            }
            return false
        }

        private fun tryDeletePost(url: String): Boolean {
            val m = Pattern.compile("4pda.ru/forum/index.php\\?act=Mod&CODE=04&f=(\\d+)&t=(\\d+)&p=(\\d+)&st=(\\d+)&auth_key=(.*?)", Pattern.CASE_INSENSITIVE).matcher(url)
            if (m.find()) {
                prepareDeleteMessage(m.group(3))
                return true
            }
            return false
        }

        private fun tryQuote(url: String): Boolean {
            val m = Pattern.compile("4pda.ru/forum/index.php\\?act=Post&CODE=02&f=\\d+&t=\\d+&qpid=\\d+", Pattern.CASE_INSENSITIVE).matcher(url)
            if (m.find()) {
                showQuoteEditor(url)
                return true
            }
            return false
        }

    }

    private class GetThemeTask(themeFragment: ThemeFragment) : AsyncTask<String?, String?, Boolean>() {
        private var scrollY = 0
        private var m_ThemeBody: String? = null
        private var ex: Throwable? = null
        private val themeFragmentRef: WeakReference<ThemeFragment>
        private fun prepareTopicUrl(url: CharSequence?): CharSequence {
            val uri = Uri.parse(url.toString())
            return if (uri.host == null) uri.toString() else uri.query!!
        }

        override fun doInBackground(vararg forums: String?): Boolean {
            var pageBody: String? = null
            return try {
                if (isCancelled) return false
                val client = Client.getInstance()
                val themeFragment = themeFragmentRef.get()
                if (themeFragment != null && themeFragment.isAdded) {
                    themeFragment.lastUrl = forums[0]
                    themeFragment.lastUrl = "https://4pda.ru/forum/index.php?" + prepareTopicUrl(themeFragment.lastUrl)
                    if (forums.size == 1) {
                        themeFragment.lastResponse = instance.performGet("https://4pda.ru/forum/index.php?" + prepareTopicUrl(themeFragment.lastUrl))
                        pageBody = themeFragment.lastResponse!!.responseBody
                        Client.getInstance().check(pageBody)
                    } else pageBody = forums[1]
                    if (themeFragment.lastResponse != null) {
                        themeFragment.lastUrl = themeFragment.lastResponse!!.redirectUrlElseRequestUrl()
                    }
                    themeFragment.m_SpoilFirstPost = Preferences.Topic.getSpoilFirstPost()
                    val topicBodyBuilder = client.parseTopic(pageBody, App.getInstance(), themeFragment.lastUrl,
                            themeFragment.m_SpoilFirstPost)
                    themeFragment.topic = topicBodyBuilder.topic
                    themeFragment.topic!!.setLastUrl(themeFragment.lastUrl)
                    m_ThemeBody = topicBodyBuilder.body
                    topicBodyBuilder.clear()
                }
                true
            } catch (e: Throwable) {
                m_ThemeBody = pageBody
                // Log.e(ThemeActivity.getMainActivity(), e);
                ex = e
                false
            }
        }

        override fun onPreExecute() {
            try {
                val themeFragment = themeFragmentRef.get()
                if (themeFragment != null && themeFragment.isAdded) {
                    themeFragment.setLoading(true)
                    scrollY = themeFragment.m_ScrollY
                    themeFragment.hideMessagePanel()
                }
            } catch (ex: Exception) {
                AppLog.e(null, ex)
            }
        }

        override fun onPostExecute(success: Boolean) {
            val themeFragment = themeFragmentRef.get()
            if (themeFragment != null && themeFragment.isAdded) {
                themeFragment.setLoading(false)
                val item = TabsRepository.instance.getTabByTag(themeFragment.tag)
                if (item != null) {
                    val tabItem = TabsRepository.instance.getTabByTag(item.parentTag)
                    if (tabItem != null && !tabItem.tag.contains("tag")) {
                        val fragment = themeFragment.mainActivity.supportFragmentManager.findFragmentByTag(item.parentTag)
                        if (fragment is TopicsListFragment && themeFragment.topic != null && themeFragment.topic!!.id != null) fragment.topicAfterClick(themeFragment.topic!!.id)
                    }
                }
                themeFragment.m_ScrollY = scrollY
                if (themeFragment.topic != null) themeFragment.mQuickPostFragment!!.setTopic(themeFragment.topic!!.forumId, themeFragment.topic!!.id, Client.getInstance().authKey)
                if (isCancelled) return
                if (success && themeFragment.topic != null) {
                    themeFragment.addToHistory(m_ThemeBody)
                    try {
                        themeFragment.showBody(m_ThemeBody)
                    } catch (e: Exception) {
                        AppLog.e(e)
                    }
                } else {
                    if (themeFragment.topic == null) {
                        return
                    }
                    if (ex!!.javaClass != NotReportException::class.java) {
                        themeFragment.setTitle(ex!!.message)
                        themeFragment.webView.loadDataWithBaseURL(themeFragment.lastUrl, m_ThemeBody, "text/html", "UTF-8", null)
                        //webView.loadDataWithBaseURL("https://4pda.ru/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                        themeFragment.addToHistory(m_ThemeBody)
                    }
                    AppLog.e(themeFragment.mainActivity, ex) { themeFragment.showTheme(themeFragment.lastUrl) }
                }
            }
        }

        init {
            themeFragmentRef = WeakReference(themeFragment)
        }
    }

    companion object {
        private val TAG = ThemeFragment::class.java.simpleName
        private const val TOPIC_URL_KEY = "ThemeActivity.TOPIC_URL_KEY"

        @JvmField
        var LoadsImagesAutomatically: Boolean? = null

        @JvmStatic
        fun newInstance(url: String?): ThemeFragment {
            val fragment = ThemeFragment()
            val args = Bundle()
            args.putString(TOPIC_URL_KEY, url)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun getThemeUrl(topicId: CharSequence): String {
            return "https://4pda.ru/forum/index.php?showtopic=$topicId"
        }

        @JvmStatic
        fun getThemeUrl(topicId: CharSequence?, urlParams: CharSequence): String {
            return String.format("https://4pda.ru/forum/index.php?showtopic=%s%s", topicId, if (TextUtils.isEmpty(urlParams)) "" else "&$urlParams")
        }

        @JvmStatic
        fun showTopicById(topicId: CharSequence?, urlParams: CharSequence) {
            val url = getThemeUrl(topicId, urlParams)
            MainActivity.addTab(Utils.getS(R.string.theme), url, newInstance(url))
        }

        @JvmStatic
        fun showTopicById(title: CharSequence, topicId: CharSequence?, urlParams: CharSequence) {
            val url = getThemeUrl(topicId, urlParams)
            MainActivity.addTab(title.toString(), url, newInstance(url))
        }

        @JvmStatic
        fun showTopicById(topicId: CharSequence) {
            val url = getThemeUrl(topicId)
            MainActivity.addTab(Utils.getS(R.string.theme), url, newInstance(url))
        }

        @JvmStatic
        fun showImgPreview(context: FragmentActivity, title: String?, previewUrl: String?,
                           fullUrl: String?) {
            val fragment = ImageViewDialogFragment()
            val args = Bundle()
            args.putString(ImageViewDialogFragment.PREVIEW_URL_KEY, previewUrl)
            args.putString(ImageViewDialogFragment.URL_KEY, fullUrl)
            args.putString(ImageViewDialogFragment.TITLE_KEY, title)
            fragment.arguments = args
            fragment.show(context.supportFragmentManager, "dlg1")
        }
    }
}