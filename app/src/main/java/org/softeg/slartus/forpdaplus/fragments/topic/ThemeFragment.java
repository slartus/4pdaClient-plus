package org.softeg.slartus.forpdaplus.fragments.topic;

import static org.softeg.slartus.forpdaplus.utils.Utils.getS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.AppTheme;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.common.HelpTask;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewDialogFragment;
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer;
import org.softeg.slartus.forpdaplus.controls.quickpost.PostTask;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostFragment;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaplus.fragments.ForumFragment;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.NotesListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicReadersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicUtils;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NewsPagerBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicReadersBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo;
import org.softeg.slartus.forpdaplus.notes.NoteDialog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;
import org.softeg.slartus.forpdaplus.tabs.TabsManager;
import org.softeg.slartus.hosthelper.HostHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.paperdb.Paper;
import ru.slartus.http.AppResponse;
import ru.slartus.http.Http;
import timber.log.Timber;

@SuppressWarnings("unused")
public class ThemeFragment extends WebViewFragment implements BricksListDialogFragment.IBricksListDialogCaller, QuickPostFragment.PostSendListener {
    LinearLayout mQuickPostPanel;
    FloatingActionButton fab;
    AdvWebView webView;
    EditText txtSearch;
    LinearLayout pnlSearch;
    FrameLayout buttonsPanel;


    private static final String TAG = ThemeFragment.class.getSimpleName();
    private static final String TOPIC_URL_KEY = "ThemeActivity.TOPIC_URL_KEY";
    public static Boolean LoadsImagesAutomatically = null;


    private final Handler mHandler = new Handler();

    private String m_LastUrl;
    private AppResponse lastResponse = null;
    private ArrayList<SessionHistory> m_History = new ArrayList<>();
    private ExtTopic m_Topic;
    private Boolean m_SpoilFirstPost = true;
    // текст редактирования сообщения при переходе по страницам
    private String m_PostBody = "";
    // id сообщения к которому скроллить
    private String m_ScrollElement = null;
    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private QuickPostFragment mQuickPostFragment;

    private String lastStyle;
    private GetThemeTask asyncTask;
    private MyWebViewClient webViewClient;

    public static ThemeFragment newInstance(String url) {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString(TOPIC_URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getThemeUrl(CharSequence topicId) {
        return "https://" + HostHelper.getHost() + "/forum/index.php?showtopic=" + topicId;
    }

    public static String getThemeUrl(CharSequence topicId, CharSequence urlParams) {
        return String.format("https://" + HostHelper.getHost() + "/forum/index.php?showtopic=%s%s", topicId, TextUtils.isEmpty(urlParams) ? "" : ("&" + urlParams));
    }

    public static void showTopicById(CharSequence topicId, CharSequence urlParams) {
        String url = getThemeUrl(topicId, urlParams);
        MainActivity.addTab(getS(R.string.theme), url, newInstance(url));
    }

    public static void showTopicById(CharSequence title, CharSequence topicId, CharSequence urlParams) {
        String url = getThemeUrl(topicId, urlParams);
        MainActivity.addTab(title.toString(), url, newInstance(url));
    }

    public static void showTopicById(CharSequence topicId) {
        String url = getThemeUrl(topicId);
        MainActivity.addTab(getS(R.string.theme), url, newInstance(url));
    }

    public static void showImgPreview(final FragmentActivity context, String title, String previewUrl,
                                      final String fullUrl) {


        ImageViewDialogFragment fragment = new ImageViewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ImageViewDialogFragment.PREVIEW_URL_KEY, previewUrl);
        args.putString(ImageViewDialogFragment.URL_KEY, fullUrl);
        args.putString(ImageViewDialogFragment.TITLE_KEY, title);
        fragment.setArguments(args);
        fragment.show(context.getSupportFragmentManager(), "dlg1");

    }

    @Override
    public WebViewClient getWebViewClient() {
        if (webViewClient == null)
            webViewClient = new MyWebViewClient();
        return webViewClient;
    }

    @Override
    public String getTitle() {
        return m_Topic.getTitle();
    }

    @Override
    public String getUrl() {
        return getLastUrl();
    }

    @Override
    public void reload() {
        reloadTopic();
    }


    @Override
    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    @Override
    public boolean closeTab() {
        getPostBody();
        if (!TextUtils.isEmpty(m_PostBody)) {
            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.ConfirmTheAction)
                    .content(R.string.entered_text)
                    .positiveText(R.string.apply_yes)
                    .onPositive((dialog, which) -> {
                        clear();
                        getMainActivity().tryRemoveTab(getTag());
                    })
                    .negativeText(R.string.apply_cancel)
                    .show();
            return true;
        } else {
            clear();
            if (TabsManager.getInstance().getTabItems().size() == 1) {
                // если фрагмент с топиком - последний, то покажем главный экран
                // такое происходит, если клиент открылся по прямой ссылке на топик
                BrickInfo brickInfo = ListCore.getRegisteredBrick(Preferences.Lists.getLastSelectedList());
                if (brickInfo == null)
                    brickInfo = new NewsPagerBrickInfo();
                ((MainActivity) requireActivity()).selectItem(brickInfo);
                return true;
            }
            return false;
        }
    }

    @Override
    public String Prefix() {
        return "theme";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.theme, container, false);

        mQuickPostPanel = view.findViewById(R.id.quick_post_panel);
        fab = view.findViewById(R.id.fab);
        webView = view.findViewById(R.id.wvBody);
        txtSearch = view.findViewById(R.id.txtSearch);
        pnlSearch = view.findViewById(R.id.pnlSearch);
        buttonsPanel = view.findViewById(R.id.buttonsPanel);

