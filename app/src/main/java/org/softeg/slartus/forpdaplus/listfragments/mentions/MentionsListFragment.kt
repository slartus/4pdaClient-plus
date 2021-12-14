package org.softeg.slartus.forpdaplus.listfragments.mentions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.afollestad.materialdialogs.MaterialDialog
import com.nineoldandroids.view.ViewPropertyAnimator
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_mentions_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaapi.IListItem
import org.softeg.slartus.forpdaapi.parsers.MentionsParser
import org.softeg.slartus.forpdaapi.vo.MentionsResult
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdaplus.*
import org.softeg.slartus.forpdaplus.classes.AdvWebView
import org.softeg.slartus.forpdaplus.classes.ForumUser
import org.softeg.slartus.forpdaplus.classes.SaveHtml
import org.softeg.slartus.forpdaplus.classes.WebViewExternals
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment
import org.softeg.slartus.forpdaplus.prefs.Preferences
import org.softeg.slartus.forpdaplus.repositories.InternetConnection
import org.softeg.slartus.hosthelper.HostHelper
import java.util.*
import java.util.regex.Pattern

class MentionsListFragment : WebViewFragment() {
    companion object {
        private const val ARG_MENTIONS_RESULT = "ARG_MENTIONS_RESULT"
        fun newFragment() = MentionsListFragment()

        private const val FILECHOOSER_RESULTCODE = 1
        val URL = "https://${HostHelper.host}/forum/index.php?act=mentions"
    }

    private val mHandler = Handler()

