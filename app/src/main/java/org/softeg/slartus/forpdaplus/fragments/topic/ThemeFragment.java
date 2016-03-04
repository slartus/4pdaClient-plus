package org.softeg.slartus.forpdaplus.fragments.topic;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
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
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.common.HelpTask;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewDialogFragment;
import org.softeg.slartus.forpdaplus.controls.imageview.ImgViewer;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostFragment;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.NotesListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicReadersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicUtils;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicsListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicReadersBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo;
import org.softeg.slartus.forpdaplus.notes.NoteDialog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by radiationx on 28.10.15.
 */
public class ThemeFragment extends WebViewFragment implements BricksListDialogFragment.IBricksListDialogCaller {
    @Bind(R.id.quick_post_panel) LinearLayout mQuickPostPanel;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.wvBody) AdvWebView webView;
    @Bind(R.id.txtSearch) EditText txtSearch;
    @Bind(R.id.pnlSearch) LinearLayout pnlSearch;
    @Bind(R.id.buttonsPanel) FrameLayout buttonsPanel;

    @OnClick(R.id.btnPrevSearch)
    public void btnPrevSearch(){
        webView.findNext(false);
    }
    @OnClick(R.id.btnNextSearch)
    public void btnNextSearch(){
        webView.findNext(true);
    }
    @OnClick(R.id.btnCloseSearch)
    public void btnCloseSearch(){
        closeSearch();
    }
    @OnClick(R.id.btnUp)
    public void btnUp(){
        webView.pageUp(true);
    }
    @OnClick(R.id.btnDown)
    public void btnDown(){
        webView.pageDown(true);
    }

    private static final String TAG = "ThemeActivity";
    private static final String TOPIC_URL_KEY = "ThemeActivity.TOPIC_URL_KEY";
    public static Boolean LoadsImagesAutomatically = null;


    private Handler mHandler = new Handler();

    private String m_LastUrl;
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
    private ForPdaDeveloperInterface m_ForPdaDeveloperInterface;
    private Menu menu;
    private GetThemeTask asyncTask;
    private MyWebViewClient webViewClient;

    public static ThemeFragment newInstance(String url){
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString(TOPIC_URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getThemeUrl(CharSequence topicId){
        return "http://4pda.ru/forum/index.php?showtopic=" + topicId;
    }
    public static String getThemeUrl(CharSequence topicId, CharSequence urlParams){
        return String.format("http://4pda.ru/forum/index.php?showtopic=%s%s", topicId, TextUtils.isEmpty(urlParams) ? "" : ("&" + urlParams));
    }

    public static void showTopicById(CharSequence topicId, CharSequence urlParams) {
        String url = getThemeUrl(topicId, urlParams);
        MainActivity.addTab("Тема", url, newInstance(url));
    }
    public static void showTopicById(CharSequence title, CharSequence topicId, CharSequence urlParams) {
        String url = getThemeUrl(topicId, urlParams);
        MainActivity.addTab(title.toString(), url, newInstance(url));
    }
    public static void showTopicById(CharSequence topicId) {
        String url = getThemeUrl(topicId);
        MainActivity.addTab("Тема", url, newInstance(url));
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
        if(webViewClient==null)
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
                    .title("Подтвердите действие")
                    .content("Имеется введенный текст сообщения! Закрыть тему?")
                    .positiveText("Да")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            clear();
                            getMainActivity().removeTab(getTag());
                        }
                    })
                    .negativeText("Отмена")
                    .show();
            return true;
        } else {
            clear();
            return false;
        }
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public String Prefix() {
        return "theme";
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.theme, container, false);
        ButterKnife.bind(this, view);
        initSwipeRefreshLayout();
        lastStyle = App.getInstance().getThemeCssFileName();
        LoadsImagesAutomatically = null;

        getMainActivity().setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);// чтобы поиск начинался при вводе текста

        mQuickPostFragment = (QuickPostFragment) getChildFragmentManager().findFragmentById(R.id.quick_post_fragment);
        mQuickPostFragment.setParentTag(getTag());
        mQuickPostFragment.setOnPostSendListener(new QuickPostFragment.PostSendListener() {
            @Override
            public void onPostExecute(org.softeg.slartus.forpdaplus.controls.quickpost.PostTask.PostResult postResult) {
                if (postResult.Success) {
                    hideMessagePanel();
                    if (Client.getInstance().getRedirectUri() == null)
                        android.util.Log.e("ThemeActivity", "redirect is null");
                    m_LastUrl = Client.getInstance().getRedirectUri() == null ? Client.getInstance().getLastUrl() : Client.getInstance().getRedirectUri().toString();
                    m_Topic = postResult.ExtTopic;

                    if (postResult.TopicBody == null)
                        android.util.Log.e("ThemeActivity", "TopicBody is null");
                    addToHistory(postResult.TopicBody);
                    showBody(postResult.TopicBody);

                } else {
                    if (postResult.Exception != null)
                        AppLog.e(getMainActivity(), postResult.Exception, new Runnable() {
                            @Override
                            public void run() {
                                mQuickPostFragment.post();
                            }
                        });
                    else if (!TextUtils.isEmpty(postResult.ForumErrorMessage))
                        new MaterialDialog.Builder(getContext())
                                .title("Сообщение форума")
                                .content(postResult.ForumErrorMessage)
                                .show();
                }
            }
        });

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

        setFabColors(fab);
        if(App.getInstance().getPreferences().getBoolean("pancilInActionBar",false))
            fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMessagePanelVisibility();
            }
        });
        setHideFab(fab);


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
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        webView.setWebChromeClient(new MyChromeClient());
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable e) {
                android.util.Log.e(TAG, e.getMessage());
            }
        }
        if (getSupportActionBar() != null)
            webView.setActionBarheight(getSupportActionBar().getHeight());

        setHideArrows(Preferences.isHideArrows());
        webView.addJavascriptInterface(new ForPdaWebInterface(this), ForPdaWebInterface.NAME);
        m_ForPdaDeveloperInterface = new ForPdaDeveloperInterface(this);
        webView.addJavascriptInterface(m_ForPdaDeveloperInterface, ForPdaDeveloperInterface.NAME);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putSerializable("History", m_History);
            outState.putSerializable("Topic", m_Topic);
            webView.saveState(outState);
            outState.putString("LastUrl", getLastUrl());
            outState.putString("ScrollElement", m_ScrollElement);
            outState.putString("LastStyle", lastStyle);
            outState.putBoolean("FromHistory", m_FromHistory);

            outState.putString("LoadsImagesAutomatically", LoadsImagesAutomatically == null ? "null" : (LoadsImagesAutomatically ? "1" : "0"));
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    //@Override
    protected void onRestoreInstanceState(Bundle outState) {
        //super.onRestoreInstanceState(outState);
        try {
            m_Topic = (ExtTopic) outState.getSerializable("Topic");
            if (m_Topic != null)
                mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
            m_LastUrl = outState.getString("LastUrl");
            m_ScrollElement = outState.getString("ScrollElement");

            if (m_ScrollElement != null && !TextUtils.isEmpty(m_ScrollElement))
                webView.setPictureListener(new MyPictureListener());

            m_FromHistory = outState.getBoolean("FromHistory");

            String sLoadsImagesAutomatically = outState.getString("LoadsImagesAutomatically");
            LoadsImagesAutomatically = "null".equals(sLoadsImagesAutomatically) ? null : Boolean.parseBoolean(sLoadsImagesAutomatically);


            loadPreferences(App.getInstance().getPreferences());
            m_History = (ArrayList<SessionHistory>) outState.getSerializable("History");
            assert m_History != null;
            if (m_History.size() > 0) {
                SessionHistory sessionHistory = m_History.get(m_History.size() - 1);
                m_ScrollY = sessionHistory.getY();
                m_LastUrl = sessionHistory.getUrl();
                m_Topic = sessionHistory.getTopic();
                if (m_Topic != null)
                    mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
                if (sessionHistory.getBody() == null) {
                    showTheme(sessionHistory.getUrl());
                } else {
                    String body = sessionHistory.getBody().replace(outState.getString("LastStyle"), App.getInstance().getThemeCssFileName());
                    showBody(body);
                    sessionHistory.setBody(body);
                }
            }


        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }

    }

    private Boolean m_FirstTime = true;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!m_FirstTime)
            onPrepareOptionsMenu();
        m_FirstTime = false;
        if (mTopicOptionsMenu != null)
            configureOptionsMenu(getMainActivity(), getHandler(), mTopicOptionsMenu, true, getLastUrl());
        else if (getTopic() != null)
            mTopicOptionsMenu = addOptionsMenu(getMainActivity(), getHandler(), menu, true, getLastUrl());

    }

    private SubMenu mTopicOptionsMenu;

    private SubMenu addOptionsMenu(final Context context, final Handler mHandler,
                                          Menu menu, Boolean addFavorites, final String shareItUrl) {
        SubMenu optionsMenu = menu.addSubMenu("Опции темы");

        optionsMenu.getItem().setIcon(R.drawable.ic_menu_more);
        configureOptionsMenu(context, mHandler, optionsMenu, addFavorites, shareItUrl);
        return optionsMenu;
    }

    private void configureOptionsMenu(final Context context, final Handler mHandler, SubMenu optionsMenu,
                                             Boolean addFavorites, final String shareItUrl) {

        optionsMenu.clear();


        if (addFavorites) {
            optionsMenu.add(R.string.AddToFavorites).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        TopicUtils.showSubscribeSelectTypeDialog(context, mHandler, getTopic());
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }

                    return true;
                }
            });

            optionsMenu.add(R.string.DeleteFromFavorites).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        final HelpTask helpTask = new HelpTask(context, context.getString(R.string.DeletingFromFavorites));
                        helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                            public Object onMethod(Object param) {
                                if (helpTask.Success)
                                    Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                else
                                    AppLog.e(context, helpTask.ex);
                                return null;
                            }
                        });
                        helpTask.execute(new HelpTask.OnMethodListener() {
                                             public Object onMethod(Object param) throws IOException, ParseException, URISyntaxException {
                                                 return TopicApi.deleteFromFavorites(Client.getInstance(),
                                                         getTopic().getId());
                                             }
                                         }
                        );
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }
                    return true;
                }
            });


            optionsMenu.add(R.string.OpenTopicForum).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        ForumFragment.showActivity(context, getTopic().getForumId(), getTopic().getId());
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }
                    return true;
                }
            });
        }


        optionsMenu.add(R.string.NotesByTopic).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Bundle args = new Bundle();
                args.putString(NotesListFragment.TOPIC_ID_KEY, getTopic().getId());
                MainActivity.showListFragment(new NotesBrickInfo().getName(), args);
                return true;
            }
        });
        if(!Preferences.Topic.getReadersAndWriters()) {
            optionsMenu.add("Кто читает тему").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Bundle args = new Bundle();
                    args.putString(TopicReadersListFragment.TOPIC_ID_KEY, getTopic().getId());
                    MainActivity.showListFragment(getTopic().getId(), TopicReadersBrickInfo.NAME, args);
                    return true;
                }
            });
            optionsMenu.add("Кто писал сообщения").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Bundle args = new Bundle();
                    args.putString(TopicWritersListFragment.TOPIC_ID_KEY, getTopic().getId());
                    MainActivity.showListFragment(getTopic().getId(), TopicWritersBrickInfo.NAME, args);
                    return true;
                }
            });
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        try {
            boolean pancil = App.getInstance().getPreferences().getBoolean("pancilInActionBar",false);
            if(pancil) {
                menu.add("Написать")
                        .setIcon(R.drawable.ic_pencil_white_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(MenuItem item) {
                                toggleMessagePanelVisibility();
                                return true;
                            }
                        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            menu.add(R.string.Refresh)
                    .setIcon(R.drawable.ic_refresh_white_24dp)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            reloadTopic();
                            return true;
                        }
                    }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            SubMenu subMenu = menu.addSubMenu(R.string.Attaches)
                    .setIcon(R.drawable.ic_download_white_24dp);

            subMenu.add("Вложения текущей страницы")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            showPageAttaches();
                            return true;
                        }
                    });
            subMenu.add("Все вложения топика")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            showTopicAttaches();
                            return true;
                        }
                    });

            menu.add(R.string.FindOnPage)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            onSearchRequested();

                            return true;
                        }
                    });
            menu.add(R.string.FindInTopic)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            SearchSettingsDialogFragment.showSearchSettingsDialog(getMainActivity(),
                                    SearchSettingsDialogFragment.createTopicSearchSettings(getTopic().getId()));
                            return true;
                        }
                    });

            mTopicOptionsMenu = addOptionsMenu(getMainActivity(), getHandler(), menu, true, getLastUrl());

            menu.add("Ссылка").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ExtUrl.showSelectActionDialog(getMainActivity(), "Ссылка", TextUtils.isEmpty(getLastUrl()) ? ("http://4pda.ru/forum/index.php?showtopic=" + getTopic().getId()) : getLastUrl());
                    return true;
                }
            });
            SubMenu optionsMenu = menu.addSubMenu("Вид");
            optionsMenu.getItem().setTitle("Вид");


            optionsMenu.add(String.format("Аватары (%s)",
                    App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()]))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(final MenuItem menuItem) {
                            String[] avatars = App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles);
                            new MaterialDialog.Builder(getMainActivity())
                                    .title("Показывать аватары")
                                    .cancelable(true)
                                    .items(avatars)
                                    .itemsCallbackSingleChoice(Preferences.Topic.getShowAvatarsOpt(), new MaterialDialog.ListCallbackSingleChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog, View view, int i, CharSequence avatars) {
                                            //if(i==-1) return false;

                                            Preferences.Topic.setShowAvatarsOpt(i);
                                            menuItem.setTitle(String.format("Показывать аватары (%s)",
                                                    App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()]));
                                            return true; // allow selection
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    });
            if(!pancil) {
                optionsMenu.add("Скрывать карандаш")
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Preferences.setHideFab(!Preferences.isHideFab());
                                setHideFab(fab);
                                menuItem.setChecked(Preferences.isHideFab());
                                return true;
                            }
                        }).setCheckable(true).setChecked(Preferences.isHideFab());
            }

            optionsMenu.add("Скрыть стрелки")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setHideArrows(!Preferences.isHideArrows());
                            setHideArrows(Preferences.isHideArrows());
                            menuItem.setChecked(Preferences.isHideArrows());
                            return true;
                        }
                    }).setCheckable(true).setChecked(Preferences.isHideArrows());

            optionsMenu.add("Загр-ть изобр-я (для сессии)")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Boolean loadImagesAutomatically1 = getLoadsImagesAutomatically();
                            setLoadsImagesAutomatically(!loadImagesAutomatically1);
                            menuItem.setChecked(!loadImagesAutomatically1);
                            return true;
                        }
                    }).setCheckable(true).setChecked(getLoadsImagesAutomatically());
            optionsMenu.add("Размер шрифта")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            showFontSizeDialog();
                            return true;
                        }
                    });

            optionsMenu.add("Стиль").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    showStylesDialog(App.getInstance().getPreferences());
                    return true;
                }
            });
            if (Preferences.System.isCurator()) {
                menu.add("Мультимодерация").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            ThemeCurator.showMmodDialog(getActivity(), ThemeFragment.this, getTopic().getId());
                        } catch (Exception ex) {
                            return false;
                        }
                        return true;
                    }
                });
            }

        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }

        this.menu = menu;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(m_Topic!=null) {
            setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());
        }
        if(mQuickPostFragment!=null)
            mQuickPostFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mQuickPostFragment!=null)
            mQuickPostFragment.onPause();
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

    public boolean onSearchRequested() {
        hideMessagePanel();
        pnlSearch.setVisibility(View.VISIBLE);

        return false;
    }

    protected void showQuoteEditor(String url) {
        DialogFragment quoteEditorDialogFragment = ThemeQuoteEditor
                .newInstance(url, getTag());
        quoteEditorDialogFragment.show(getChildFragmentManager(), "dialog");
    }

    public void saveHtml() {
        try {
            webView.evalJs("window." + ForPdaDeveloperInterface.NAME + ".saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void checkBodyAndReload(String body) {
        if (TextUtils.isEmpty(body)) {
            reloadTopic();
        }
    }

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
        TopicAttachmentListFragment.showActivity(getMainActivity(), m_Topic.getId());
    }

    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        if (Build.VERSION.SDK_INT >= 16) {
            webView.findAllAsync(query);
        } else {
            webView.findAll(query);
        }
        try {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(webView, true);
        } catch (Throwable ignored) {
        }
        //onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= 16) {
                    webView.findAllAsync("");
                } else {
                    webView.findAll("");
                }

                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, false);
                } catch (Throwable ignored) {
                }

                pnlSearch.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getMainActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
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

    @Override
    public boolean onBackPressed() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return true;
        }

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
                    mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
                showBody(sessionHistory.getBody());
            }
            return true;
        }

        getPostBody();
        if (!TextUtils.isEmpty(m_PostBody)) {
            new MaterialDialog.Builder(getMainActivity())
                    .title("Подтвердите действие")
                    .content("Имеется введенный текст сообщения! Закрыть тему?")
                    .positiveText("Да")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            clear();
                            //onBackPressed();
                            getMainActivity().removeTab(getTag());
                        }
                    })
                    .negativeText("Отмена")
                    .show();
            return true;
        } else {
            clear();
            return false;
            //super.onBackPressed();
        }

    }

    public void clear() {
        clear(false);
    }

    public void clear(Boolean clearChache) {
        webView.setPictureListener(null);
        webView.setWebViewClient(null);
        webView.loadData("<html><head></head><body bgcolor=" + App.getInstance().getCurrentBackgroundColorHtml() + "></body></html>", "text/html", "UTF-8");
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

    public void setPostBody(String postBody) {
        m_PostBody = postBody;
    }

    public void quote(final String forumId, final String topicId, final String postId, final String postDate, String userId, String userNick) {
        final String finalPostDate = Functions.getForumDateTime(Functions.parseForumDateTime(postDate, Functions.getToday(), Functions.getYesterToday()));
        final String mUserNick = userNick.replace("\"","\\\"");
        CharSequence clipboardText = null;
        try {
            ClipboardManager clipboardManager = (android.content.ClipboardManager) App.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData primaryClip = clipboardManager.getPrimaryClip();
            clipboardText = null;
            if (primaryClip != null)
                for (int i = 0; i < primaryClip.getItemCount(); i++) {
                    clipboardText = clipboardManager.getPrimaryClip().getItemAt(i).getText();
                    if ("primaryClip".equals(clipboardText) || "clipboardManager".equals(clipboardText))
                        clipboardText = null;
                    if (clipboardText != null)
                        clipboardText = clipboardText.toString().trim();
                    if (!TextUtils.isEmpty(clipboardText))
                        break;
                }
        } catch (Throwable ex) {
            AppLog.eToast(getContext(), ex);
        }


        CharSequence[] titles = new CharSequence[]{"Цитата сообщения", "Пустая цитата", "Цитата буфера"};
        if (TextUtils.isEmpty(clipboardText))
            titles = new CharSequence[]{"Редактор цитаты", "Пустая цитата"};
        final CharSequence finalClipboardText = clipboardText;
        new MaterialDialog.Builder(getContext())
                .title("Цитата")
                .cancelable(true)
                .items(titles)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int i, CharSequence titles) {
                        switch (i) {
                            case 0:
                                showQuoteEditor("http://4pda.ru/forum/index.php?act=Post&CODE=02&f=" + forumId + "&t=" + topicId + "&qpid=" + postId);
                                break;
                            case 1:
                                getMainActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler().post(new Runnable() {
                                            public void run() {
                                                insertTextToPost("[quote name=\"" + mUserNick + "\" date=\"" + finalPostDate + "\" post=\"" + postId + "\"]\n\n[/quote]");
                                            }
                                        });
                                    }
                                });
                                break;
                            case 2:
                                getMainActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler().post(new Runnable() {
                                            public void run() {
                                                insertTextToPost("[quote name=\"" + mUserNick + "\" date=\"" + finalPostDate + "\" post=\"" + postId + "\"]\n" + finalClipboardText + "\n[/quote]");
                                            }
                                        });
                                    }
                                });
                                break;
                        }
                    }
                })
                .show();
    }

    public void openActionMenu(final String postId, final String postDate,
                               final String userId, final String userNick,
                               final Boolean canEdit, final Boolean canDelete) {
        try {
            final List<MenuListDialog> list = new ArrayList<>();

            if (Client.getInstance().getLogined()){
                list.add(new MenuListDialog("Ссылка на сообщение", new Runnable() {
                    @Override
                    public void run() {
                        showLinkMenu(Post.getLink(m_Topic.getId(), postId), postId);
                    }
                }));
                list.add(new MenuListDialog("Жалоба на сообщение", new Runnable() {
                    @Override
                    public void run() {
                        Post.claim(getMainActivity(), mHandler, m_Topic.getId(), postId);
                    }
                }));
                if (canEdit) {
                    list.add(new MenuListDialog("Изменить сообщение", new Runnable() {
                        @Override
                        public void run() {
                            EditPostFragment.editPost(getMainActivity(), m_Topic.getForumId(), m_Topic.getId(), postId, m_Topic.getAuthKey(), getTag());
                        }
                    }));
                }
                if (canDelete) {
                    list.add(new MenuListDialog("Удалить сообщение", new Runnable() {
                        @Override
                        public void run() {
                            prepareDeleteMessage(postId);
                        }
                    }));
                }
                list.add(new MenuListDialog("Цитата сообщения", new Runnable() {
                    @Override
                    public void run() {
                        quote(m_Topic.getForumId(), m_Topic.getId(), postId, postDate, userId, userNick);
                    }
                }));
            }
            list.add(new MenuListDialog("Сделать заметку", new Runnable() {
                @Override
                public void run() {
                    NoteDialog.showDialog(mHandler, getMainActivity(), m_Topic.getTitle(), null,
                            "http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + postId,
                            m_Topic.getId(), m_Topic.getTitle(), postId, null, null);
                }
            }));

            ExtUrl.showContextDialog(getContext(), null, list);
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private void showBody(String body) {
        super.showBody();
        try {
            setScrollElement();
            setTitle(m_Topic.getTitle());
            if(getSupportActionBar()!=null)
                setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());

            webView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);

            TopicsHistoryTable.addHistory(m_Topic, m_LastUrl);
            if(buttonsPanel.getTranslationY()!=0)
                ViewPropertyAnimator.animate(buttonsPanel)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(500)
                        .translationY(0);
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void showMessagePanel() {
        fab.setImageResource(R.drawable.ic_close_white_24dp);
        pnlSearch.setVisibility(View.GONE);
        mQuickPostPanel.setVisibility(View.VISIBLE);
        mQuickPostPanel.setEnabled(Client.getInstance().getLogined());
        mQuickPostFragment.showKeyboard();
    }

    public void hideMessagePanel() {
        fab.setImageResource(R.drawable.ic_pencil_white_24dp);
        mQuickPostPanel.setVisibility(View.GONE);
        mQuickPostFragment.hidePopupWindow();
        hideKeyboard();
    }

    public void toggleMessagePanelVisibility() {
        if (!Client.getInstance().getLogined()) {
            Toast.makeText(getMainActivity(), "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
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
                .title("Выберите действие")
                .content("Обновить страницу?")
                .positiveText("Обновить")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        reloadTopic();
                    }
                })
                .negativeText("Нет")
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
        if (requestCode == ForPdaDeveloperInterface.FILECHOOSER_RESULTCODE) {
            m_ForPdaDeveloperInterface.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == EditPostFragment.NEW_EDIT_POST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String url = data.getStringExtra(EditPostFragment.POST_URL_KEY);
                assert url != null;
                mQuickPostFragment.clearPostBody();

                closeSearch();

                asyncTask = new GetThemeTask();
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
                "(https?:/+4pda.ru/+forum/+index.php\\?.*?showtopic=[^\"]*)",
                "(https?:/+4pda.ru/+forum/+index.php\\?.*?act=findpost&pid=\\d+[^\"]*?)",
                "(https?:/+4pda.ru/+index.php\\?.*?act=findpost&pid=\\d+[^\"]*?)"
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


            Uri uri = Uri.parse(topicUrl.toLowerCase());
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
                Matcher m = p.matcher(topicUrl);
                if (m.find()) {
                    anchor = m.group(1);
                }
            }
            if (anchor == null) {
                showTheme(topicUrl);
                return;
            }
            String fragment = anchor;
            String currentBody = m_History.get(m_History.size() - 1).getBody();
            if (currentBody.contains("name=\"" + fragment + "\"")) {
                webView.scrollTo(fragment);
                return;
            }

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
            return "http://4pda.ru/forum/index.php?showtopic=" + m.group(1) +
                    (m.group(2) != null ? ("&st=" + m.group(2)) : "");
        return url;
    }

    public void showTheme(String url, boolean clearText) {
        if(clearText) mQuickPostFragment.clearPostBody();
        showTheme(url);
    }

    public void showTheme(String url) {
        try {
            closeSearch();
            if (url == null) {
                Toast.makeText(getMainActivity(), "Пустой url", Toast.LENGTH_SHORT).show();
                return;
            }
            url = lofiversionToNormal(url);
            //webView.clearCache(true);
            if (m_History.size() > 0) {
                m_History.get(m_History.size() - 1).setY(webView.getScrollY());
            }
            webView.setWebViewClient(getWebViewClient());
            //webView.getSettings().setLoadsImagesAutomatically(getLoadsImagesAutomatically());

            asyncTask = new GetThemeTask();
            asyncTask.execute(url.replace("|", ""));
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public AdvWebView getWebView() {
        return webView;
    }

    private class MyChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.e("kekp", newProgress+" %");
        }
    }

    private void prepareDeleteMessage(final String postId) {
        new MaterialDialog.Builder(getMainActivity())
                .title("Подтвердите действие")
                .content("Вы действительно хотите удалить это сообщение?")
                .positiveText("Удалить")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        deleteMessage(postId);
                    }
                })
                .negativeText("Отмена")
                .show();
    }

    private void deleteMessage(final String postId) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getMainActivity())
                .progress(true,0)
                .cancelable(false)
                .content("Удаление сообщения...")
                .show();
        new Thread(new Runnable() {
            public void run() {
                Throwable ex = null;

                try {
                    org.softeg.slartus.forpdaplus.classes.Post.delete(postId, m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
                } catch (Throwable e) {
                    ex = e;
                }

                final Throwable finalEx = ex;
                mHandler.post(new Runnable() {
                    public void run() {
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
                        getWebView().evalJs("document.querySelector('div[name*=del"+postId+"]').remove();");
                    }
                });
            }
        }).start();

    }

    public void showRep(final String userId) {
        UserReputationFragment.showActivity(getMainActivity(), userId, false);
    }

    public void insertTextToPost(final String text) {
        mQuickPostFragment.insertTextToPost(text);
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
        if (m_ScrollElement != null) {
            webView.setPictureListener(new MyPictureListener());
        }
    }

    private void addToHistory(String topicBody) {
        int historyLimit = Preferences.Topic.getHistoryLimit();
        if (m_History.size() >= historyLimit && m_History.size() > 0)
            m_History.get(m_History.size() - historyLimit).setBody(null);
        m_History.add(new SessionHistory(m_Topic, m_LastUrl, topicBody, 0));
    }

    private class MyPictureListener implements WebView.PictureListener {
        Thread m_ScrollThread;

        public void onNewPicture(WebView view, Picture arg1) {
            if (TextUtils.isEmpty(m_ScrollElement) && m_ScrollY == 0) {
                //webView.setPictureListener(null);
                return;
            }

            if (m_ScrollThread != null) return;

            m_ScrollThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            tryScrollToElement();
                        }
                    });
                }
            });


            m_ScrollThread.start();
        }

        private void tryScrollToElement() {
            if (m_ScrollY != 0) {
                webView.scrollTo(0, m_ScrollY);
                m_ScrollY = 0;
            } else if (!TextUtils.isEmpty(m_ScrollElement)) {
                webView.offActionBarOnScrollEvents();
                webView.scrollTo(0, 100);
                webView.scrollTo(0, 0);
                webView.onActionBarOnScrollEvents();
                webView.evalJs("scrollToElement('" + m_ScrollElement + "');");
                if (getSupportActionBar() != null && Preferences.isHideActionBar()) {
                    //getSupportActionBar().hide();
                }
            }

            m_ScrollElement = null;
            webView.setPictureListener(null);
        }
    }

    public List<ArrayList<String>> imageAttaches = new ArrayList<>();
    private class MyWebViewClient extends WebViewClient {
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
                Log.e(TAG, "onReceivedError: " + errorCode + ", " + description);
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
                        Class[] parameterTypes = null;
                        String[] parameterValues = new String[0];
                        if (!TextUtils.isEmpty(query)) {
                            Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(url);
                            ArrayList<String> objs = new ArrayList<String>();

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

            if(checkIsImage(url))
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
                    m_Topic == null ? null : m_Topic.getAuthKey());
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

        private boolean checkIsImage(final String url){
            final Pattern imagePattern = PatternExtensions.compile("http://.*?\\.(png|jpg|jpeg|gif)$");
            if(!imagePattern.matcher(url).find()) return false;
            if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
                Client.getInstance().showLoginForm(getContext(), new Client.OnUserChangedListener() {
                    public void onUserChanged(String user, Boolean success) {
                        if (success) {
                            showImage(url);
                        }
                    }
                });
            }else {
                showImage(url);
            }
            return true;
        }

        private void showImage(String url){
            for(ArrayList<String> list:imageAttaches){
                for(int i = 0; i<list.size();i++){
                    if(list.get(i).equals(url)){
                        ImgViewer.startActivity(getContext(), list, i);
                        return;
                    }
                }
            }
            ImgViewer.startActivity(getContext(), url);
        }

        private boolean checkIsPoll(String url) {
            Matcher m = Pattern.compile("4pda.ru.*?addpoll=1").matcher(url);
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

            Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Mod&CODE=04&f=(\\d+)&t=(\\d+)&p=(\\d+)&st=(\\d+)&auth_key=(.*?)").matcher(url);
            if (m.find()) {

                prepareDeleteMessage(m.group(3));
                return true;
            }
            return false;
        }

        private boolean tryQuote(final String url) {

            Matcher m = Pattern.compile("4pda.ru/forum/index.php\\?act=Post&CODE=02&f=\\d+&t=\\d+&qpid=\\d+").matcher(url);
            if (m.find()) {
                showQuoteEditor(url);
                return true;
            }
            return false;
        }
    }

    private class GetThemeTask extends AsyncTask<String, String, Boolean> {
        private int scrollY = 0;
        private String m_ThemeBody;
        private Throwable ex;

        public GetThemeTask() {}

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
                m_LastUrl = forums[0];
                m_LastUrl = "http://4pda.ru/forum/index.php?" + prepareTopicUrl(m_LastUrl);
                if (forums.length == 1) {
                    pageBody = client.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?" + prepareTopicUrl(m_LastUrl), null);
                } else
                    pageBody = forums[1];

                m_LastUrl = client.getRedirectUri() == null ? m_LastUrl : client.getRedirectUri().toString();
                m_SpoilFirstPost = Preferences.Topic.getSpoilFirstPost();
                TopicBodyBuilder topicBodyBuilder = client.parseTopic(pageBody, App.getInstance(), m_LastUrl,
                        m_SpoilFirstPost);

                m_Topic = topicBodyBuilder.getTopic();

                m_ThemeBody = topicBodyBuilder.getBody();

                topicBodyBuilder.clear();
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
                setLoading(true);
                scrollY = m_ScrollY;
                hideMessagePanel();
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
        }

        protected void onPostExecute(final Boolean success) {
            setLoading(false);
            TabItem item = App.getInstance().getTabByTag(getTag());
            if(item!=null){
                TabItem tabItem = App.getInstance().getTabByTag(item.getParentTag());
                if(tabItem!=null&&!tabItem.getTag().contains("tag")){
                    Fragment fragment = getMainActivity().getSupportFragmentManager().findFragmentByTag(item.getParentTag());
                    if(fragment instanceof TopicsListFragment&&getTopic()!=null&&getTopic().getId()!=null)
                        ((TopicsListFragment) fragment).topicAfterClick(getTopic().getId());
                }
            }

            if (scrollY != 0)
                webView.setPictureListener(new MyPictureListener());
            Log.e("kek", webView.getSettings().getLoadsImagesAutomatically()+" loadimages");

            m_ScrollY = scrollY;
            if (m_Topic != null)
                mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
            if (isCancelled()) return;

            if (success) {
                addToHistory(m_ThemeBody);
                showBody(m_ThemeBody);
            } else {
                if (ex.getClass() != NotReportException.class) {
                    setTitle(ex.getMessage());
                    webView.loadDataWithBaseURL("http://4pda.ru/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                    addToHistory(m_ThemeBody);

                }
                AppLog.e(getMainActivity(), ex, new Runnable() {
                    @Override
                    public void run() {
                        showTheme(getLastUrl());
                    }
                });
            }
        }
    }

}
