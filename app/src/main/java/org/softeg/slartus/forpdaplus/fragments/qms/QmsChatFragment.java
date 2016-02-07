package org.softeg.slartus.forpdaplus.fragments.qms;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.SaveHtml;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.fragments.WebViewFragment;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by radiationx on 12.11.15.
 */
public class QmsChatFragment extends WebViewFragment {

    private static final String MID_KEY = "mid";
    private static final String TID_KEY = "tid";
    private static final String THEME_TITLE_KEY = "theme_title";
    private static final String NICK_KEY = "nick";
    private static final String PAGE_BODY_KEY = "page_body";
    private static final String POST_TEXT_KEY = "PostText";
    private final static int FILECHOOSER_RESULTCODE = 1;
    final Handler uiHandler = new Handler();
    Menu menu;
    private Handler mHandler = new Handler();
    private AdvWebView wvChat;
    private String m_Id;
    private String m_TId;
    private String m_Nick = "";
    private String m_ThemeTitle = "";
    private long m_LastBodyLength = 0;
    private EditText edMessage;
    private long m_UpdateTimeout = 15000;
    private Timer m_UpdateTimer = new Timer();
    private HtmlPreferences m_HtmlPreferences;
    private WebViewExternals m_WebViewExternals;
    private View view;
    private PopupPanelView mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);
    private String m_MessageText = null;
    private AsyncTask<ArrayList<String>, Void, Boolean> m_SendTask = null;

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        mPopupPanelView.hidePopupWindow();
    }

    public static void openChat(String userId, String userNick, String tid, String themeTitle, String pageBody){
        MainActivity.addTab(themeTitle, themeTitle + userId, newInstance(userId, userNick, tid, themeTitle, pageBody));
    }

    public static void openChat(String userId, String userNick, String tid, String themeTitle){
        MainActivity.addTab(themeTitle, themeTitle + userId, newInstance(userId, userNick, tid, themeTitle));
    }

    public static QmsChatFragment newInstance(String userId, String userNick, String tid, String themeTitle, String pageBody) {
        Bundle args = new Bundle();
        args.putString(MID_KEY, userId);
        args.putString(NICK_KEY, userNick);
        args.putString(TID_KEY, tid);
        args.putString(THEME_TITLE_KEY, themeTitle);
        args.putString(PAGE_BODY_KEY, pageBody);

        QmsChatFragment fragment = new QmsChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static QmsChatFragment newInstance(String userId, String userNick, String tid, String themeTitle) {
        Bundle args = new Bundle();
        args.putString(MID_KEY, userId);
        args.putString(NICK_KEY, userNick);
        args.putString(TID_KEY, tid);
        args.putString(THEME_TITLE_KEY, themeTitle);

        QmsChatFragment fragment = new QmsChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String getEncoding() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        return prefs.getString("qms.chat.encoding", "UTF-8");

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public WebViewClient MyWebViewClient() {
        return new MyWebViewClient();
    }

    @Override
    public String getTitle() {
        return m_ThemeTitle;
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public void reload() {
        new Thread(new Runnable() {
            public void run() {
                reLoadChatSafe();
            }
        }).start();

    }

    @Override
    public AsyncTask getAsyncTask() {
        return m_SendTask;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.qms_chat, container, false);
        setHasOptionsMenu(true);
        assert view != null;

        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(getContext());

        edMessage = (EditText) view.findViewById(R.id.edMessage);
        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) view.findViewById(R.id.advanced_button), edMessage);
        mPopupPanelView.activityCreated(getMainActivity(), view);
        view.findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startSendMessage();
            }
        });


        wvChat = (AdvWebView) view.findViewById(R.id.wvChat);
        registerForContextMenu(wvChat);


        wvChat.getSettings().setDomStorageEnabled(true);
        wvChat.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getMainActivity().getApplicationContext().getCacheDir().getAbsolutePath();
        wvChat.getSettings().setAppCachePath(appCachePath);
        wvChat.getSettings().setAppCacheEnabled(true);

        wvChat.getSettings().setAllowFileAccess(true);

        wvChat.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        wvChat.addJavascriptInterface(this, "HTMLOUT");
        wvChat.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
        m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(PreferenceManager.getDefaultSharedPreferences(App.getContext()));

        m_WebViewExternals.setWebViewSettings(true);

        wvChat.setWebViewClient(new MyWebViewClient());
        Bundle extras = getArguments();

        m_Id = extras.getString(MID_KEY);
        m_Nick = extras.getString(NICK_KEY);
        m_TId = extras.getString(TID_KEY);
        m_ThemeTitle = extras.getString(THEME_TITLE_KEY);


        final String[] m_PageBody = {extras.getString(PAGE_BODY_KEY)};
        if (TextUtils.isEmpty(m_Nick))
            setTitle("QMS");
        else
            setTitle(m_ThemeTitle);
        if (getSupportActionBar() != null)
            setSubtitle(m_Nick);
        if (!TextUtils.isEmpty(m_PageBody[0])) {
            m_LastBodyLength = m_PageBody[0].length();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String body = transformChatBody(m_PageBody[0]);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);
                        }
                    });
                }
            }).start();


        }
        hideKeyboard();
        if (Preferences.System.isDevSavePage()|
                Preferences.System.isDevInterface()|
                Preferences.System.isDevStyle())
            Toast.makeText(getMainActivity(), "Режим разработчика", Toast.LENGTH_SHORT).show();

        loadPrefs();
        startUpdateTimer();
        return view;
    }

    @JavascriptInterface
    public void showChooseCssDialog() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getMainActivity(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(getMainActivity(), ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(getContext(), data.getData());
            String cssData = FileUtils.readFileText(attachFilePath)
                    .replace("\\", "\\\\")
                    .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            if (Build.VERSION.SDK_INT < 19)
                wvChat.loadUrl("javascript:window['HtmlInParseLessContent']('" + cssData + "');");
            else
                wvChat.evaluateJavascript("window['HtmlInParseLessContent']('" + cssData + "')",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        }
                );
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edMessage.getWindowToken(), 0);
    }