    private var mWebviewexternals: WebViewExternals? = null
    private var buttonsPanel: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mentionsResult = Paper.book().read(ARG_MENTIONS_RESULT, mentionsResult)

    }

    @Suppress("unused")
    @JavascriptInterface
    fun showChooseCssDialog() {
        mainActivity.runOnUiThread {
            try {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "file/*"

                // intent.setDataAndType(Uri.parseCount("file://" + lastSelectDirPath), "file/*");
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
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            val attachFilePath = FileUtils.getRealPathFromURI(mainActivity, data!!.data!!)
            val cssData = FileUtils.readFileText(attachFilePath)
                .replace("\\", "\\\\")
                .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
            if (Build.VERSION.SDK_INT < 19)
                body_webview.loadUrl("javascript:window['HtmlInParseLessContent']('$cssData');")
            else
                body_webview.evaluateJavascript(
                    "window['HtmlInParseLessContent']('$cssData')"
                ) { }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //setHideActionBar();

        load(0)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_mentions_list, container, false)

        return view
    }

    @SuppressLint("AddJavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSwipeRefreshLayout()


        btnUp.setOnClickListener { this.onBtnUpClick() }
        btnDown.setOnClickListener { this.onBtnDownClick() }
        mWebviewexternals = WebViewExternals(this)
        mWebviewexternals!!.loadPreferences(App.getInstance().preferences)
        configWebView()
        mWebviewexternals!!.setWebViewSettings()

        body_webview.settings.loadWithOverviewMode = false
        body_webview.settings.useWideViewPort = true
        body_webview.settings.defaultFontSize = Preferences.Topic.fontSize
        body_webview.addJavascriptInterface(this, "HTMLOUT")
        body_webview.loadDataWithBaseURL(
            "https://${HostHelper.host}/forum/",
            "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">" +
                    "</head><body bgcolor=" + AppTheme.currentBackgroundColorHtml + "></body></html>",
            "text/html",
            "UTF-8",
            null
        )
        registerForContextMenu(body_webview)
        buttonsPanel = findViewById(R.id.buttonsPanel) as FrameLayout
    }

    fun load(startNum: Int) {
        setLoading(true)
        InternetConnection.instance.loadDataOnInternetConnected({
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val pageBody = Client.getInstance()
                        .performGet("${URL}&st=$startNum")
                        .responseBody
                    Client.getInstance().check(pageBody)

                    val mentions = MentionsParser.instance.parseMentions(pageBody)
                    val body = MentionsHtmlBuilder(mentions).build()
                    withContext(Dispatchers.Main) {
                        setMentionsResult(mentions)
                        if (isAdded) {
                            setLoading(false)
                            showHtmlBody(body)
                        }
                    }
                    Client.getInstance().check(
                        Client.getInstance()
                            .performGet(URL)
                            .responseBody
                    )
                } catch (ex: Throwable) {
                    AppLog.e(ex)
                }
            }
        })
    }

    override fun getContext(): Context? {
        return mainActivity
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun configWebView() {

        body_webview.settings.javaScriptEnabled = true
        body_webview.settings.javaScriptCanOpenWindowsAutomatically = false
        body_webview.settings.domStorageEnabled = true
        body_webview.settings.allowFileAccess = true

        if (App.getInstance().preferences.getBoolean("system.WebViewScroll", true)) {
            body_webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            body_webview.isScrollbarFadingEnabled = false
        }


        mWebviewexternals!!.setWebViewSettings()
        body_webview.webViewClient = MyWebViewClient()
        body_webview.addJavascriptInterface(this, "HTMLOUT")

    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return mWebviewexternals!!.dispatchKeyEvent(event)
    }

    private fun showHtmlBody(body: String?) {
        try {
            body_webview.loadDataWithBaseURL(
                "https://${HostHelper.host}/forum/",
                body ?: "",
                "text/html",
                "UTF-8",
                null
            )
            if (buttonsPanel!!.translationY != 0f)
                ViewPropertyAnimator.animate(buttonsPanel)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(500)
                    .translationY(0f)
        } catch (ex: Exception) {
            AppLog.e(context, ex)
        }

    }

    @Suppress("unused")
    @JavascriptInterface
    fun showUserMenu(userId: String, userNick: String) {
        mainActivity.runOnUiThread { ForumUser.showUserQuickAction(mainActivity, userId, userNick) }
    }

    @JavascriptInterface
    override fun nextPage() {
        mainActivity.runOnUiThread { load(mentionsResult!!.getCurrentPage() * mentionsResult!!.getPostsPerPageCount()) }
    }

    @JavascriptInterface
    override fun prevPage() {
        mainActivity.runOnUiThread { load((mentionsResult!!.getCurrentPage() - 2) * mentionsResult!!.getPostsPerPageCount()) }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun firstPage() {
        mainActivity.runOnUiThread { load(0) }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun lastPage() {
        mainActivity.runOnUiThread { load((mentionsResult!!.getPagesCount() - 1) * mentionsResult!!.getPostsPerPageCount()) }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun jumpToPage() {
        mainActivity.runOnUiThread {
            val pages = arrayOfNulls<CharSequence>(mentionsResult!!.getPagesCount())

            val postsPerPage: Int = mentionsResult!!.getPostsPerPageCount()

            val page = context!!.getString(R.string.page_short)
            for (p in 0 until mentionsResult!!.getPagesCount()) {
                pages[p] =
                    page + (p + 1) + " (" + ((p * postsPerPage + 1).toString() + "-" + (p + 1) * postsPerPage) + ")"
            }

            MaterialDialog.Builder(context!!)
                .title(R.string.jump_to_page)
                .items(*pages)
                .itemsCallbackSingleChoice(mentionsResult!!.getCurrentPage() - 1) { _, _, i, _ ->
                    load(i * postsPerPage)
                    true // allow selection
                }
                .show()
        }
    }

    override fun Prefix(): String? {
        return "theme"
    }

    @JavascriptInterface
    override fun saveHtml(html: String) {
        mainActivity.runOnUiThread { SaveHtml(mainActivity, html, "search") }
    }

    override fun getWebView(): AdvWebView? {
        return body_webview
    }

    override fun getWebViewClient(): WebViewClient {
        return MyWebViewClient()
    }

    override fun getTitle(): String {
        return App.getContext().getString(R.string.search)
    }

    override fun getUrl(): String? {
        return URL
    }

    override fun reload() {
        load(0)
    }

    override fun getAsyncTask(): AsyncTask<*, *, *>? {
        return null
    }

    override fun closeTab(): Boolean {
        return false
    }

    override fun getWindow(): Window {
        assert(context != null)
        return (context as Activity).window
    }

    override fun getSupportActionBar(): ActionBar? {
        return null
    }

    override fun dispatchSuperKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    private fun onBtnUpClick() {
        body_webview.pageUp(true)
    }

    private fun onBtnDownClick() {
        body_webview.pageDown(true)
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.contains("HTMLOUT.ru")) {
                val uri = Uri.parse(url)
                try {
                    val function = uri.pathSegments[0]
                    val query = uri.query
                    var parameterTypes = arrayOfNulls<Class<*>>(0)
                    var parameterValues = arrayOfNulls<String>(0)
                    if (!TextUtils.isEmpty(query)) {
                        val m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(query!!)
                        val objs = ArrayList<String>()

                        while (m.find()) {
                            objs.add(m.group(2))
                        }
                        parameterValues = arrayOfNulls(objs.size)
                        parameterTypes = arrayOfNulls(objs.size)
                        for (i in objs.indices) {
                            parameterTypes[i] = String::class.java
                            parameterValues[i] = objs[i]
                        }
                    }
                    val method = this.javaClass.getMethod(function, *parameterTypes)

                    @Suppress("UNCHECKED_CAST")
                    method.invoke(mainActivity, *parameterValues as Array<Any>)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return true
            }

            IntentActivity.tryShowUrl(context as Activity?, mHandler, url, true, false)

            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(R.string.link)
            .setOnMenuItemClickListener {
                ExtUrl.showSelectActionDialog(mainActivity, getString(R.string.link), URL)
                true
            }
    }

    private var mentionsResult: MentionsResult? = null

    private fun setMentionsResult(mentionsResult: MentionsResult?) {
        this.mentionsResult = mentionsResult
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mentionsResult != null)
            Paper.book().write(ARG_MENTIONS_RESULT, mentionsResult)
    }
}
