package org.softeg.slartus.forpdaplus.fragments.qms;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.post.EditAttach;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.SimpleCookie;
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.HttpHelper;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AdvWebView;
import org.softeg.slartus.forpdaplus.classes.HtmlBuilder;
import org.softeg.slartus.forpdaplus.classes.ImageFilePath;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by radiationx on 12.11.15.
 */
public class QmsChatFragment extends WebViewFragment {
    private boolean emptyText = true;
    private static final String MID_KEY = "mid";
    private static final String TID_KEY = "tid";
    private static final String THEME_TITLE_KEY = "theme_title";
    private static final String NICK_KEY = "nick";
    private static final String PAGE_BODY_KEY = "page_body";
    private static final String POST_TEXT_KEY = "PostText";
    private final static int FILECHOOSER_RESULTCODE = 1;
    final Handler uiHandler = new Handler();
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
    private PopupPanelView mPopupPanelView;
    private String m_MessageText = null;
    private AsyncTask<ArrayList<String>, Void, Boolean> m_SendTask = null;
    private Button btnAttachments;

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        mPopupPanelView.hidePopupWindow();
    }

    public static void openChat(String userId, String userNick, String tid, String themeTitle, String pageBody) {
        MainActivity.addTab(themeTitle, themeTitle + userId, newInstance(userId, userNick, tid, themeTitle, pageBody));
    }

    public static void openChat(String userId, String userNick, String tid, String themeTitle) {
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
        SharedPreferences prefs = App.getInstance().getPreferences();

        return prefs.getString("qms.chat.encoding", "UTF-8");

    }

    @Override
    public WebViewClient getWebViewClient() {
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
        new Thread(this::reLoadChatSafe).start();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.qms_chat, container, false);
        assert view != null;

        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(getContext());

        edMessage = (EditText) findViewById(R.id.edMessage);
        if (mPopupPanelView == null)
            mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);
        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), edMessage);
        mPopupPanelView.activityCreated(getMainActivity(), view);

        final ImageButton send_button = (ImageButton) findViewById(R.id.btnSend);

        send_button.setOnClickListener(view12 -> startSendMessage());
        edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    if (!emptyText) {
                        send_button.clearColorFilter();
                        emptyText = true;
                    }
                } else {
                    if (emptyText) {
                        send_button.setColorFilter(ContextCompat.getColor(App.getContext(), R.color.selectedItemText), PorterDuff.Mode.SRC_ATOP);
                        emptyText = false;
                    }
                }
            }
        });


        wvChat = (AdvWebView) findViewById(R.id.wvChat);
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
        WebViewExternals m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(App.getInstance().getPreferences());

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
            new Thread(() -> {
                final String body = transformChatBody(m_PageBody[0]);

                mHandler.post(() -> wvChat.loadDataWithBaseURL("file:///android_asset/", body, "text/html", "UTF-8", null));
            }).start();


        }
        hideKeyboard();

        loadPrefs();
        startUpdateTimer();
        btnAttachments = (Button) findViewById(R.id.btnAttachments);
        btnAttachments.setOnClickListener(view1 -> showAttachesListDialog());
        return view;
    }

    @JavascriptInterface
    public void showChooseCssDialog() {
        getMainActivity().runOnUiThread(() -> {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");

                // intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);

            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getMainActivity(), R.string.no_app_for_get_file, Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                AppLog.e(getMainActivity(), ex);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK) {
                if (null == data) return;
                Uri selectedImageUri = data.getData();
                String selectedImagePath = ImageFilePath.getPath(getMainActivity().getApplicationContext(), selectedImageUri);
                if (selectedImagePath != null && selectedImagePath.matches("(?i)(.*)(jpg|png|gif)$")) {
                    saveAttachDirPath(selectedImagePath);
                    new UpdateTask(getMainActivity(), selectedImagePath).execute();
                } else {
                    Toast.makeText(getContext(), "Данный формат файла не поддерживается", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == FILECHOOSER_RESULTCODE) {
                String attachFilePath = FileUtils.getRealPathFromURI(getContext(), data.getData());
                String cssData = FileUtils.readFileText(attachFilePath)
                        .replace("\\", "\\\\")
                        .replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
                if (Build.VERSION.SDK_INT < 19)
                    wvChat.loadUrl("javascript:window['HtmlInParseLessContent']('" + cssData + "');");
                else
                    wvChat.evaluateJavascript("window['HtmlInParseLessContent']('" + cssData + "')",
                            s -> {

                            }
                    );
            }
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
        getMainActivity().runOnUiThread(() -> Toast.makeText(getMainActivity(), message, Toast.LENGTH_LONG).show());
    }

    @JavascriptInterface
    public void deleteMessages(final String[] checkBoxNames) {
        getMainActivity().runOnUiThread(() -> {
            if (checkBoxNames == null) {
                Toast.makeText(getMainActivity(), R.string.no_messages_for_delete, Toast.LENGTH_LONG).show();
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
                Toast.makeText(getMainActivity(), R.string.no_messages_for_delete, Toast.LENGTH_LONG).show();
                return;
            }

            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.confirm_action)
                    .cancelable(true)
                    .content(String.format(App.getContext().getString(R.string.ask_delete_messages), ids.size()))
                    .positiveText(R.string.delete)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            m_SendTask = new DeleteTask(getMainActivity());
                            m_SendTask.execute(ids);
                        }
                    })
                    .negativeText(R.string.cancel)
                    .show();
        });
    }

    @JavascriptInterface
    public void startDeleteModeJs(final String count) {
        getMainActivity().runOnUiThread(() -> startDeleteMode(count));

    }

    @JavascriptInterface
    public void stopDeleteModeJs() {
        Log.d("kek", "STOP");
        if (!DeleteMode)
            return;
        getMainActivity().runOnUiThread(() -> stopDeleteMode(true));
    }

    private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(R.string.delete)
                    .setIcon(R.drawable.delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            getWebView().loadUrl("javascript:deleteMessages('thread_form');");
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            stopDeleteMode(false);
        }
    }

    ActionMode mMode;
    private Boolean DeleteMode = false;

    private void startDeleteMode(String count) {
        if (!DeleteMode)
            mMode = getMainActivity().startActionMode(new AnActionModeOfEpicProportions());
        if (mMode != null)
            mMode.setTitle("Сообщений:" + count);

        DeleteMode = true;
    }

    private void stopDeleteMode(Boolean finishActionMode) {
        if (finishActionMode && mMode != null)
            mMode.finish();
        DeleteMode = false;
    }

    public void deleteDialog() {

        new MaterialDialog.Builder(getMainActivity())
                .title(R.string.confirm_action)
                .cancelable(true)
                .content(R.string.ask_delete_dialog)
                .positiveText(R.string.delete)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        ArrayList<String> ids = new ArrayList<>();
                        ids.add(m_TId);
                        m_SendTask = new DeleteDialogTask(getMainActivity(), ids);
                        m_SendTask.execute();
                    }
                })
                .negativeText(R.string.cancel)
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
        if (mPopupPanelView != null)
            mPopupPanelView.resume();
    }

    private void startAdaptiveTimeOutService() {
        if (!QmsNotifier.isUse(getContext()))
            return;

        App.reStartQmsService(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        startAdaptiveTimeOutService();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        if (mPopupPanelView != null)
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
        m_UpdateTimeout = ExtPreferences.parseInt(App.getInstance().getPreferences(), "qms.chat.update_timer", 15) * 1000;
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
        if (m_ThemeTitle == null | m_Nick == null) {
            Matcher m = Pattern.compile("<span id=\"chatInfo\"[^>]*>([^>]*?)\\|:\\|([^<]*)</span>").matcher(chatBody);
            if (m.find()) {
                m_Nick = m.group(1);
                m_ThemeTitle = m.group(2);
            }
        }
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.beginHtml("QMS");
        htmlBuilder.beginBody("qms", "onload=\"scrollToElement('bottom_element')\"", Preferences.Topic.isShowAvatars());

        if (!Preferences.Topic.isShowAvatars())
            chatBody = chatBody.replaceAll("<img[^>]*?class=\"avatar\"[^>]*>", "");
        if (m_HtmlPreferences.isSpoilerByButton())
            chatBody = HtmlPreferences.modifySpoiler(chatBody);
        chatBody = HtmlPreferences.modifyBody(chatBody, Smiles.getSmilesDict());
        chatBody = chatBody.replaceAll("(<a[^>]*?href=\"([^\"]*?savepice[^\"]*-)[\\w]*(\\.[^\"]*)\"[^>]*?>)[^<]*?(</a>)", "$1<img src=\"$2prev$3\">$4");
        htmlBuilder.append(chatBody);
        htmlBuilder.append("<div id=\"bottom_element\" name=\"bottom_element\"></div>");
        htmlBuilder.endBody();
        htmlBuilder.endHtml();

        return htmlBuilder.getHtml().toString();
    }

    private void reLoadChatSafe() {
        uiHandler.post(() -> {
//                setLoading(false);
            setSubtitle(App.getContext().getString(R.string.refreshing));
        });

        String chatBody = null;
        Throwable ex = null;
        Boolean updateTitle = false;
        try {
            String body;

            if (TextUtils.isEmpty(m_Nick)) {
                updateTitle = true;
                Map<String, String> additionalHeaders = new HashMap<>();
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
                uiHandler.post(() -> {
//                        setLoading(false);
                    setSubtitle("");
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
        uiHandler.post(() -> {
            if (finalEx == null) {
                if (finalUpdateTitle)
                    setTitle(m_ThemeTitle);
                setSubtitle(m_Nick);
                wvChat.loadDataWithBaseURL("file:///android_asset/", finalChatBody, "text/html", "UTF-8", null);
            } else {
                if ("Такого диалога не существует.".equals(finalEx.getMessage())) {
                    new MaterialDialog.Builder(getMainActivity())
                            .title(R.string.error)
                            .content(finalEx.getMessage())
                            .positiveText(R.string.ok)
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
        });

    }

    private void onPostChat(String chatBody, Boolean success, Throwable ex) {
        if (success) {
            edMessage.getText().clear();

            wvChat.loadDataWithBaseURL("file:///android_asset/", chatBody, "text/html", "UTF-8", null);
        } else {
            if (ex != null)
                AppLog.e(getMainActivity(), ex, () -> {
                    m_SendTask = new SendTask(getMainActivity());
                    m_SendTask.execute();
                });
            else
                Toast.makeText(getMainActivity(), R.string.unknown_error,
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

    private void startSendMessage() {
        if (emptyText) {
            Toast toast = Toast.makeText(getContext(), R.string.EnterMessage_, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, App.getInstance().getResources().getDisplayMetrics()));
            toast.show();
            return;
        }
        m_MessageText = edMessage.getText().toString();
        for (EditAttach attach : attachList) {
            if (!m_MessageText.contains(attach.getId())) {
                m_MessageText += "\n" + "[url=" + attach.getId() + "]" + attach.getId() + "[/url]";
            }
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
        getMainActivity().runOnUiThread(() -> new SaveHtml(getMainActivity(), html, "qms"));
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
        menu.add(R.string.refresh)
                .setIcon(R.drawable.refresh)
                .setOnMenuItemClickListener(menuItem -> {
                    reload();
                    return true;
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(R.string.setting)
                .setIcon(R.drawable.settings_white)
                .setOnMenuItemClickListener(menuItem -> {
                    Intent intent = new Intent(getMainActivity(), QmsChatPreferencesActivity.class);
                    getMainActivity().startActivity(intent);
                    return true;
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(R.string.delete_dialog)
                .setIcon(R.drawable.delete)
                .setOnMenuItemClickListener(menuItem -> {
                    deleteDialog();
                    return true;
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        menu.add(R.string.font_size)
                .setOnMenuItemClickListener(menuItem -> {
                    showFontSizeDialog();
                    return true;
                });

        menu.add(R.string.profile_interlocutor)
                .setOnMenuItemClickListener(menuItem -> {
                    showCompanionProfile();
                    return true;
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
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
        String m_ChatBody;
        private Throwable ex;

        SendTask(Context context) {
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(getString(R.string.sending_message))
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
            attachList.clear();
            refreshAttachmentsInfo();
        }


    }

    private class DeleteTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final MaterialDialog dialog;
        String m_ChatBody;
        private Throwable ex;

        DeleteTask(Context context) {

            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.deleting_messages)
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
            stopDeleteMode(true);
        }
    }

    private class DeleteDialogTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final MaterialDialog dialog;

        ArrayList<String> m_Ids;
        private Throwable ex;

        DeleteDialogTask(Context context, ArrayList<String> ids) {
            m_Ids = ids;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.deleting_dialogs)
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
                    Toast.makeText(getMainActivity(), R.string.unknown_error,
                            Toast.LENGTH_SHORT).show();
            }
            stopDeleteMode(true);
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


    //Upload file to savepic.ru
    private List<EditAttach> attachList = new ArrayList<>();
    private static final int MY_INTENT_CLICK = 302;

    private void showAttachesListDialog() {
        if (attachList.size() == 0) {
            startAddAttachment();
            return;
        }
        List<String> listItems = new ArrayList<>();
        for (EditAttach attach : attachList)
            listItems.add(attach.getName());
        CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        new MaterialDialog.Builder(getMainActivity())
                .cancelable(true)
                .title(R.string.attachments)
                .items(items)
                .itemsCallback((dialog, itemView, which, text) -> edMessage.append("[url=" + attachList.get(which).getId() + "]" + attachList.get(which).getId() + "[/url]"))
                .positiveText(R.string.do_download)
                .onPositive((dialog, which) -> startAddAttachment())
                .negativeText(R.string.ok)
                .show();
    }


    private void startAddAttachment() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), R.string.no_permission, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent imageintent = new Intent(
                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                imageintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(imageintent, MY_INTENT_CLICK);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getMainActivity(), R.string.no_app_for_get_image_file, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            AppLog.e(getMainActivity(), ex);
        }
    }

    private void saveAttachDirPath(String attachFilePath) {
        String lastSelectDirPath = FileUtils.getDirPath(attachFilePath);
        App.getInstance().getPreferences().edit().putString("EditPost.AttachDirPath", lastSelectDirPath).apply();
    }


    private class UpdateTask extends AsyncTask<String, Pair<String, Integer>, Boolean> {
        private final MaterialDialog dialog;
        private ProgressState m_ProgressState;

        private List<String> attachFilePaths;

        UpdateTask(Context context, List<String> attachFilePaths) {

            this.attachFilePaths = attachFilePaths;
            dialog = new MaterialDialog.Builder(context)
                    .progress(false, 100, false)
                    .content(R.string.sending_file)
                    .show();
        }

        UpdateTask(Context context, String newAttachFilePath) {
            this(context, new ArrayList<>(Arrays.asList(new String[]{newAttachFilePath})));
        }

        private EditAttach editAttach;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_ProgressState = new ProgressState() {
                    @Override
                    public void update(String message, int percents) {
                        publishProgress(new Pair<>("", percents));
                    }
                };

                int i = 1;
                for (String newAttachFilePath : attachFilePaths) {
                    publishProgress(new Pair<>(String.format(App.getContext().getString(R.string.format_sending_file), i++, attachFilePaths.size()), 0));

                    boolean found = false;
                    for (Cookie cookie1 : Client.getInstance().getCookies()) {
                        if (cookie1.getName().equals("PHPSESSID")) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        CookieStore cookieStore = new BasicCookieStore();
                        HttpContext context = new BasicHttpContext();
                        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                        new DefaultHttpClient().execute(new HttpPost("http://savepice.ru/"), context);

                        for (Cookie cookie : cookieStore.getCookies()) {
                            Log.d("save", "coolie name" + cookie.getName());
                            if (cookie.getName().equals("PHPSESSID")) {
                                Log.d("save", "try save cookie");
                                HttpHelper helper = new HttpHelper();
                                try {
                                    helper.getCookieStore().getCookies();
                                    helper.getCookieStore().addCookie(new SimpleCookie(cookie.getName(), cookie.getValue()));
                                    helper.writeExternalCookies();
                                } finally {
                                    helper.close();
                                }
                            }
                        }
                    }
                    String res = QmsApi.attachFile(Client.getInstance(), newAttachFilePath, m_ProgressState);


                    editAttach = new EditAttach("http://cdn1.savepice.ru" + res, "Изображение №" + attachList.size(), null, null);
                }

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Pair<String, Integer>... values) {
            super.onProgressUpdate(values);
            if (!TextUtils.isEmpty(values[0].first))
                dialog.setContent(values[0].first);
            dialog.setProgress(values[0].second);
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setCancelable(true);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setOnCancelListener(dialogInterface -> {
                if (m_ProgressState != null)
                    m_ProgressState.cancel();
                cancel(false);
            });
            this.dialog.setProgress(0);

            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success || (isCancelled() && editAttach != null)) {
                attachList.add(editAttach);
                refreshAttachmentsInfo();
            } else {

                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error, Toast.LENGTH_SHORT).show();

            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Boolean success) {
            super.onCancelled(success);
            if (success || (isCancelled() && editAttach != null)) {
                attachList.add(editAttach);
                refreshAttachmentsInfo();
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error, Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void refreshAttachmentsInfo() {
        btnAttachments.setText(attachList.size() + "");
    }

}