/*
    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        //super.onRestoreInstanceState(outState);

        m_Id = outState.getString(MID_KEY);
        m_Nick = outState.getString(NICK_KEY);
        m_TId = outState.getString(TID_KEY);
        m_ThemeTitle = outState.getString(THEME_TITLE_KEY);
        setTitle(m_ThemeTitle);
        setSubtitle(m_Nick);
        edMessage.setText(outState.getString(POST_TEXT_KEY));

    }
    */

    @JavascriptInterface
    public void showMessage(final String message) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getMainActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @JavascriptInterface
    public void deleteMessages(final String[] checkBoxNames) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkBoxNames == null) {
                    Toast.makeText(getMainActivity(), "Не выбраны сообщения для удаления!", Toast.LENGTH_LONG).show();
                    return;
                }

                final ArrayList<String> ids = new ArrayList<>();
                Pattern p = Pattern.compile("message-id\\[(\\d+)\\]", Pattern.CASE_INSENSITIVE);
                for (String checkBoxName : checkBoxNames) {
                    Matcher m = p.matcher(checkBoxName);
                    if (m.find()) {
                        ids.add(m.group(1));
                    }
                }
                if (ids.size() == 0) {
                    Toast.makeText(getMainActivity(), "Не выбраны сообщения для удаления!", Toast.LENGTH_LONG).show();
                    return;
                }

                new MaterialDialog.Builder(getMainActivity())
                        .title("Подтвердите действие")
                        .cancelable(true)
                        .content(String.format("Вы действительно хотите удалить выбранные сообщения (%d)?", ids.size()))
                        .positiveText("Удалить")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                m_SendTask = new DeleteTask(getMainActivity());
                                m_SendTask.execute(ids);
                            }
                        })
                        .negativeText("Отмена")
                        .show();
            }
        });
    }

    public void deleteDialog() {

        new MaterialDialog.Builder(getMainActivity())
                .title("Подтвердите действие")
                .cancelable(true)
                .content("Вы действительно хотите удалить диалог?")
                .positiveText("Удалить")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        ArrayList<String> ids = new ArrayList<>();
                        ids.add(m_TId);
                        m_SendTask = new DeleteDialogTask(getMainActivity(), ids);
                        m_SendTask.execute();
                    }
                })
                .negativeText("Отмена")
                .show();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        outState.putString(MID_KEY, m_Id);
        outState.putString(NICK_KEY, m_Nick);
        outState.putString(TID_KEY, m_TId);
        outState.putString(THEME_TITLE_KEY, m_ThemeTitle);
        outState.putString(POST_TEXT_KEY, edMessage.getText().toString());

    }

    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();
        startUpdateTimer();
        if(mPopupPanelView!=null)
            mPopupPanelView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        if(mPopupPanelView!=null)
            mPopupPanelView.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }

    @Override
    public void onDestroy() {
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        if (mPopupPanelView != null) {
            mPopupPanelView.destroy();
            mPopupPanelView = null;
        }
        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private void loadPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        m_UpdateTimeout = ExtPreferences.parseInt(preferences, "qms.chat.update_timer", 15) * 1000;
    }

    private void checkNewQms() {
        try {
            Client.getInstance().setQmsCount(QmsApi.getNewQmsCount(Client.getInstance()));
            Client.getInstance().doOnMailListener();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String transformChatBody(String chatBody) {
        checkNewQms();
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.beginHtml("QMS");
        htmlBuilder.beginBody("qms","onload=\"scrollToElement('bottom_element')\"",Preferences.Topic.isShowAvatars());

        if (!Preferences.Topic.isShowAvatars())
            chatBody = chatBody.replaceAll("<img[^>]*?class=\"avatar\"[^>]*>", "");
        if (m_HtmlPreferences.isSpoilerByButton())
            chatBody = HtmlPreferences.modifySpoiler(chatBody);
        chatBody = HtmlPreferences.modifyBody(chatBody, Smiles.getSmilesDict(), m_HtmlPreferences.isUseLocalEmoticons());
        htmlBuilder.append(chatBody);
        htmlBuilder.append("<div id=\"bottom_element\" name=\"bottom_element\"></div>");
        htmlBuilder.endBody();
        htmlBuilder.endHtml();

        return htmlBuilder.getHtml().toString();
    }

    private void reLoadChatSafe() {
        uiHandler.post(new Runnable() {
            public void run() {
//                setLoading(false);
                setSubtitle("Обновление");
            }
        });

        String chatBody = null;
        Throwable ex = null;
        Boolean updateTitle = false;
        try {
            String body;

            if (TextUtils.isEmpty(m_Nick)) {
                updateTitle = true;
                Map<String, String> additionalHeaders = new HashMap<String, String>();
                body = QmsApi.getChat(Client.getInstance(), m_Id, m_TId, additionalHeaders);
                if (additionalHeaders.containsKey("Nick"))
                    m_Nick = additionalHeaders.get("Nick");
                if (additionalHeaders.containsKey("ThemeTitle"))
                    m_ThemeTitle = additionalHeaders.get("ThemeTitle");
            } else {
                body = QmsApi.getChat(Client.getInstance(), m_Id, m_TId);
            }
            if (body.length() == m_LastBodyLength) {
                checkNewQms();
                uiHandler.post(new Runnable() {
                    public void run() {
//                        setLoading(false);
                        setSubtitle("");
                    }
                });
                return;
            }
            m_LastBodyLength = body.length();
            chatBody = transformChatBody(body);
        } catch (Throwable e) {
            ex = e;
        }
        final Throwable finalEx = ex;
        final String finalChatBody = chatBody;
        final Boolean finalUpdateTitle = updateTitle;
        uiHandler.post(new Runnable() {
            public void run() {
                if (finalEx == null) {
                    if (finalUpdateTitle)
                        setTitle(m_ThemeTitle);
                    setSubtitle(m_Nick);
                    wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", finalChatBody, "text/html", "UTF-8", null);
                } else {
                    if ("Такого диалога не существует.".equals(finalEx.getMessage())) {
                        new MaterialDialog.Builder(getMainActivity())
                                .title("Ошибка")
                                .content(finalEx.getMessage())
                                .positiveText("ОК")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        //showThread();
                                    }
                                })
                                .show();
                        m_UpdateTimer.cancel();
                        m_UpdateTimer.purge();

                    } else {
                        Toast.makeText(getMainActivity(), AppLog.getLocalizedMessage(finalEx, finalEx.getLocalizedMessage()),
                                Toast.LENGTH_SHORT).show();
                    }

                }
//                setLoading(false);
                setSubtitle("");
            }
        });

    }

    private void onPostChat(String chatBody, Boolean success, Throwable ex) {
        if (success) {
            edMessage.getText().clear();

            wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", chatBody, "text/html", "UTF-8", null);
        } else {
            if (ex != null)
                AppLog.e(getMainActivity(), ex, new Runnable() {
                    @Override
                    public void run() {
                        m_SendTask = new SendTask(getMainActivity());
                        m_SendTask.execute();
                    }
                });
            else
                Toast.makeText(getMainActivity(), "Неизвестная ошибка",
                        Toast.LENGTH_SHORT).show();
        }
    }

    private void startUpdateTimer() {
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        m_UpdateTimer = new Timer();
        m_UpdateTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                try {
                    if (m_SendTask != null && m_SendTask.getStatus() != AsyncTask.Status.FINISHED)
                        return;
                    reLoadChatSafe();
                } catch (Throwable ex) {
                    AppLog.e(getMainActivity(), ex);
                }

            }
        }, 0L, m_UpdateTimeout);

    }

    private void saveScale(float scale) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("qms.ZoomLevel", scale);
        editor.commit();
    }

    private float loadScale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        return prefs.getFloat("qms.ZoomLevel", wvChat.getScrollY());

    }

    private void startSendMessage() {
        m_MessageText = edMessage.getText().toString();
        if (TextUtils.isEmpty(m_MessageText)) {
            Toast.makeText(getMainActivity(), "Введите текст для отправки.", Toast.LENGTH_SHORT).show();
            return;
        }
        m_SendTask = new SendTask(getMainActivity());
        m_SendTask.execute();
    }

    @Override
    public String Prefix() {
        return "theme";
    }

    @Override
    @JavascriptInterface
    public void saveHtml(final String html) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SaveHtml(getMainActivity(), html, "qms");
            }
        });
    }

    @Override
    public AdvWebView getWebView() {
        return wvChat;
    }

    private void showCompanionProfile() {
        //ProfileWebViewActivity.startActivity(this, m_Id, m_Nick);
        ProfileFragment.showProfile(m_Id, m_Nick);
    }

    @Override
    public void showLinkMenu(final String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, getContext(), m_ThemeTitle, "", link, "", "", "", m_Id, m_Nick);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Обновить")
                .setIcon(R.drawable.ic_refresh_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                reload();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("Настройки")
                .setIcon(R.drawable.ic_settings_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getMainActivity(), QmsChatPreferencesActivity.class);
                getMainActivity().startActivity(intent);
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add("Удалить сообщения")
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                getWebView().loadUrl("javascript:deleteMessages('thread_form');");
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add("Удалить диалог")
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                deleteDialog();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        menu.add("Размер шрифта")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showFontSizeDialog();
                        return true;
                    }
                });

        menu.add("Профиль собеседника")
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                showCompanionProfile();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        this.menu = menu;
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

    private class SendTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final MaterialDialog dialog;
        public String m_ChatBody;
        private Throwable ex;

        public SendTask(Context context) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Отправка сообщения")
                    .build();
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {
            try {

                m_ChatBody = transformChatBody(QmsApi.sendMessage(Client.getInstance(), m_Id, m_TId, m_MessageText,
                        getEncoding()));

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
//            setLoading(false); //
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
//            setLoading(false);

            onPostChat(m_ChatBody, success, ex);
        }


    }

    private class DeleteTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final MaterialDialog dialog;
        public String m_ChatBody;
        private Throwable ex;

        public DeleteTask(Context context) {

            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Удаление сообщений")
                    .build();
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {
            try {

                m_ChatBody = transformChatBody(QmsApi.deleteMessages(Client.getInstance(),
                        m_Id, m_TId, params[0], getEncoding()));

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            onPostChat(m_ChatBody, success, ex);
        }
    }

    private class DeleteDialogTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final MaterialDialog dialog;

        ArrayList<String> m_Ids;
        private Throwable ex;

        public DeleteDialogTask(Context context, ArrayList<String> ids) {
            m_Ids = ids;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content("Удаление диалогов")
                    .build();
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {
            try {

                QmsApi.deleteDialogs(Client.getInstance(), m_Id, m_Ids);

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (!success) {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }

            //showThread();

        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Preferences.Notifications.Qms.readQmsDone();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            IntentActivity.tryShowUrl(getMainActivity(), mHandler, url, true, false, "");

            return true;
        }
    }
}
