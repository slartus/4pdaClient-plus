package org.softeg.slartus.forpdaplus.qms;


import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.IWebViewContainer;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.classes.WebViewExternals;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;
import org.softeg.slartus.forpdaplus.emotic.Smiles;
import org.softeg.slartus.forpdaplus.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 15:50
 */
public class QmsChatActivity extends BaseFragmentActivity implements IWebViewContainer {
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

    private static final String MID_KEY = "mid";
    private static final String TID_KEY = "tid";
    private static final String THEME_TITLE_KEY = "theme_title";
    private static final String NICK_KEY = "nick";
    private static final String PAGE_BODY_KEY = "page_body";
    private static final String POST_TEXT_KEY = "PostText";
    private HtmlPreferences m_HtmlPreferences;
    private WebViewExternals m_WebViewExternals;
    final Handler uiHandler = new Handler();

    private PopupPanelView mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.qms_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(this);

        edMessage = (EditText) findViewById(R.id.edMessage);
        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), edMessage);
        mPopupPanelView.activityCreated(this);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startSendMessage();
            }
        });


        wvChat = (AdvWebView) findViewById(R.id.wvChat);
        registerForContextMenu(wvChat);


        wvChat.getSettings().setDomStorageEnabled(true);
        wvChat.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        wvChat.getSettings().setAppCachePath(appCachePath);
        wvChat.getSettings().setAppCacheEnabled(true);

        wvChat.getSettings().setAllowFileAccess(true);

        wvChat.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        wvChat.addJavascriptInterface(this, "HTMLOUT");
        wvChat.getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());

        m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(PreferenceManager.getDefaultSharedPreferences(App.getContext()));

        m_WebViewExternals.setWebViewSettings();

        wvChat.setWebViewClient(new MyWebViewClient());
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString(MID_KEY);
        m_Nick = extras.getString(NICK_KEY);
        m_TId = extras.getString(TID_KEY);
        m_ThemeTitle = extras.getString(THEME_TITLE_KEY);
        final String[] m_PageBody = {extras.getString(PAGE_BODY_KEY)};
        if (TextUtils.isEmpty(m_Nick))
            setTitle("QMS");
        else
            setTitle(m_Nick + ":QMS:" + m_ThemeTitle);

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
        //  hidePanels();
    }



    private final static int FILECHOOSER_RESULTCODE = 1;

    @JavascriptInterface
    public void showChooseCssDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");

                    // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);

                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(QmsChatActivity.this, "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    AppLog.e(QmsChatActivity.this, ex);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK &&requestCode == FILECHOOSER_RESULTCODE) {
            String attachFilePath = FileUtils.getRealPathFromURI(this, data.getData());
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
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edMessage.getWindowToken(), 0);
    }

    @JavascriptInterface
    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(QmsChatActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @JavascriptInterface
    public void deleteMessages(final String[] checkBoxNames) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkBoxNames == null) {
                    Toast.makeText(QmsChatActivity.this, "Не выбраны сообщения для удаления!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(QmsChatActivity.this, "Не выбраны сообщения для удаления!", Toast.LENGTH_LONG).show();
                    return;
                }

                new AlertDialogBuilder(QmsChatActivity.this)
                        .setTitle("Подтвердите действие")
                        .setCancelable(true)
                        .setMessage(String.format("Вы действительно хотите удалить выбранные сообщения (%d)?", ids.size()))
                        .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                m_SendTask = new DeleteTask(QmsChatActivity.this);
                                m_SendTask.execute(ids);
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    public void deleteDialog() {

        new AlertDialogBuilder(this)
                .setTitle("Подтвердите действие")
                .setCancelable(true)
                .setMessage("Вы действительно хотите удалить диалог?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        ArrayList<String> ids = new ArrayList<>();
                        ids.add(m_TId);
                        m_SendTask = new DeleteDialogTask(QmsChatActivity.this, ids);
                        m_SendTask.execute();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();

    }

    public static void openChat(Context activity, String userId, String userNick,
                                String tid, String themeTitle) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsChatActivity.class);
        intent.putExtra(MID_KEY, userId);
        intent.putExtra(NICK_KEY, userNick);

        intent.putExtra(TID_KEY, tid);
        intent.putExtra(THEME_TITLE_KEY, themeTitle);

        activity.startActivity(intent);
    }

    public static void openChat(Context activity, String userId, String userNick, String tid, String themeTitle,
                                String pageBody) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsChatActivity.class);
        intent.putExtra(MID_KEY, userId);
        intent.putExtra(NICK_KEY, userNick);

        intent.putExtra(TID_KEY, tid);
        intent.putExtra(THEME_TITLE_KEY, themeTitle);
        intent.putExtra(PAGE_BODY_KEY, pageBody);

        activity.startActivity(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        outState.putString(MID_KEY, m_Id);
        outState.putString(NICK_KEY, m_Nick);
        outState.putString(TID_KEY, m_TId);
        outState.putString(THEME_TITLE_KEY, m_ThemeTitle);
        outState.putString(POST_TEXT_KEY, edMessage.getText().toString());

    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);

        m_Id = outState.getString(MID_KEY);
        m_Nick = outState.getString(NICK_KEY);
        m_TId = outState.getString(TID_KEY);
        m_ThemeTitle = outState.getString(THEME_TITLE_KEY);
        setTitle(m_Nick + "-QMS-" + m_ThemeTitle);
        edMessage.setText(outState.getString(POST_TEXT_KEY));

    }


    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();
        startUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        m_UpdateTimeout = ExtPreferences.parseInt(preferences, "qms.chat.update_timer", 15) * 1000;
    }

    private void checkNewQms()  {
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
        htmlBuilder.beginBody("onload=\"scrollToElement('bottom_element')\"");

        if(!Preferences.Topic.isShowAvatars())
            chatBody = chatBody.replaceAll("<img[^>]*?class=\"avatar\"[^>]*>","");
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
                setSupportProgressBarIndeterminateVisibility(true);
                //pbLoading.setVisibility(View.VISIBLE);
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
                        setSupportProgressBarIndeterminateVisibility(false);
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
                        setTitle(m_Nick + "-QMS-" + m_ThemeTitle);
                    wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", finalChatBody, "text/html", "UTF-8", null);
                } else {
                    if ("Такого диалога не существует.".equals(finalEx.getMessage())) {
                        new AlertDialogBuilder(QmsChatActivity.this)
                                .setTitle("Ошибка")
                                .setMessage(finalEx.getMessage())
                                .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        showThread();
                                    }
                                })
                                .create().show();
                        m_UpdateTimer.cancel();
                        m_UpdateTimer.purge();

                    } else {
                        Toast.makeText(QmsChatActivity.this, AppLog.getLocalizedMessage(finalEx, finalEx.getLocalizedMessage()),
                                Toast.LENGTH_SHORT).show();
                    }

                }
                setSupportProgressBarIndeterminateVisibility(false);
            }
        });

    }

    private void onPostChat(String chatBody, Boolean success, Throwable ex) {
        if (success) {
            edMessage.getText().clear();

            wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", chatBody, "text/html", "UTF-8", null);
        } else {
            if (ex != null)
                AppLog.e(QmsChatActivity.this, ex, new Runnable() {
                    @Override
                    public void run() {
                        m_SendTask = new SendTask(QmsChatActivity.this);
                        m_SendTask.execute();
                    }
                });
            else
                Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
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
                    AppLog.e(QmsChatActivity.this, ex);
                }

            }
        }, 0L, m_UpdateTimeout);

    }
    MenuFragment mFragment1;
    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1= (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private String m_MessageText = null;

    private void saveScale(float scale) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("qms.ZoomLevel", scale);
        editor.commit();
    }

    private float loadScale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return prefs.getFloat("qms.ZoomLevel", wvChat.getScrollY());

    }

    public static String getEncoding() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        return prefs.getString("qms.chat.encoding", "UTF-8");

    }

    private void startSendMessage() {
        m_MessageText = edMessage.getText().toString();
        if (TextUtils.isEmpty(m_MessageText)) {
            Toast.makeText(this, "Введите текст для отправки.", Toast.LENGTH_SHORT).show();
            return;
        }
        m_SendTask = new SendTask(QmsChatActivity.this);
        m_SendTask.execute();
    }


    @Override
    public String Prefix() {
        return "theme";
    }

    @Override
    public WebView getWebView() {
        return wvChat;
    }


    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }

    @Override
    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return m_WebViewExternals.dispatchKeyEvent(event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v,
                                    android.view.ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = wvChat.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    private void showCompanionProfile() {
        ProfileWebViewActivity.startActivity(this, m_Id, m_Nick);
    }


    public void showLinkMenu(final String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, this, m_ThemeTitle, "", link, "", "", "", m_Id, m_Nick);
    }

    private AsyncTask<ArrayList<String>, Void, Boolean> m_SendTask = null;

    private class SendTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;


        public SendTask(Context context) {

            dialog = new AppProgressDialog(context);
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
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            onPostChat(m_ChatBody, success, ex);
        }


    }

    private class DeleteTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;


        public DeleteTask(Context context) {

            dialog = new AppProgressDialog(context);
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
            this.dialog.setMessage("Удаление сообщений...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            onPostChat(m_ChatBody, success, ex);
        }
    }

    private class DeleteDialogTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final ProgressDialog dialog;

        ArrayList<String> m_Ids;

        public DeleteDialogTask(Context context, ArrayList<String> ids) {
            m_Ids = ids;
            dialog = new AppProgressDialog(context);
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
            this.dialog.setMessage("Удаление диалогов...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (!success) {
                if (ex != null)
                    AppLog.e(QmsChatActivity.this, ex);
                else
                    Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }

            showThread();

        }
    }

    private void showThread() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(BaseFragmentActivity.SENDER_ACTIVITY)) {
            if ("class org.softeg.slartus.forpdaplus.qms_2_0.QmsContactThemesActivity".equals(getIntent().getExtras().get(BaseFragmentActivity.SENDER_ACTIVITY))) {
                finish();
                return;
            }
        }

        QmsContactThemesActivity.showThemes(this, m_Id, m_Nick);
        finish();
    }

    public void showFontSizeDialog() {
        View v = getLayoutInflater().inflate(R.layout.font_size_dialog, null);

        assert v != null;
        final SeekBar seekBar = (SeekBar) v.findViewById(R.id.value_seekbar);
        seekBar.setProgress(Preferences.getFontSize(Prefix()) - 1);
        final TextView textView = (TextView) v.findViewById(R.id.value_textview);
        textView.setText((seekBar.getProgress() + 1) + "");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getWebView().getSettings().setDefaultFontSize(i + 1);
                textView.setText((i + 1) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new AlertDialogBuilder(this)
                .setTitle("Размер шрифта")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Preferences.setFontSize(Prefix(), seekBar.getProgress() + 1);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        getWebView().getSettings().setDefaultFontSize(Preferences.Topic.getFontSize());
                    }
                })
                .create().show();

    }

    public static final class MenuFragment extends ProfileMenuFragment {
        public MenuFragment() {

        }

        private QmsChatActivity getInterface() {
            if (getActivity() == null) return null;
            return (QmsChatActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu,inflater);
            MenuItem item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    new Thread(new Runnable() {
                        public void run() {
                            ((QmsChatActivity) getActivity()).reLoadChatSafe();
                        }
                    }).start();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Настройки").setIcon(R.drawable.ic_menu_preferences);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), QmsChatPreferencesActivity.class);
                    getActivity().startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


            item = menu.add("Удалить сообщения").setIcon(R.drawable.ic_menu_delete);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().getWebView().loadUrl("javascript:deleteMessages('thread_form');");
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            item = menu.add("Удалить диалог").setIcon(R.drawable.ic_menu_delete);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().deleteDialog();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            menu.add("Размер шрифта")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            getInterface().showFontSizeDialog();
                            return true;
                        }
                    });

            item = menu.add("Профиль собеседника").setIcon(R.drawable.ic_action_user_online);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().showCompanionProfile();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            if(Preferences.System.isDeveloper()){
                menu.add("Сохранить страницу").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            getInterface().saveHtml();
                        } catch (Exception ex) {
                            return false;
                        }
                        return true;
                    }
                });
            }
        }
    }

    public void saveHtml() {
        try {
            wvChat.loadUrl("javascript:window.HTMLOUT.saveHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        } catch (Throwable ex) {
            AppLog.e(this, ex);
        }
    }

    @JavascriptInterface
    public void saveHtml(final String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        Toast.makeText(getContext(), "Внешнее хранилище недоступно!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    File file = new File(App.getInstance().getExternalFilesDir(null), "qmschat.txt");
                    FileWriter out = new FileWriter(file);
                    out.write(html);
                    out.close();
                    Uri uri = Uri.fromFile(file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "text/plain");
                    startActivity(intent);
                } catch (Exception e) {
                    AppLog.e(QmsChatActivity.this, e);
                }
            }
        });
    }


    private class MyWebViewClient extends WebViewClient {

        public MyWebViewClient() {
            m_Scale = loadScale();
        }

        private float m_Scale;


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            //  setSupportProgressBarIndeterminateVisibility(true);


            wvChat.setInitialScale((int) (m_Scale * 100));
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            m_Scale = newScale;
            saveScale(m_Scale);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);


            try {

                wvChat.setInitialScale((int) (m_Scale * 100));

            } catch (Throwable ex) {
                AppLog.e(QmsChatActivity.this, ex);
            }


            setSupportProgressBarIndeterminateVisibility(false);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            IntentActivity.tryShowUrl(QmsChatActivity.this, mHandler, url, true, false, "");

            return true;
        }
    }
}
