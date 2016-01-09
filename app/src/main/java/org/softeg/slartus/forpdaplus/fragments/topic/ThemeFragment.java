package org.softeg.slartus.forpdaplus.fragments.topic;

import android.app.Activity;
import android.app.SearchManager;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.Post;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.common.HelpTask;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewDialogFragment;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostFragment;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.fragments.search.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.NotesListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicUtils;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo;
import org.softeg.slartus.forpdaplus.notes.NoteDialog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.utils.LogUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 28.10.15.
 */
public class ThemeFragment extends WebViewFragment implements BricksListDialogFragment.IBricksListDialogCaller {



    private static final String TAG = "ThemeActivity";
    private static final String TOPIC_URL_KEY = "ThemeActivity.TOPIC_URL_KEY";
    public static Boolean LoadsImagesAutomatically = null;
    FloatingActionButton btnShowHideEditPost;
    private AdvWebView webView;
    private Handler mHandler = new Handler();
    private EditText txtSearch;
    private LinearLayout pnlSearch;
    private String m_LastUrl;
    private ArrayList<SessionHistory> m_History = new ArrayList<SessionHistory>();
    private ExtTopic m_Topic;
    private Boolean m_SpoilFirstPost = true;
    // текст редактирования сообщения при переходе по страницам
    private String m_PostBody = "";
    // id сообщения к которому скроллить
    private String m_ScrollElement = null;
    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private QuickPostFragment mQuickPostFragment;
    private LinearLayout mQuickPostPanel;
    private ThemeCurator mCurator;
    private String lastStyle;
    private ForPdaDeveloperInterface m_ForPdaDeveloperInterface;
    private Menu menu;

    View view;
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
    public View getView() {
        return view;
    }

    public View findViewById(int id){
        return view.findViewById(id);
    }

    @Override
    public WebViewClient MyWebViewClient() {
        return new MyWebViewClient();
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

    AsyncTask asyncTask;
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
        initSwipeRefreshLayout();
        setHasOptionsMenu(true);
        lastStyle = App.getInstance().getThemeCssFileName();
        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getMainActivity(), "Режим разработчика", Toast.LENGTH_SHORT).show();

        LoadsImagesAutomatically = null;


        mCurator = new ThemeCurator(getMainActivity(), this);

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

        mQuickPostPanel = (LinearLayout) findViewById(R.id.quick_post_panel);

