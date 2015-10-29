package org.softeg.slartus.forpdaplus.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import net.londatiga.android3d.ActionItem;
import net.londatiga.android3d.QuickAction;

import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.TopicBodyBuilder;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.imageview.ImageViewDialogFragment;
import org.softeg.slartus.forpdaplus.controls.quickpost.QuickPostFragment;
import org.softeg.slartus.forpdaplus.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listfragments.TopicAttachmentListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.notes.NoteDialog;
import org.softeg.slartus.forpdaplus.post.EditPostActivity;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;
import org.softeg.slartus.forpdaplus.topicview.Curator;
import org.softeg.slartus.forpdaplus.topicview.DeveloperWebInterface;
import org.softeg.slartus.forpdaplus.topicview.QuoteEditorDialogFragment;
import org.softeg.slartus.forpdaplus.topicview.SessionHistory;
import org.softeg.slartus.forpdaplus.topicview.ThemeActivity;
import org.softeg.slartus.forpdaplus.topicview.TopicViewMenuFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 28.10.15.
 */
public class ThemeFragment extends WebViewFragment implements BricksListDialogFragment.IBricksListDialogCaller {



    private static final String TAG = "ThemeActivity";
    private static final String TOPIC_URL_KEY = "ThemeActivity.TOPIC_URL_KEY";
    public static Boolean LoadsImagesAutomatically = null;
    TopicViewMenuFragment mFragment1;
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
    private Curator mCurator;
    private String lastStyle;
    private ForPdaDeveloperInterface m_DeveloperWebInterface;
    