        view.findViewById(R.id.btnPrevSearch).setOnClickListener(view1 -> webView.findNext(false));
        view.findViewById(R.id.btnNextSearch).setOnClickListener(view1 -> webView.findNext(true));
        view.findViewById(R.id.btnCloseSearch).setOnClickListener(view1 -> closeSearch());
        view.findViewById(R.id.btnUp).setOnClickListener(view1 -> webView.pageUp(true));
        view.findViewById(R.id.btnDown).setOnClickListener(view1 -> webView.pageDown(true));

        initSwipeRefreshLayout();
        lastStyle = AppTheme.getThemeCssFileName();
        LoadsImagesAutomatically = null;

        getMainActivity().setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);// чтобы поиск начинался при вводе текста

        mQuickPostFragment = (QuickPostFragment) getChildFragmentManager().findFragmentById(R.id.quick_post_fragment);
        mQuickPostFragment.setParentTag(getTag());


        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {
            }
        });
        hideMessagePanel();
        closeSearch();
        loadPreferences(App.getInstance().getPreferences());
        showTheme(IntentActivity.normalizeThemeUrl(getArguments().getString(TOPIC_URL_KEY)));


        if (App.getInstance().getPreferences().getBoolean("pancilInActionBar", false)) {
            fab.hide();
        } else {
            setHideFab(fab);
            setFabColors(fab);
            fab.setOnClickListener(view1 -> toggleMessagePanelVisibility());
        }
        initWebView();
        return view;
    }

    @SuppressLint("AddJavascriptInterface")
    private void initWebView() {
        registerForContextMenu(webView);
        setWebViewSettings();
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT < 18)
            webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = App.getInstance().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //webView.getSettings().setLoadWithOverviewMode(false);
        //webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        webView.setWebChromeClient(new MyChromeClient());
        /*if (getSupportActionBar() != null)
            webView.setActionBarheight(getSupportActionBar().getHeight());*/

        setHideArrows(Preferences.isHideArrows());
        webView.addJavascriptInterface(new ForPdaWebInterface(this), ForPdaWebInterface.NAME);

        webView.setActionModeListener((actionMode, callback, type) -> {
            Menu menu = actionMode.getMenu();
            menu.add(R.string.quote)
                    .setOnMenuItemClickListener(item -> {
                        webView.evalJs("htmlOutSelectionPostInfo();");
                        actionMode.finish();
                        return true;
                    })
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true);
        }
        //webView.getSettings().setJavaScriptEnabled(false);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            if (m_History != null)
                Paper.book().write("History", m_History);
            if (m_Topic != null)
                Paper.book().write("Topic", m_Topic);
            //outState.putSerializable("History", m_History);
            //outState.putSerializable("Topic", m_Topic);
            //webView.saveState(outState);
            outState.putString("LastUrl", getLastUrl());
            outState.putString("ScrollElement", m_ScrollElement);
            outState.putString("LastStyle", lastStyle);
            outState.putBoolean("FromHistory", m_FromHistory);

            outState.putString("LoadsImagesAutomatically", LoadsImagesAutomatically == null ? "null" : (LoadsImagesAutomatically ? "1" : "0"));
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) return;

        try {
            m_Topic = Paper.book().read("Topic", null);
            if (m_Topic != null)
                mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), Client.getInstance().getAuthKey());
            m_LastUrl = savedInstanceState.getString("LastUrl");
            if (m_Topic != null)
                m_Topic.setLastUrl(m_LastUrl);
            m_ScrollElement = savedInstanceState.getString("ScrollElement");

            m_FromHistory = savedInstanceState.getBoolean("FromHistory");

            String sLoadsImagesAutomatically = savedInstanceState.getString("LoadsImagesAutomatically");
            LoadsImagesAutomatically = "null".equals(sLoadsImagesAutomatically) ? null : Boolean.parseBoolean(sLoadsImagesAutomatically);


            loadPreferences(App.getInstance().getPreferences());
            m_History = Paper.book().read("History", m_History);
            assert m_History != null;
            if (m_History.size() > 0) {
                SessionHistory sessionHistory = m_History.get(m_History.size() - 1);
                m_ScrollY = sessionHistory.getY();
                m_LastUrl = sessionHistory.getUrl();
                m_Topic = sessionHistory.getTopic();
                if (m_Topic != null)
                    m_Topic.setLastUrl(m_LastUrl);

                if (m_Topic != null)
                    mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), Client.getInstance().getAuthKey());
                if (sessionHistory.getBody() == null) {
                    showTheme(sessionHistory.getUrl());
                } else {
                    String body = sessionHistory.getBody().replace(savedInstanceState.getString("LastStyle"),
                            AppTheme.getThemeCssFileName());
                    showBody(body);
                    sessionHistory.setBody(body);
                }
            }


        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu();
        boolean pancil = App.getInstance().getPreferences().getBoolean("pancilInActionBar", false);
        menu.findItem(R.id.new_post_item).setVisible(pancil);
        boolean onTopicReadersAndWriters = Preferences.Topic.getReadersAndWriters();
        menu.findItem(R.id.topic_readers_item).setVisible(!onTopicReadersAndWriters);
        menu.findItem(R.id.topic_writers_item).setVisible(!onTopicReadersAndWriters);
        menu.findItem(R.id.avatars_item).setTitle(String.format(getS(R.string.avatars), App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()]));
        menu.findItem(R.id.hide_pencil_item).setVisible(!pancil);
        menu.findItem(R.id.hide_pencil_item).setChecked(Preferences.isHideFab());
        menu.findItem(R.id.hide_arrows_item).setChecked(Preferences.isHideArrows());
        menu.findItem(R.id.loading_img_for_session_item).setChecked(getLoadsImagesAutomatically());
        menu.findItem(R.id.multi_moderation_item).setVisible(Preferences.System.isCurator());
        menu.findItem(R.id.search_item).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ExtTopic topic = getTopic();
        try {
            Bundle args = new Bundle();
            switch (id) {
                case R.id.new_post_item:
                    toggleMessagePanelVisibility();
                    return true;
                case R.id.refresh_item:
                    reloadTopic();
                    return true;
                case R.id.page_attaches_item:
                    showPageAttaches();
                    return true;
                case R.id.topic_attaches_item:
                    showTopicAttaches();
                    return true;
                case R.id.page_search_item:
                    onSearchRequested();
                    return true;
                case R.id.topic_search_item:
                    if (topic != null) {
                        SearchSettingsDialogFragment.showSearchSettingsDialog(getMainActivity(),
                                SearchSettingsDialogFragment.createTopicSearchSettings(topic.getId()));
                    }
                    return true;
                case R.id.add_to_favorites_item:
                    try {
                        if (topic != null) {
                            TopicUtils.showSubscribeSelectTypeDialog(getContext(), mHandler, topic, null);
                        }
                    } catch (Exception ex) {
                        AppLog.e(getContext(), ex);
                    }
                    return true;
                case R.id.del_from_favorites_item:
                    if (topic != null) {
                        final HelpTask helpTask = new HelpTask(getContext(), getContext().getString(R.string.DeletingFromFavorites));
                        helpTask.setOnPostMethod(param -> {
                            if (helpTask.Success)
                                Toast.makeText(getContext(), (String) param, Toast.LENGTH_SHORT).show();
                            else
                                AppLog.e(getContext(), helpTask.ex);
                            return null;
                        });
                        helpTask.execute(param -> TopicApi.deleteFromFavorites(Client.getInstance(), topic.getId()));
                    }
                    return true;
                case R.id.open_topic_forum_item:
                    if (topic != null) {
                        ForumFragment.Companion.showActivity(topic.getForumId(), topic.getId());
                    }
                    return true;
                case R.id.topic_notes_item:
                    if (topic != null) {
                        args.putString(NotesListFragment.TOPIC_ID_KEY, topic.getId());
                        MainActivity.showListFragment(new NotesBrickInfo().getName(), args);
                    }
                    return true;
                case R.id.topic_readers_item:
                    if (topic != null) {
                        args.putString(TopicReadersListFragment.TOPIC_ID_KEY, topic.getId());
                        MainActivity.showListFragment(topic.getId(), TopicReadersBrickInfo.NAME, args);
                    }
                    return true;
                case R.id.topic_writers_item:
                    if (topic != null) {
                        args.putString(TopicWritersListFragment.TOPIC_ID_KEY, topic.getId());
                        MainActivity.showListFragment(topic.getId(), TopicWritersBrickInfo.NAME, args);
                    }
                    return true;
                case R.id.link_item:
                    if (topic != null) {
                        ExtUrl.showSelectActionDialog(getMainActivity(), getS(R.string.link),
                                TextUtils.isEmpty(getLastUrl()) ? ("https://" + HostHelper.getHost() + "/forum/index.php?showtopic=" + topic.getId()) : getLastUrl());
                    }
                    return true;
                case R.id.avatars_item:
                    String[] avatars = App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles);
                    new MaterialDialog.Builder(getMainActivity())
                            .title(R.string.show_avatars)
                            .cancelable(true)
                            .items(avatars)
                            .itemsCallbackSingleChoice(Preferences.Topic.getShowAvatarsOpt(), (dialog, view1, i, avatars1) -> {
                                Preferences.Topic.setShowAvatarsOpt(i);
                                getActivity().invalidateOptionsMenu();
                                return true; // allow selection
                            })
                            .show();
                    return true;
                case R.id.hide_pencil_item:
                    Preferences.setHideFab(!Preferences.isHideFab());
                    setHideFab(fab);
                    getActivity().invalidateOptionsMenu();
                    return true;
                case R.id.hide_arrows_item:
                    Preferences.setHideArrows(!Preferences.isHideArrows());
                    setHideArrows(Preferences.isHideArrows());
                    getActivity().invalidateOptionsMenu();
                    return true;
                case R.id.loading_img_for_session_item:
                    boolean loadImagesAutomatically1 = getLoadsImagesAutomatically();
                    setLoadsImagesAutomatically(!loadImagesAutomatically1);
                    return true;
                case R.id.font_size_item:
                    showFontSizeDialog();
                    return true;
                case R.id.topic_style_item:
                    showStylesDialog(App.getInstance().getPreferences());
                    return true;
                case R.id.multi_moderation_item:
                    if (topic != null) {
                        ThemeCurator.showMmodDialog(getActivity(), ThemeFragment.this, getTopic().getId());
                    }
                    return true;

            }
        } catch (Exception ex) {
            AppLog.e(getContext(), ex);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        if (inflater != null && menu.findItem(R.id.new_post_item) == null)
            inflater.inflate(R.menu.topic, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_Topic != null) {
            setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());
        }
    }


    private void hideKeyboard() {
        mQuickPostFragment.hideKeyboard();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        mQuickPostFragment.hidePopupWindow();
    }

    public Handler getHandler() {
        return mHandler;
    }

    public ExtTopic getTopic() {
        return m_Topic;
    }

    public String getLastUrl() {
        return m_LastUrl;
    }

    public void onSearchRequested() {
        hideMessagePanel();
        pnlSearch.setVisibility(View.VISIBLE);
    }

    protected void showQuoteEditor(String url) {
        DialogFragment quoteEditorDialogFragment = ThemeQuoteEditor
                .newInstance(url, getTag());
        quoteEditorDialogFragment.show(getChildFragmentManager(), "dialog");
    }

    public void checkBodyAndReload(String body) {
        if (TextUtils.isEmpty(body)) {
            reloadTopic();
        }
    }

    @JavascriptInterface
    public void checkBodyAndReload() {
        try {
            webView.evalJs("window.HTMLOUT.checkBodyAndReload(document.getElementsByTagName('body')[0].innerHTML);");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void showPageAttaches() {
        try {
            webView.evalJs("window.HTMLOUT.showTopicAttaches('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void showTopicAttaches() {
        if (m_Topic == null)
            return;
        TopicAttachmentListFragment.showActivity(m_Topic.getId());
    }

    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        webView.findAllAsync(query);
        try {
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(webView, true);
        } catch (Throwable ignored) {
        }
        //onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(() -> {
            webView.findAllAsync("");

            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                m.invoke(webView, false);
            } catch (Throwable ignored) {
            }

            pnlSearch.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getMainActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
        });
    }

    @Override
    protected void loadPreferences(SharedPreferences prefs) {
        super.loadPreferences(prefs);
        LoadsImagesAutomatically = WebViewExternals.isLoadImages("theme");
        m_SpoilFirstPost = Preferences.Topic.getSpoilFirstPost();
    }

    public void showLinkMenu(final String link) {
        showLinkMenu(link, "");
    }

    public void showLinkMenu(final String link, String postId) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, getMainActivity(), m_Topic.getTitle(), "", link, m_Topic.getId(),
                m_Topic.getTitle(), postId, "", "");

    }

    private boolean tryCloseSearch() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return true;
        }
        return false;
    }

    private boolean tryBackByHistory() {
        if (m_History.size() > 1) {
            m_History.remove(m_History.size() - 1);
            SessionHistory sessionHistory = m_History.get(m_History.size() - 1);
            m_ScrollY = sessionHistory.getY();
            if (sessionHistory.getBody() == null) {
                m_History.remove(m_History.size() - 1);
                showTheme(sessionHistory.getUrl());
            } else {
                m_LastUrl = sessionHistory.getUrl();
                m_Topic = sessionHistory.getTopic();
                if (m_Topic != null)
                    m_Topic.setLastUrl(m_LastUrl);
                if (m_Topic != null)
                    mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), Client.getInstance().getAuthKey());
                try {
                    showBody(sessionHistory.getBody());
                } catch (Exception e) {
                    AppLog.e(e);
                }
            }
            return true;
        }
        return false;
    }

    private boolean trySkipPostBodyChanges() {
        getPostBody();
        if (!TextUtils.isEmpty(m_PostBody)) {
            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.ConfirmTheAction)
                    .content(R.string.entered_text)
                    .positiveText(R.string.apply_yes)
                    .onPositive((dialog, which) -> {
                        clear();
                        getMainActivity().tryRemoveTab(getTag());
                    })
                    .negativeText(R.string.apply_cancel)
                    .show();
            return true;
        } else {
            clear();
            return false;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (tryCloseSearch()) {
            return true;
        }

        if (tryBackByHistory()) {
            return true;
        }

        if (trySkipPostBodyChanges()) {
            return true;
        }

        clear();

        return false;
    }

    public void clear() {
        clear(false);
    }

    public void clear(Boolean clearChache) {
        webView.setWebViewClient(null);
        webView.loadData("<html><head></head><body bgcolor=" + AppTheme.getCurrentBackgroundColorHtml() + "></body></html>", "text/html", "UTF-8");
        if (clearChache)
            webView.clearCache(true);
        if (m_Topic != null)
            m_Topic.dispose();
        m_Topic = null;
    }

    public String getPostBody() {
        m_PostBody = mQuickPostFragment.getPostBody();
        return m_PostBody;
    }

    @JavascriptInterface
    public void setPostBody(String postBody) {
        m_PostBody = postBody;
    }

    public void insertQuote(CharSequence postId, CharSequence postDate, CharSequence userNick, CharSequence text) {
        String endQuote = "\n[/quote]";
        final String fullQuoteText = "[quote name=\"" + userNick + "\" date=\"" + postDate + "\" post=\"" + postId + "\"]\n" + text + endQuote;
        int selectedIndex = -1;//fullQuoteText.length() - endQuote.length(); //наверное , лучше курсор в конец всё же ставить
        getMainActivity().runOnUiThread(() -> new Handler().post(() ->
                insertTextToPost(fullQuoteText, selectedIndex)));
    }

    @JavascriptInterface
    public void quote(final String forumId, final String topicId, final String postId, final String postDate, String userId, String userNick) {
        final String finalPostDate = Functions.getForumDateTime(Functions.parseForumDateTime(postDate, Functions.getToday(), Functions.getYesterToday()));
        final String mUserNick = userNick.replace("\"", "\\\"");
        CharSequence clipboardText = StringUtils.fromClipboard(App.getContext());
        if (TextUtils.isEmpty(clipboardText)) {
            insertQuote(postId, finalPostDate, mUserNick, "");
            return;
        }

        CharSequence[] titles = new CharSequence[]{getS(R.string.blank_quote), getS(R.string.quote_from_buffer)};
        final CharSequence finalClipboardText = clipboardText;
        new MaterialDialog.Builder(requireContext())
                .title(R.string.quote)
                .cancelable(true)
                .items(titles)
                .itemsCallback((dialog, view1, i, titles1) -> {
                    switch (i) {
                        case 0:
                            insertQuote(postId, finalPostDate, mUserNick, "");
                            break;
                        case 1:
                            insertQuote(postId, finalPostDate, mUserNick, finalClipboardText);
                            break;
                    }
                })
                .show();
    }

    public void openActionMenu(final String postId, final String postDate,
                               final String userId, final String userNick,
                               final Boolean canEdit, final Boolean canDelete) {
        try {
            final List<MenuListDialog> list = new ArrayList<>();

            if (Client.getInstance().getLogined()) {
                list.add(new MenuListDialog(getS(R.string.url_post), () -> showLinkMenu(Post.getLink(m_Topic.getId(), postId), postId)));
                list.add(new MenuListDialog(getS(R.string.report_msg), () -> Post.claim(getMainActivity(), mHandler, m_Topic.getId(), postId)));
                if (canEdit) {
                    list.add(new MenuListDialog(getS(R.string.edit_post), () ->
                            EditPostFragment.Companion.editPost(getMainActivity(), m_Topic.getForumId(), m_Topic.getId(), postId, Client.getInstance().getAuthKey(), getTag())));
                }
                if (canDelete) {
                    list.add(new MenuListDialog(getS(R.string.delete_post), () -> prepareDeleteMessage(postId)));
                }
                list.add(new MenuListDialog(getS(R.string.quote_post), () -> quote(m_Topic.getForumId(), m_Topic.getId(), postId, postDate, userId, userNick)));
            }
            list.add(new MenuListDialog(getS(R.string.create_note), () -> NoteDialog.showDialog(mHandler, getMainActivity(), m_Topic.getTitle(), null,
                    "https://" + HostHelper.getHost() + "/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + postId,
                    m_Topic.getId(), m_Topic.getTitle(), postId, null, null)));

            ExtUrl.showContextDialog(getContext(), null, list);
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private void showBody(String body) throws Exception {
        super.showBody();
        try {
            setScrollElement();
            setTitle(m_Topic.getTitle());
            if (getSupportActionBar() != null)
                setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());

            //webView.loadDataWithBaseURL(m_LastUrl, body, "text/html", "UTF-8", null);
            webView.loadDataWithBaseURL("https://" + HostHelper.getHost() + "/forum/", body, "text/html", "UTF-8", null);

            TopicsHistoryTable.addHistory(m_Topic, m_LastUrl);
            if (buttonsPanel.getTranslationY() != 0)
                ViewPropertyAnimator.animate(buttonsPanel)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(500)
                        .translationY(0);
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void showMessagePanel() {
        fab.setImageResource(R.drawable.close_white);
        pnlSearch.setVisibility(View.GONE);
        mQuickPostPanel.setVisibility(View.VISIBLE);
        mQuickPostPanel.setEnabled(Client.getInstance().getLogined());
        mQuickPostFragment.showKeyboard();
    }

    public void hideMessagePanel() {
        fab.setImageResource(R.drawable.pencil);
        mQuickPostPanel.setVisibility(View.GONE);
        mQuickPostFragment.hidePopupWindow();
        hideKeyboard();
    }

    public void toggleMessagePanelVisibility() {
        if (!Client.getInstance().getLogined()) {
            Toast.makeText(getMainActivity(), R.string.NeedToLogin, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mQuickPostPanel.getVisibility() == View.GONE)
            showMessagePanel();
        else
            hideMessagePanel();

    }

    public boolean getLoadsImagesAutomatically() {
        return WebViewExternals.isLoadImages("theme");
    }

    public void setLoadsImagesAutomatically(boolean loadsImagesAutomatically) {
        LoadsImagesAutomatically = loadsImagesAutomatically;
        new MaterialDialog.Builder(getMainActivity())
                .title(R.string.select_action)
                .content(R.string.refresh_page)
                .positiveText(R.string.refresh_p)
                .onPositive((dialog, which) -> reloadTopic())
                .negativeText(R.string.apply_no)
                .show();
    }

    @Override
    public FragmentManager getSupportFragmentManager() {
        return getChildFragmentManager();
    }

    @Override
    public void onBricksListDialogResult(DialogInterface dialog, String dialogId, BrickInfo brickInfo, Bundle args) {
        dialog.dismiss();
        MainActivity.showListFragment(brickInfo.getName(), args);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditPostFragment.Companion.getNEW_EDIT_POST_REQUEST_CODE()) {
            if (resultCode == Activity.RESULT_OK) {
                String url = data.getStringExtra(EditPostFragment.POST_URL_KEY);
                assert url != null;
                mQuickPostFragment.clearPostBody();

                closeSearch();

                asyncTask = new GetThemeTask(this);
                if (data.getExtras() != null && data.getExtras().containsKey(EditPostFragment.TOPIC_BODY_KEY)) {
                    asyncTask.execute(url.replace("|", ""), data.getStringExtra(EditPostFragment.TOPIC_BODY_KEY));
                } else
                    asyncTask.execute(url.replace("|", ""));
            }
        }
    }

    private boolean checkIsTheme(String url) {

        url = IntentActivity.normalizeThemeUrl(url);

        String[] patterns = {
                "(/+" + HostHelper.getHost() + "/+forum/+index.php\\?.*?showtopic=[^\"]*)",
                "(/+" + HostHelper.getHost() + "/+forum/+index.php\\?.*?act=findpost&pid=\\d+[^\"]*?)$",
                "(/+" + HostHelper.getHost() + "/+index.php\\?.*?act=findpost&pid=\\d+[^\"]*?)$"
        };


        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern).matcher(url);
            if (m.find()) {
                goToAnchorOrLoadTopic(m.group(1));
                return true;
            }
        }

        return false;
    }

    public void reloadTopic() {
        m_ScrollY = webView.getScrollY();
        showTheme(getLastUrl());
    }

    public void goToAnchorOrLoadTopic(final String topicUrl) {
        try {
            if (getTopic() == null || m_History.size() == 0) {
                showTheme(topicUrl);
                return;
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
            }*/

            showTheme(topicUrl);
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }

    }

    private String lofiversionToNormal(String url) {
        if (url == null)
            return null;
        Matcher m = Pattern.compile("lofiversion/index.php\\?t(\\d+)(?:-(\\d+))?.html", Pattern.CASE_INSENSITIVE)
                .matcher(url);
        if (m.find())
            return "https://" + HostHelper.getHost() + "/forum/index.php?showtopic=" + m.group(1) +
                    (m.group(2) != null ? ("&st=" + m.group(2)) : "");
        return url;
    }

    public void showTheme(String url, boolean clearText) {
        if (clearText) mQuickPostFragment.clearPostBody();
        showTheme(url);
    }

    public void showTheme(String url) {
        try {
            closeSearch();
            if (url == null) {
                Toast.makeText(getMainActivity(), R.string.blank_url, Toast.LENGTH_SHORT).show();
                return;
            }
            url = lofiversionToNormal(url);
            if (m_History.size() > 0) {
                m_History.get(m_History.size() - 1).setY(webView.getScrollY());
                webView.evalJs("window.HTMLOUT.setHistoryBody(" + (m_History.size() - 1) + ",'<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
            webView.setWebViewClient(getWebViewClient());

            asyncTask = new GetThemeTask(this);
            asyncTask.execute(url.replace("|", ""));
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void setHistoryBody(int index, String body) {
        if (index > m_History.size()) {
            addToHistory(body);
        } else {
            m_History.get(index).setBody(body);
        }
    }

    public AdvWebView getWebView() {
        return webView;
    }

    private void prepareDeleteMessage(final String postId) {
        new MaterialDialog.Builder(getMainActivity())
                .title(R.string.ConfirmTheAction)
                .content(R.string.want_to_delete_msg)
                .positiveText(R.string.delete_m)
                .onPositive((dialog, which) -> deleteMessage(postId))
                .negativeText(R.string.apply_cancel)
                .show();
    }

    private void deleteMessage(final String postId) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getMainActivity())
                .progress(true, 0)
                .cancelable(false)
                .content(R.string.deleting_msg)
                .show();
        new Thread(() -> {
            Throwable ex = null;

            try {
                Post.delete(postId, Client.getInstance().getAuthKey());
            } catch (Throwable e) {
                ex = e;
            }

            final Throwable finalEx = ex;
            mHandler.post(() -> {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } catch (Throwable ignored) {

                }
                if (finalEx != null)
                    AppLog.e(getMainActivity(), finalEx);

                m_ScrollY = 0;
                //showTheme(getLastUrl());
                getWebView().evalJs("document.querySelector('div[name*=del" + postId + "]').remove();");
            });
        }).start();

    }

    public void showRep(final String userId) {
        UserReputationFragment.showActivity(userId, false);
    }

    public void insertTextToPost(final String text, Integer cursorPosition) {
        mQuickPostFragment.insertTextToPost(text, cursorPosition);
        showMessagePanel();
    }

    public void post() {
        mQuickPostFragment.post();
    }

    public void nextPage() {
        m_ScrollY = 0;
        showTheme("showtopic=" + m_Topic.getId() + "&st=" + m_Topic.getCurrentPage() * m_Topic.getPostsPerPageCount(getLastUrl()));
    }

    public void prevPage() {
        m_ScrollY = 0;
        showTheme("showtopic=" + m_Topic.getId() + "&st=" + (m_Topic.getCurrentPage() - 2) * m_Topic.getPostsPerPageCount(getLastUrl()));
    }

    public void firstPage() {
        m_ScrollY = 0;
        showTheme("showtopic=" + m_Topic.getId());
    }

    public void lastPage() {
        m_ScrollY = 0;
        showTheme("showtopic=" + m_Topic.getId() + "&st=" + (m_Topic.getPagesCount() - 1) * m_Topic.getPostsPerPageCount(getLastUrl()));
    }

    public void openFromSt(int st) {
        showTheme("showtopic=" + m_Topic.getId() + "&st=" + st);
    }

    protected void showChangeRep(final String postId, String userId, String userNick, final String type, String title) {
        ForumUser.startChangeRep(getMainActivity(), mHandler, userId, userNick, postId, type, title);
    }

    private void addToHistory(String topicBody) {
        int historyLimit = Preferences.Topic.getHistoryLimit();
        if (m_History.size() >= historyLimit && m_History.size() > 0)
            m_History.get(m_History.size() - historyLimit).setBody(null);
        m_History.add(new SessionHistory(m_Topic, m_LastUrl, topicBody, 0));
    }

    private void setScrollElement() {
        m_ScrollElement = null;

        String url = getLastUrl();
        if (url != null) {
            Pattern p = Pattern.compile("#(\\w+\\d+)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                m_ScrollElement = m.group(1);
            }
        }
    }

    private boolean calledScroll = false;

    private void tryScrollToElement() {
        if (calledScroll)
            return;

        calledScroll = true;
        mHandler.postDelayed(() -> {
            if (m_ScrollY != 0) {
                webView.setScrollY(m_ScrollY);
            } else if (!TextUtils.isEmpty(m_ScrollElement)) {
                webView.evalJs("scrollToElement('" + m_ScrollElement + "');");
            }
            calledScroll = false;
        }, 250);
    }

    @Override
    public void onAfterSendPost(@Nullable PostTask.PostResult postResult) {
        if (postResult == null) return;
        if (postResult.Success) {
            hideMessagePanel();

            m_LastUrl = postResult.Response.redirectUrlElseRequestUrl();
            m_Topic = postResult.ExtTopic;
            m_Topic.setLastUrl(m_LastUrl);

            if (postResult.TopicBody == null)
                Timber.w("TopicBody is null");
            addToHistory(postResult.TopicBody);
            try {
                showBody(postResult.TopicBody);
            } catch (Exception e) {
                AppLog.e(e);
            }

        } else {
            if (postResult.Exception != null)
                AppLog.e(getMainActivity(), postResult.Exception, () -> mQuickPostFragment.post());
            else if (!TextUtils.isEmpty(postResult.ForumErrorMessage))
                if (getContext() != null)
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.forum_msg)
                            .content(postResult.ForumErrorMessage)
                            .show();
        }
    }

    private static class MyChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            //if (newProgress >= 10 && m_ScrollElement != null && m_ScrollY == 0) ;
            //tryScrollToElement();
        }

    }

    public List<ArrayList<String>> imageAttaches = new ArrayList<>();

    public void showImage(String url) {
        Pattern tPattern = Pattern.compile("(post/\\d*?/[\\s\\S]*?\\.(?:png|jpg|jpeg|gif))", Pattern.CASE_INSENSITIVE);
        Matcher target = tPattern.matcher(url);
        Matcher temp;
        String id;
        if (target.find()) {
            id = target.group(1);
            for (ArrayList<String> list : imageAttaches) {
                for (int i = 0; i < list.size(); i++) {
                    temp = tPattern.matcher(list.get(i));
                    if (temp.find()) {
                        if (temp.group(1).equals(id)) {
                            ImgViewer.startActivity(getContext(), list, i);
                            return;
                        }
                    }
                }
            }
            ImgViewer.startActivity(getContext(), url);
        }

    }

    private class MyWebViewClient extends WebViewClient {
        public MyWebViewClient() {

        }

        private final long LOADING_ERROR_TIMEOUT = TimeUnit.SECONDS.toMillis(45);

        // WebView instance is kept in WeakReference because of mPageLoadingTimeoutHandlerTask
        private WeakReference<WebView> mReference;
        private boolean mLoadingFinished = false;
        private boolean mLoadingError = false;
        private String mOnErrorUrl;

        // Helps to know what page is loading in the moment
        // Allows check url to prevent onReceivedError/onPageFinished calling for wrong url
        // Helps to prevent double call of onPageStarted
        // These problems cached on many devices
        private String mUrl;

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            if (mUrl != null && !mLoadingError) {
                mLoadingError = true;
            } else {
                mOnErrorUrl = url;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!startsWith(url, mUrl) && !mLoadingFinished) {
                if (url.contains("HTMLOUT.ru")) {
                    Uri uri = Uri.parse(url);
                    try {
                        String function = uri.getPathSegments().get(0);
                        String query = uri.getQuery();
                        @SuppressWarnings("rawtypes")
                        Class[] parameterTypes = null;
                        String[] parameterValues = new String[0];
                        if (!TextUtils.isEmpty(query)) {
                            Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(url);
                            ArrayList<String> objs = new ArrayList<>();

                            while (m.find()) {
                                objs.add(Uri.decode(m.group(2)));
                            }
                            parameterValues = new String[objs.size()];

                            parameterTypes = new Class[objs.size()];
                            for (int i = 0; i < objs.size(); i++) {
                                parameterTypes[i] = String.class;
                                parameterValues[i] = objs.get(i);
                            }
                        }
                        ThemeFragment.class.getMethod(function, parameterTypes).invoke(getMainActivity(), parameterValues);
                    } catch (Exception e) {
                        AppLog.eToast(getMainActivity(), e);
                    }
                    return true;
                }
                mUrl = null;
                onPageStarted(view, url, null);
            }
            m_ScrollY = 0;

            if (checkIsImage(url))
                return true;

            if (checkIsTheme(url))
                return true;

            if (checkIsPoll(url))
                return true;

            if (tryDeletePost(url))
                return true;

            if (tryQuote(url))
                return true;

            IntentActivity.tryShowUrl(getMainActivity(), mHandler, url, true, false,
                    m_Topic == null ? null : Client.getInstance().getAuthKey());
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (startsWith(url, mOnErrorUrl)) {
                mUrl = url;
                mLoadingError = true;
                mLoadingFinished = false;
                onPageFinished(view, url);
            }
            if (mUrl == null) {
                mUrl = url;
                mLoadingError = false;
                mLoadingFinished = false;
                view.removeCallbacks(mPageLoadingTimeoutHandlerTask);
                view.postDelayed(mPageLoadingTimeoutHandlerTask, LOADING_ERROR_TIMEOUT);
                mReference = new WeakReference<>(view);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (startsWith(url, mUrl) && !mLoadingFinished) {
                mLoadingFinished = true;
                view.removeCallbacks(mPageLoadingTimeoutHandlerTask);
                mOnErrorUrl = null;
                mUrl = null;
            } else if (mUrl == null) {
                view.setWebViewClient(getWebViewClient());
                mLoadingFinished = true;
            }
            view.clearHistory();
        }

        private boolean startsWith(String str, String prefix) {
            return str != null && prefix != null && str.startsWith(prefix);
        }

        private final Runnable mPageLoadingTimeoutHandlerTask = new Runnable() {
            @Override
            public void run() {
                mUrl = null;
                mLoadingFinished = true;
                if (mReference != null) {
                    WebView webView = mReference.get();
                    if (webView != null) {
                        webView.stopLoading();
                    }
                }
            }
        };

        private boolean checkIsImage(final String url) {
            final Pattern imagePattern = PatternExtensions.compile("\\.(png|jpg|jpeg|gif)$");
            if (!imagePattern.matcher(url).find()) return false;
            if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
                Client.getInstance().showLoginForm(getContext());
            } else {
                // запросим список всех изображений на странице
                if (org.softeg.slartus.forpdacommon.Functions.isWebviewAllowJavascriptInterface()) {
                    webView.evalJs("requestImageAttaches('" + url + "');");
                } else {
                    showImage(url);
                }
            }
            return true;
        }


        private boolean checkIsPoll(String url) {
            Matcher m = Pattern.compile(HostHelper.getHost() + ".*?addpoll=1", Pattern.CASE_INSENSITIVE).matcher(url);
            if (m.find()) {
                Uri uri = Uri.parse(url);
                uri = uri.buildUpon()
                        .appendQueryParameter("showtopic", getTopic().getId())
                        .appendQueryParameter("st", "" + getTopic().getCurrentPage() * getTopic().getPostsPerPageCount(m_LastUrl))
                        .build();
                showTheme(uri.toString());
                return true;
            }
            return false;
        }

        private boolean tryDeletePost(String url) {

            Matcher m = Pattern.compile(HostHelper.getHost() + "/forum/index.php\\?act=Mod&CODE=04&f=(\\d+)&t=(\\d+)&p=(\\d+)&st=(\\d+)&auth_key=(.*?)", Pattern.CASE_INSENSITIVE).matcher(url);
            if (m.find()) {

                prepareDeleteMessage(m.group(3));
                return true;
            }
            return false;
        }

        private boolean tryQuote(final String url) {

            Matcher m = Pattern.compile(HostHelper.getHost() + "/forum/index.php\\?act=Post&CODE=02&f=\\d+&t=\\d+&qpid=\\d+", Pattern.CASE_INSENSITIVE).matcher(url);
            if (m.find()) {
                showQuoteEditor(url);
                return true;
            }
            return false;
        }
    }

    private static class GetThemeTask extends AsyncTask<String, String, Boolean> {
        private int scrollY = 0;
        private String m_ThemeBody;
        private Throwable ex;
        private final WeakReference<ThemeFragment> themeFragmentRef;

        GetThemeTask(ThemeFragment themeFragment) {
            this.themeFragmentRef = new WeakReference<>(themeFragment);
        }

        private CharSequence prepareTopicUrl(CharSequence url) {
            final Uri uri = Uri.parse(url.toString());
            return uri.getHost() == null ? uri.toString() : uri.getQuery();
        }

        @Override
        protected Boolean doInBackground(String... forums) {
            String pageBody = null;
            try {
                if (isCancelled()) return false;

                Client client = Client.getInstance();
                ThemeFragment themeFragment = themeFragmentRef.get();
                if (themeFragment != null && themeFragment.isAdded()) {
                    themeFragment.m_LastUrl = forums[0];
                    themeFragment.m_LastUrl = "https://" + HostHelper.getHost() + "/forum/index.php?" + prepareTopicUrl(themeFragment.m_LastUrl);

                    if (forums.length == 1) {
                        themeFragment.lastResponse = Http.Companion.getInstance().performGet("https://" + HostHelper.getHost() + "/forum/index.php?" + prepareTopicUrl(themeFragment.m_LastUrl));
                        //pageBody = ResourcesUtilKt.loadAssetsText(themeFragment.getContext(), "1.htm", "windows-1251");
                        pageBody = themeFragment.lastResponse.getResponseBody();
                        Client.getInstance().check(pageBody);
                    } else
                        pageBody = forums[1];
                    if (themeFragment.lastResponse != null) {
                        themeFragment.m_LastUrl = themeFragment.lastResponse.redirectUrlElseRequestUrl();
                    }
                    themeFragment.m_SpoilFirstPost = Preferences.Topic.getSpoilFirstPost();
                    TopicBodyBuilder topicBodyBuilder = client.parseTopic(pageBody, App.getInstance(), themeFragment.m_LastUrl,
                            themeFragment.m_SpoilFirstPost);
                    themeFragment.m_Topic = topicBodyBuilder.getTopic();
                    themeFragment.m_Topic.setLastUrl(themeFragment.m_LastUrl);
                    m_ThemeBody = topicBodyBuilder.getBody();
                    topicBodyBuilder.clear();
                }
                return true;
            } catch (Throwable e) {
                m_ThemeBody = pageBody;
                // Log.e(ThemeActivity.getMainActivity(), e);
                ex = e;
                return false;
            }
        }


        protected void onPreExecute() {
            try {
                ThemeFragment themeFragment = themeFragmentRef.get();
                if (themeFragment != null && themeFragment.isAdded()) {
                    themeFragment.setLoading(true);
                    scrollY = themeFragment.m_ScrollY;
                    themeFragment.hideMessagePanel();
                }
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
        }

        protected void onPostExecute(final Boolean success) {
            ThemeFragment themeFragment = themeFragmentRef.get();
            if (ex != null) {
                AppLog.e(themeFragment.getContext(), ex);
            }
            if (themeFragment != null && themeFragment.isAdded()) {
                themeFragment.setLoading(false);
                TabItem item = TabsManager.getInstance().getTabByTag(themeFragment.getTag());
                if (item != null) {
                    TabItem tabItem = TabsManager.getInstance().getTabByTag(item.getParentTag());
                    if (tabItem != null && !tabItem.getTag().contains("tag")) {
                        Fragment fragment = themeFragment.getMainActivity().getSupportFragmentManager().findFragmentByTag(item.getParentTag());
                        if (fragment instanceof TopicsListFragment && themeFragment.getTopic() != null && themeFragment.getTopic().getId() != null)
                            ((TopicsListFragment) fragment).topicAfterClick(themeFragment.getTopic().getId());
                    }
                }

                themeFragment.m_ScrollY = scrollY;
                if (themeFragment.m_Topic != null)
                    themeFragment.mQuickPostFragment.setTopic(themeFragment.m_Topic.getForumId(), themeFragment.m_Topic.getId(), Client.getInstance().getAuthKey());
                if (isCancelled()) return;

                if (success && themeFragment.m_Topic != null) {
                    themeFragment.addToHistory(m_ThemeBody);
                    try {
                        themeFragment.showBody(m_ThemeBody);
                    } catch (Exception e) {
                        AppLog.e(e);
                    }
                } else {
                    if (themeFragment.m_Topic == null) {
                        return;
                    }
                    if (ex.getClass() != NotReportException.class) {
                        themeFragment.setTitle(ex.getMessage());
                        themeFragment.webView.loadDataWithBaseURL(themeFragment.m_LastUrl, m_ThemeBody, "text/html", "UTF-8", null);
                        //webView.loadDataWithBaseURL("https://"+ HostHelper.getHost() +"/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                        themeFragment.addToHistory(m_ThemeBody);
                    }
                    AppLog.e(themeFragment.getMainActivity(), ex, () -> themeFragment.showTheme(themeFragment.getLastUrl()));
                }
            }
        }
    }

}