        pnlSearch = (LinearLayout) findViewById(R.id.pnlSearch);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {

            }
        });


        ImageButton btnPrevSearch = (ImageButton) findViewById(R.id.btnPrevSearch);
        btnPrevSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(false);
            }
        });
        ImageButton btnNextSearch = (ImageButton) findViewById(R.id.btnNextSearch);
        btnNextSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(true);
            }
        });
        ImageButton btnCloseSearch = (ImageButton) findViewById(R.id.btnCloseSearch);
        btnCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSearch();
            }
        });
        btnShowHideEditPost = (FloatingActionButton) findViewById(R.id.fab);
        btnShowHideEditPost.setColorNormal(App.getInstance().getColorAccent("Accent"));
        btnShowHideEditPost.setColorPressed(App.getInstance().getColorAccent("Pressed"));
        btnShowHideEditPost.setColorRipple(App.getInstance().getColorAccent("Pressed"));
        if(PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("pancilInActionBar",false)) {
            btnShowHideEditPost.setVisibility(View.GONE);
        }


        btnShowHideEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMessagePanelVisibility();
            }
        });


        webView = (AdvWebView) findViewById(R.id.wvBody);
        registerForContextMenu(webView);
        setWebViewSettings(true);


        webView.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT < 18)
            //noinspection deprecation
            webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = App.getInstance().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAppCacheEnabled(true);


        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable e) {
                android.util.Log.e(TAG, e.getMessage());
            }
        }
        if (getSupportActionBar() != null)
            webView.setActionBarheight(getSupportActionBar().getHeight());
        setHideFab(btnShowHideEditPost);
        setHideArrows(Preferences.isHideArrows());
        webView.addJavascriptInterface(new ForPdaWebInterface(this), ForPdaWebInterface.NAME);

        m_ForPdaDeveloperInterface = new ForPdaDeveloperInterface(this);
        webView.addJavascriptInterface(m_ForPdaDeveloperInterface, ForPdaDeveloperInterface.NAME);


        ImageButton up = (ImageButton) findViewById(R.id.btnUp);
        ImageButton down = (ImageButton) findViewById(R.id.btnDown);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.pageUp(true);
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.pageDown(true);
            }
        });
        hideMessagePanel();
        closeSearch();
        loadPreferences(PreferenceManager.getDefaultSharedPreferences(App.getContext()));
        showTheme(IntentActivity.normalizeThemeUrl(getArguments().getString(TOPIC_URL_KEY)));

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


            loadPreferences(PreferenceManager.getDefaultSharedPreferences(App.getContext()));
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

        optionsMenu.add("Ссылка").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                ExtUrl.showSelectActionDialog(getMainActivity(), "Ссылка", TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + getTopic().getId()) : shareItUrl);
                return true;
            }
        });


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        try {
            boolean pancil = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("pancilInActionBar",false);
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



            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getMainActivity().getApplicationContext());
            mTopicOptionsMenu = addOptionsMenu(getMainActivity(), getHandler(), menu, true, getLastUrl());


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

            /*optionsMenu.add("Скрывать верхнюю панель")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setHideActionBar(!Preferences.isHideActionBar());
                            setHideActionBar();
                            menuItem.setChecked(Preferences.isHideActionBar());
                            return true;
                        }
                    }).setCheckable(true).setChecked(Preferences.isHideActionBar());*/
            if(!pancil) {
                optionsMenu.add("Скрывать карандаш")
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Preferences.setHideFab(!Preferences.isHideFab());
                                setHideFab();
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
                    showStylesDialog(prefs);
                    return true;
                }
            });
            /*optionsMenu.add("Вид как в браузере")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setBrowserView(!Preferences.isBrowserView());
                            new MaterialDialog.Builder(getMainActivity())
                                    .content("Перезагрузить страницу?")
                                    .positiveText("Ок")
                                    .negativeText("Отмена")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            showTheme(getLastUrl());
                                        }
                                    })
                                    .show();
                            menuItem.setChecked(Preferences.isBrowserView());
                            return true;
                        }
                    }).setCheckable(true).setChecked(Preferences.isBrowserView());*/
            if (Preferences.System.isCurator()) {
                menu.add("Мультимодерация").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            getCurator().showMmodDialog();
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
            getSupportActionBar().setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());
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
/*
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }
*/
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
        if(intent.getData()!=null) {
            if (!intent.getData().toString().equals("")) {
                new GetThemeTask(getMainActivity()).execute(intent.getData().toString());
            }
        }else {
            new GetThemeTask(getMainActivity()).execute(intent.getStringExtra(TOPIC_URL_KEY));
        }
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
                                                insertTextToPost("[quote name=\"" + mUserNick + "\" date=\"" + postDate + "\" post=\"" + postId + "\"]\n\n[/quote]");
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
                                                insertTextToPost("[quote name=\"" + mUserNick + "\" date=\"" + postDate + "\" post=\"" + postId + "\"]\n" + finalClipboardText + "\n[/quote]");
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
            List<String> items = new ArrayList<>();

            int i = 0;


            int linkPosition = -1;
            int claimPosition = -1;
            int editPosition = -1;
            int deletePosition = -1;
            int notePosition = -1;
            int quotePosition = -1;
            if (Client.getInstance().getLogined()) {
                items.add("Ссылка на сообщение");
                linkPosition = i; i++;

                items.add("Жалоба на сообщение");
                claimPosition = i; i++;

                if (canEdit) {
                    items.add("Изменить сообщение");
                    editPosition = i; i++;
                }
                if (canDelete) {
                    items.add("Удалить сообщение");
                    deletePosition = i; i++;
                }
                items.add("Цитата сообщения");
                quotePosition = i; i++;
            }

            items.add("Сделать заметку");
            notePosition = i;

            final int finalDeletePosition = deletePosition;
            final int finalEditPosition = editPosition;

            final int finalLinkPosition = linkPosition;
            final int finalClaimPosition = claimPosition;
            final int finalNotePosition = notePosition;
            final int finalQuotePosition = quotePosition;
            new MaterialDialog.Builder(getMainActivity())
                    .items(items.toArray(new CharSequence[items.size()]))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            if (i == finalDeletePosition) {
                                prepareDeleteMessage(postId);
                            } else if (i == finalEditPosition) {
                                EditPostFragment.editPost(getMainActivity(), m_Topic.getForumId(), m_Topic.getId(), postId, m_Topic.getAuthKey(), getTag());
                            } else  if (i== finalLinkPosition) {
                                showLinkMenu(Post.getLink(m_Topic.getId(), postId), postId);
                            } else if (i == finalClaimPosition) {
                                Post.claim(getMainActivity(), mHandler, m_Topic.getId(), postId);
                            } else if (i == finalNotePosition) {
                                NoteDialog.showDialog(mHandler, getMainActivity(), m_Topic.getTitle(), null,
                                        "http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + postId,
                                        m_Topic.getId(), m_Topic.getTitle(), postId, null, null);
                            } else if (i == finalQuotePosition) {
                                quote(m_Topic.getForumId(), m_Topic.getId(), postId, postDate, userId, userNick);
                            }
                        }
                    })
                    .show();
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private void showBody(String body) {
        super.showBody();
        try {
            setScrollElement();
            getMainActivity().setTitle(m_Topic.getTitle());
            if(getSupportActionBar()!=null)
                getSupportActionBar().setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());

            webView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);

            TopicsHistoryTable.addHistory(m_Topic, m_LastUrl);
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public void showMessagePanel() {
        btnShowHideEditPost.setImageResource(R.drawable.ic_close_white_24dp);
        /*Boolean translucentNavigation = true;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            translucentNavigation = false;
        Window w = getWindow();
        if (translucentNavigation)
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);*/

        pnlSearch.setVisibility(View.GONE);
        mQuickPostPanel.setVisibility(View.VISIBLE);
        mQuickPostPanel.setEnabled(Client.getInstance().getLogined());
    }

    public void hideMessagePanel() {
        btnShowHideEditPost.setImageResource(R.drawable.ic_pencil_white_24dp);
        /*Boolean translucentNavigation = true;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            translucentNavigation = false;
        Window w = getWindow();
        if (translucentNavigation)
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);*/


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

    public ThemeCurator getCurator() {
        return mCurator;
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

                GetThemeTask getThemeTask = new GetThemeTask(getMainActivity());
                if (data.getExtras() != null && data.getExtras().containsKey(EditPostFragment.TOPIC_BODY_KEY)) {
                    getThemeTask.execute(url.replace("|", ""), data.getStringExtra(EditPostFragment.TOPIC_BODY_KEY));
                } else
                    getThemeTask.execute(url.replace("|", ""));
                asyncTask = getThemeTask;
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
            webView.clearCache(true);
            if (m_History.size() > 0) {
                m_History.get(m_History.size() - 1).setY(webView.getScrollY());
            }
            webView.setWebViewClient(new MyWebViewClient());

            GetThemeTask getThemeTask = new GetThemeTask(getMainActivity());
            getThemeTask.execute(url.replace("|", ""));
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    public AdvWebView getWebView() {
        return webView;
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

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            //setSupportProgressBarIndeterminateVisibility(true);
            //ThemeActivity.getMainActivity().setProgressBarIndeterminateVisibility(true);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.clearHistory();
            //setSupportProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            // if (ThemeActivity.getMainActivity().webView.GetJavascriptInterfaceBroken())
            {
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
                                LogUtil.D("THEME FRAGMENT", "objs " + Uri.decode(m.group(2)));
                            }
                            parameterValues = new String[objs.size()];
                            parameterTypes = new Class[objs.size()];
                            for (int i = 0; i < objs.size(); i++) {
                                parameterTypes[i] = String.class;
                                parameterValues[i] = objs.get(i);
                            }
                        }
                        Method method = ThemeFragment.class.getMethod(function, parameterTypes);

                        method.invoke(getMainActivity(), parameterValues);
                    } catch (Exception e) {
                        AppLog.eToast(getMainActivity(), e);
                    }
                    return true;
                }

            }
            m_ScrollY = 0;
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

        public GetThemeTask(Context context) {

        }

        protected void onCancelled() {
            //Toast.makeText(getMainActivity(), "Отменено", Toast.LENGTH_SHORT).show();
        }

        private CharSequence prepareTopicUrl(CharSequence url) {
            Uri uri = Uri.parse(url.toString());
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
            if (scrollY != 0)
                webView.setPictureListener(new MyPictureListener());

            m_ScrollY = scrollY;
            if (m_Topic != null)
                mQuickPostFragment.setTopic(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
            if (isCancelled()) return;

            if (success) {
                addToHistory(m_ThemeBody);
                showBody(m_ThemeBody);
            } else {
                if (ex.getClass() != NotReportException.class) {
                    getMainActivity().setTitle(ex.getMessage());
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