    View view;
    public static ThemeFragment newInstance(Context context, String url){
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString(TOPIC_URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }
    public static void showTopicById(Context context, CharSequence topicId, CharSequence urlParams) {
        String url = String.format("http://4pda.ru/forum/index.php?showtopic=%s%s", topicId, TextUtils.isEmpty(urlParams) ? "" : ("&" + urlParams));
        //showTopicByUrl(context, url);
        ((MainActivity)context).addTab("Тема", url, newInstance(context, url));
    }

    public static void showTopicById(Context context, CharSequence topicId) {
        String url = "http://4pda.ru/forum/index.php?showtopic=" + topicId;
        showTopicByUrl(context, url);
    }

    public static void showTopicByUrl(Context context, CharSequence url) {
        Intent intent = new Intent(context, ThemeActivity.class);
        intent.putExtra(TOPIC_URL_KEY, url.toString());
        context.startActivity(intent);
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
        public Window getWindow() {
        return null;
    }

    @Override 
    public ActionBar getSupportActionBar() {
        return ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    @Override
    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return false;
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
    public void refresh() {

    }

    @Override
    public String Prefix() {
        return "theme";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.theme, container, false);
        setHasOptionsMenu(true);
        lastStyle = App.getInstance().getThemeCssFileName();
        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getActivity(), "Режим разработчика", Toast.LENGTH_SHORT).show();

        LoadsImagesAutomatically = null;
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //createActionMenu();

        //mCurator = new Curator(getActivity());

        getActivity().setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);// чтобы поиск начинался при вводе текста

        mQuickPostFragment = (QuickPostFragment) getChildFragmentManager().findFragmentById(R.id.quick_post_fragment);
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
                        AppLog.e(getActivity(), postResult.Exception, new Runnable() {
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

        m_DeveloperWebInterface = new ForPdaDeveloperInterface(this);
        webView.addJavascriptInterface(m_DeveloperWebInterface, DeveloperWebInterface.NAME);


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
            AppLog.e(getActivity(), ex);
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
            AppLog.e(getActivity(), ex);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getPostBody();
                if (!TextUtils.isEmpty(m_PostBody)) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Подтвердите действие")
                            .content("Имеется введенный текст сообщения! Закрыть?")
                            .positiveText("Да")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    //finish();
                                }
                            })
                            .negativeText("Отмена")
                            .show();
                }else{
                    App.showMainActivityWithoutBack(getActivity());
                }
                return true;
        }
        return true;
    }

    protected void createActionMenu() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (TopicViewMenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new TopicViewMenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            MenuItem item;
            boolean pancil = PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean("pancilInActionBar",false);
            if(pancil) {
                item = menu.add("Написать")
                        .setIcon(R.drawable.ic_pencil_white_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(MenuItem item) {
                                toggleMessagePanelVisibility();
                                return true;
                            }
                        });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            item = menu.add(R.string.Refresh)
                    .setIcon(R.drawable.ic_refresh_white_24dp)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            reloadTopic();
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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

            item = menu.add(R.string.FindOnPage)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            onSearchRequested();

                            return true;
                        }
                    });
            item = menu.add(R.string.FindInTopic)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            SearchSettingsDialogFragment.showSearchSettingsDialog(getActivity(),
                                    SearchSettingsDialogFragment.createTopicSearchSettings(getTopic().getId()));
                            return true;
                        }
                    });

            menu.add(R.string.Browser)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                Intent marketIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(getLastUrl()));
                                startActivity(Intent.createChooser(marketIntent, "Выберите"));


                            } catch (ActivityNotFoundException e) {
                                AppLog.e(getActivity(), e);
                            }
                            return true;
                        }
                    });

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            /*if (getInterface() != null)
                mTopicOptionsMenu = addOptionsMenu(getActivity(), getHandler(), menu, getInterface(),
                        true, getLastUrl());*/


            SubMenu optionsMenu = menu.addSubMenu("Вид");
            optionsMenu.getItem().setTitle("Вид");


            optionsMenu.add(String.format("Аватары (%s)",
                    App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()]))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(final MenuItem menuItem) {
                            String[] avatars = App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles);
                            new MaterialDialog.Builder(getActivity())
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

            optionsMenu.add("Скрывать верхнюю панель")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setHideActionBar(!Preferences.isHideActionBar());
                            setHideActionBar();
                            menuItem.setChecked(Preferences.isHideActionBar());
                            return true;
                        }
                    }).setCheckable(true).setChecked(Preferences.isHideActionBar());
            if(!pancil) {
                optionsMenu.add("Скрывать карандаш")
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Preferences.setHideFab(!Preferences.isHideFab());
                                setHideActionBar();
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
                    //showStylesDialog(prefs);
                    return true;
                }
            });
            optionsMenu.add("Вид как в браузере")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Preferences.setBrowserView(!Preferences.isBrowserView());
                            new MaterialDialog.Builder(getActivity())
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
                    }).setCheckable(true).setChecked(Preferences.isBrowserView());

            menu.add("Быстрый доступ").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    BricksListDialogFragment.showDialog((BricksListDialogFragment.IBricksListDialogCaller) getActivity(),
                            BricksListDialogFragment.QUICK_LIST_ID,
                            ListCore.getBricksNames(ListCore.getQuickBricks()), null);

                    return true;
                }
            });
            menu.add("Правила форума").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    StringBuilder text = new StringBuilder();
                    try {

                        BufferedReader br = new BufferedReader(new InputStreamReader(App.getInstance().getAssets().open("rules.txt"), "UTF-8"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line).append("\n");
                        }

                    } catch (IOException e) {
                        AppLog.e(getActivity(), e);
                    }
                    new MaterialDialog.Builder(getActivity())
                            .title("Правила форума")
                            .content(Html.fromHtml(text.toString()))
                            .positiveText(android.R.string.ok)
                            .show();

                    return true;
                }
            });

            if (Preferences.System.isDevSavePage()) {
                menu.add("Сохранить страницу").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            saveHtml();
                        } catch (Exception ex) {
                            return false;
                        }
                        return true;
                    }
                });
            }

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
            //addCloseMenuItem(menu);

        } catch (Exception ex) {
            AppLog.e(getActivity(), ex);
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        if(m_Topic!=null) {
            getSupportActionBar().setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());
        }
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
                .newInstance(url,getTag());
        quoteEditorDialogFragment.show(getChildFragmentManager(), "dialog");
    }

    public void saveHtml() {
        try {
            webView.evalJs("window." + DeveloperWebInterface.NAME + ".saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
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
            AppLog.e(getActivity(), ex);
        }
    }

    public void showPageAttaches() {
        try {
            webView.evalJs("window.HTMLOUT.showTopicAttaches('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    public void showTopicAttaches() {
        if (m_Topic == null)
            return;
        TopicAttachmentListFragment.showActivity(getActivity(), m_Topic.getId());
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
                new GetThemeTask(getActivity()).execute(intent.getData().toString());
            }
        }else {
            new GetThemeTask(getActivity()).execute(intent.getStringExtra(TOPIC_URL_KEY));
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
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    public void showLinkMenu(final String link) {
        showLinkMenu(link, "");
    }

    public void showLinkMenu(final String link, String postId) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, getActivity(), m_Topic.getTitle(), "", link, m_Topic.getId(),
                m_Topic.getTitle(), postId, "", "");

    }

    @Override
    public String getListName() {
        return null;
    }

    @Override
    public String getListTitle() {
        return null;
    }

    @Override
    public void loadData(boolean isRefresh) {

    }

    @Override
    public void startLoad() {

    }

    //@Override
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
            new MaterialDialog.Builder(getActivity())
                    .title("Подтвердите действие")
                    .content("Имеется введенный текст сообщения! Закрыть тему?")
                    .positiveText("Да")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            clear();
                            //onBackPressed();
                            ((MainActivity)getActivity()).removeTab(getTag());
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
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
                                getActivity().runOnUiThread(new Runnable() {
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
                                getActivity().runOnUiThread(new Runnable() {
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
            final QuickAction mQuickAction = new QuickAction(getActivity());

            ActionItem actionItem;


            int linkPosition = -1;
            if (Client.getInstance().getLogined()) {
                actionItem = new ActionItem();
                actionItem.setTitle("Ссылка");
                linkPosition = mQuickAction.addActionItem(actionItem);
            }

            int claimPosition = -1;
            if (Client.getInstance().getLogined()) {
                actionItem = new ActionItem();
                actionItem.setTitle("Жалоба");
                claimPosition = mQuickAction.addActionItem(actionItem);
            }

            int editPosition = -1;
            if (canEdit) {
                actionItem = new ActionItem();
                actionItem.setTitle("Изменить");
                editPosition = mQuickAction.addActionItem(actionItem);
            }


            int deletePosition = -1;
            if (canDelete) {
                actionItem = new ActionItem();
                actionItem.setTitle("Удалить");
                deletePosition = mQuickAction.addActionItem(actionItem);
            }

            /*
            int plusOdinPosition = -1;
            int minusOdinPosition = -1;
            if (Client.getInstance().getLogined() && !Client.getInstance().UserId.equals(userId)) {

                actionItem = new ActionItem();
                actionItem.setTitle("Хорошо (+)");
                plusOdinPosition = mQuickAction.addActionItem(actionItem);

                actionItem = new ActionItem();
                actionItem.setTitle("Плохо (-)");
                minusOdinPosition = mQuickAction.addActionItem(actionItem);
            }*/

            int notePosition;

            actionItem = new ActionItem();
            actionItem.setTitle("Заметка");
            notePosition = mQuickAction.addActionItem(actionItem);


            int quotePosition = -1;
            if (Client.getInstance().getLogined()) {
                actionItem = new ActionItem();
                actionItem.setTitle("Цитата");
                quotePosition = mQuickAction.addActionItem(actionItem);
            }

            final int finalDeletePosition = deletePosition;
            final int finalEditPosition = editPosition;

            final int finalLinkPosition = linkPosition;
            final int finalClaimPosition = claimPosition;
            //final int finalPlusOdinPosition = plusOdinPosition;
            //final int finalMinusOdinPosition = minusOdinPosition;
            final int finalNotePosition = notePosition;
            final int finalQuotePosition = quotePosition;
            mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(QuickAction source, int pos, int actionId) {
                    if (pos == finalDeletePosition) {
                        prepareDeleteMessage(postId);
                    } else if (pos == finalEditPosition) {
                        EditPostActivity.editPost(getActivity(), m_Topic.getForumId(), m_Topic.getId(), postId, m_Topic.getAuthKey());
                    } else  if (pos== finalLinkPosition) {
                        showLinkMenu(org.softeg.slartus.forpdaplus.classes.Post.getLink(m_Topic.getId(), postId), postId);
                    } else if (pos == finalClaimPosition) {
                        org.softeg.slartus.forpdaplus.classes.Post.claim(getActivity(), mHandler, m_Topic.getId(), postId);
                    }/* else if (pos == finalPlusOdinPosition) {
                        org.softeg.slartus.forpdaplus.classes.Post.plusOne(ThemeActivity.getActivity(), mHandler, postId);
                    } else if (pos == finalMinusOdinPosition) {
                        org.softeg.slartus.forpdaplus.classes.Post.minusOne(ThemeActivity.getActivity(), mHandler, postId);
                    }*/ else if (pos == finalNotePosition) {
                        NoteDialog.showDialog(mHandler, getActivity(), m_Topic.getTitle(), null,
                                "http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + postId,
                                m_Topic.getId(), m_Topic.getTitle(), postId, null, null);
                    } else if (pos == finalQuotePosition) {
                        quote(m_Topic.getForumId(), m_Topic.getId(), postId, postDate, userId, userNick);
                    }
                }
            });

            mQuickAction.show(webView, webView.getLastMotionEvent());
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    private void showBody(String body) {
        super.showBody();
        try {
            setScrollElement();
            getActivity().setTitle(m_Topic.getTitle());
            getSupportActionBar().setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());

            webView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);

            TopicsHistoryTable.addHistory(m_Topic, m_LastUrl);
        } catch (Exception ex) {
            AppLog.e(getActivity(), ex);
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
            Toast.makeText(getActivity(), "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
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
        new MaterialDialog.Builder(getActivity())
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
        ListFragmentActivity.showListFragment(getActivity(), brickInfo.getName(), args);
    }

    public Curator getCurator() {
        return mCurator;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DeveloperWebInterface.FILECHOOSER_RESULTCODE) {
            m_DeveloperWebInterface.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == EditPostActivity.NEW_EDIT_POST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String url = data.getStringExtra(EditPostActivity.POST_URL_KEY);
                assert url != null;
                mQuickPostFragment.clearPostBody();

                closeSearch();

                GetThemeTask getThemeTask = new GetThemeTask(getActivity());
                if (data.getExtras() != null && data.getExtras().containsKey(EditPostActivity.TOPIC_BODY_KEY)) {
                    getThemeTask.execute(url.replace("|", ""), data.getStringExtra(EditPostActivity.TOPIC_BODY_KEY));
                } else
                    getThemeTask.execute(url.replace("|", ""));
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
            AppLog.e(getActivity(), ex);
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

    public void showTheme(String url) {
        try {
            closeSearch();
            if (url == null) {
                Toast.makeText(getActivity(), "Пустой url", Toast.LENGTH_SHORT).show();
                return;
            }
            url = lofiversionToNormal(url);
            webView.clearCache(true);
            if (m_History.size() > 0) {
                m_History.get(m_History.size() - 1).setY(webView.getScrollY());
            }
            webView.setWebViewClient(new MyWebViewClient());


            GetThemeTask getThemeTask = new GetThemeTask(getActivity());
            getThemeTask.execute(url.replace("|", ""));
        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    public AdvWebView getWebView() {
        return webView;
    }

    private void prepareDeleteMessage(final String postId) {
        new MaterialDialog.Builder(getActivity())
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
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
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
                            AppLog.e(getActivity(), finalEx);

                        m_ScrollY = 0;
                        //showTheme(getLastUrl());
                        getWebView().evalJs("document.querySelector('div[name*=del"+postId+"]').remove();");
                    }
                });
            }
        }).start();

    }

    public String getPostText(String postId, String date, String userNick, String innerText) {
        return org.softeg.slartus.forpdaplus.classes.Post.getQuote(postId, date, userNick, innerText);
    }

    public void advPost() {
        EditPostActivity.newPost(getActivity(), m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey(),
                getPostBody());
    }

    public void showRep(final String userId) {
        UserReputationFragment.showActivity(getActivity(), userId, false);
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
        ForumUser.startChangeRep(getActivity(), mHandler, userId, userNick, postId, type, title);
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
            //ThemeActivity.getActivity().setProgressBarIndeterminateVisibility(true);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.clearHistory();
            //setSupportProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            // if (ThemeActivity.getActivity().webView.GetJavascriptInterfaceBroken())
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
                            }
                            parameterValues = new String[objs.size()];
                            parameterTypes = new Class[objs.size()];
                            for (int i = 0; i < objs.size(); i++) {
                                parameterTypes[i] = String.class;
                                parameterValues[i] = objs.get(i);
                            }
                        }
                        Method method = ThemeActivity.class.getMethod(function, parameterTypes);

                        method.invoke(getActivity(), parameterValues);
                    } catch (Exception e) {
                        AppLog.eToast(getActivity(), e);
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

            IntentActivity.tryShowUrl(getActivity(), mHandler, url, true, false,
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

        private final MaterialDialog dialog;
        private int scrollY = 0;
        private String m_ThemeBody;
        private Throwable ex;

        public GetThemeTask(Context context) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancel(true);
                        }
                    })
                    .content("Загрузка темы")
                    .build();
        }

        protected void onCancelled() {
            Toast.makeText(getActivity(), "Отменено", Toast.LENGTH_SHORT).show();
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

                TopicBodyBuilder topicBodyBuilder = client.parseTopic(pageBody, App.getInstance(), m_LastUrl,
                        m_SpoilFirstPost);

                m_Topic = topicBodyBuilder.getTopic();

                m_ThemeBody = topicBodyBuilder.getBody();

                topicBodyBuilder.clear();
                return true;
            } catch (Throwable e) {
                m_ThemeBody = pageBody;
                // Log.e(ThemeActivity.getActivity(), e);
                ex = e;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    dialog.setContent(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                scrollY = m_ScrollY;
                hideMessagePanel();

                this.dialog.setCanceledOnTouchOutside(false);
                this.dialog.show();
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
        }

        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ignored) {

            }
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
                    getActivity().setTitle(ex.getMessage());
                    webView.loadDataWithBaseURL("http://4pda.ru/forum/", m_ThemeBody, "text/html", "UTF-8", null);
                    addToHistory(m_ThemeBody);

                }
                AppLog.e(getActivity(), ex, new Runnable() {
                    @Override
                    public void run() {
                        showTheme(getLastUrl());
                    }
                });
            }
        }
    }

}
