package org.softeg.slartus.forpdaplus.fragments.topic;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.post.EditAttach;
import org.softeg.slartus.forpdaapi.post.EditPost;
import org.softeg.slartus.forpdaapi.post.PostApi;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ImageFilePath;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.listfragments.IBrickFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by radiationx on 30.10.15.
 */
public class EditPostFragment extends GeneralFragment {

    public static final int NEW_EDIT_POST_REQUEST_CODE = App.getInstance().getUniqueIntValue();
    public static final String TOPIC_BODY_KEY = "EditPostActivity.TOPIC_BODY_KEY";

    public static final String POST_URL_KEY = "EditPostActivity.POST_URL_KEY";
    private EditText txtPost, txtpost_edit_reason;

    private Button btnAttachments;
    private ImageButton btnUpload;
    private ProgressBar progress_search;
    private EditPost m_EditPost;

    private ArrayList<String> m_AttachFilePaths = new ArrayList<>();
    private String lastSelectDirPath = Environment.getExternalStorageDirectory().getPath();

    final Handler uiHandler = new Handler();

    private final int REQUEST_SAVE = 0;
    private final int REQUEST_SAVE_IMAGE = 1;

    private String parentTag = "";

    private View m_BottomPanel;
    private PopupPanelView mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);


    public static final String thisFragmentUrl = "EditPostFragment";

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        mPopupPanelView.hidePopupWindow();
    }

    public static EditPostFragment newInstance(Context context, Bundle args){
        EditPostFragment fragment = new EditPostFragment();

        fragment.setArguments(args);
        return fragment;
    }

    public static void editPost(Activity context, String forumId, String topicId, String postId, String authKey, String tag) {
        String url = thisFragmentUrl+forumId+topicId+postId;
        Bundle args = new Bundle();
        args.putString("forumId", forumId);
        args.putString("themeId", topicId);
        args.putString("postId", postId);
        args.putString("authKey", authKey);
        args.putString("parentTag", tag);
        MainActivity.addTab("Ред. сообщения в " + App.getInstance().getTabByTag(tag).getTitle(), url, newInstance(context, args));
    }

    public static void newPost(Activity context, String forumId, String topicId, String authKey,
                               final String body, String tag) {
        String url = thisFragmentUrl+forumId+topicId+PostApi.NEW_POST_ID;
        Bundle args = new Bundle();
        args.putString("forumId", forumId);
        args.putString("themeId", topicId);
        args.putString("postId", PostApi.NEW_POST_ID);
        args.putString("body", body);
        args.putString("authKey", authKey);
        args.putString("parentTag", tag);
        MainActivity.addTab("Ответ в " + App.getInstance().getTabByTag(tag).getTitle(), url, newInstance(context, args));
    }

    public static void newPostWithAttach(Context context, String forumId, String topicId, String authKey,
                                         final Bundle extras) {
        String url = thisFragmentUrl+forumId+topicId+PostApi.NEW_POST_ID;
        Bundle args = new Bundle();
        args.putString("forumId", forumId);
        args.putString("themeId", topicId);
        args.putString("postId", PostApi.NEW_POST_ID);
        args.putBundle("extras", extras);
        args.putString("authKey", authKey);
        MainActivity.addTab("Ред. сообщения", url, newInstance(context, args));
    }

    View view;
    private View findViewById(int id){
        return view.findViewById(id);
    }
    public ActionBar getSupportActionBar() {
        return ((AppCompatActivity)getMainActivity()).getSupportActionBar();
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean closeTab() {
        if (!TextUtils.isEmpty(txtPost.getText())) {
            new MaterialDialog.Builder(getMainActivity())
                    .title("Подтвердите действие")
                    .content("Имеется введенный текст сообщения! Закрыть?")
                    .positiveText("Да")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            getMainActivity().removeTab(getTag());
                        }
                    })
                    .negativeText("Отмена")
                    .show();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
        if(mPopupPanelView!=null)
            mPopupPanelView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeArrow();
        if(mPopupPanelView!=null)
            mPopupPanelView.pause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_post_plus, container, false);
        setHasOptionsMenu(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        progress_search = (ProgressBar) findViewById(R.id.progress_search);
        lastSelectDirPath = prefs.getString("EditPost.AttachDirPath", lastSelectDirPath);

        m_BottomPanel = findViewById(R.id.bottomPanel);

        txtPost = (EditText) findViewById(R.id.txtPost);

        txtpost_edit_reason = (EditText) findViewById(R.id.txtpost_edit_reason);
        txtPost.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });


        findViewById(R.id.btnSendPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail();
            }
        });

        btnAttachments = (Button) findViewById(R.id.btnAttachments);
        btnAttachments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showAttachesListDialog();
            }
        });

        btnUpload = (ImageButton) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startAddAttachment();
            }
        });

        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), txtPost);
        mPopupPanelView.activityCreated(getMainActivity(), view);


        try {
            Bundle args = getArguments();


            String forumId = args.getString("forumId");
            String topicId = args.getString("themeId");
            String postId = args.getString("postId");
            String authKey = args.getString("authKey");
            parentTag = args.getString("parentTag");
            m_EditPost = new EditPost();
            m_EditPost.setId(postId);
            m_EditPost.setForumId(forumId);
            m_EditPost.setTopicId(topicId);
            m_EditPost.setAuthKey(authKey);
            mPopupPanelView.setTopic(forumId, topicId, authKey);

            if (isNewPost()) {
                if (args.getString("body")!=null) {
                    txtPost.setText(args.getString("body"));
                    txtPost.setSelection(txtPost.getText().length());
                }
            }
            setDataFromExtras(args.getBundle("extras"));

            startLoadPost(forumId, topicId, postId, authKey);
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
            getMainActivity().removeTab(getTag());
        }
        //createActionMenu();
        return view;
    }


    @Override
    public boolean onBackPressed() {
        if (!TextUtils.isEmpty(txtPost.getText())) {
            new MaterialDialog.Builder(getMainActivity())
                    .title("Подтвердите действие")
                    .content("Имеется введенный текст сообщения! Закрыть?")
                    .positiveText("Да")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            getMainActivity().removeTab(getTag());
                        }
                    })
                    .negativeText("Отмена")
                    .show();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    private boolean sendMail() {
        final String body = getPostText();
        if (TextUtils.isEmpty(body))
            return true;

        if (Preferences.Topic.getConfirmSend()) {
            new MaterialDialog.Builder(getContext())
                    .title("Уверены?")
                    .content("Подтвердите отправку")
                    .positiveText("ОК")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            sendPost(body, getEditReasonText());
                        }
                    })
                    .negativeText("Отмена")
                    .show();
        } else {
            sendPost(body, getEditReasonText());
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (m_EditPost != null)
            outState.putSerializable("EditPost", m_EditPost);
        if (m_AttachFilePaths != null)
            outState.putStringArray("AttachFilePaths", m_AttachFilePaths.toArray(new String[m_AttachFilePaths.size()]));
        outState.putString("lastSelectDirPath", lastSelectDirPath);
        outState.putString("postText", getPostText());
        outState.putString("txtpost_edit_reason", getEditReasonText());


        super.onSaveInstanceState(outState);
    }

    //@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;
        if (savedInstanceState.containsKey("EditPost"))
            m_EditPost = (EditPost) savedInstanceState.getSerializable("EditPost");
        if (savedInstanceState.containsKey("AttachFilePaths") && savedInstanceState.getStringArray("AttachFilePaths") != null)
            m_AttachFilePaths = new ArrayList<>(Arrays.asList(savedInstanceState.getStringArray("AttachFilePaths")));
        lastSelectDirPath = savedInstanceState.getString("lastSelectDirPath");

        txtPost.setText(savedInstanceState.getString("postText"));
        txtpost_edit_reason.setText(savedInstanceState.getString("txtpost_edit_reason"));
        mPopupPanelView.setTopic(m_EditPost.getForumId(), m_EditPost.getTopicId(), m_EditPost.getAuthKey());

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setDataFromExtras(Bundle extras) throws NotReportException {
        if (extras==null) return;
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            Object attachesObject = extras.get(Intent.EXTRA_STREAM);
            if (attachesObject instanceof Uri) {
                Uri uri = (Uri) extras.get(Intent.EXTRA_STREAM);
                m_AttachFilePaths = new ArrayList<>(Arrays.asList(new String[]{ImageFilePath.getPath(getMainActivity().getApplicationContext(), uri)}));
            } else if (attachesObject instanceof ArrayList<?>) {
                m_AttachFilePaths = new ArrayList<>();
                ArrayList<?> list = (ArrayList<?>) attachesObject;
                for (Object item : list) {
                    Uri uri = (Uri) item;
                    m_AttachFilePaths.add(ImageFilePath.getPath(getMainActivity().getApplicationContext(), uri));
                }
            }
        }

        if (extras.containsKey(Intent.EXTRA_TEXT))
            txtPost.setText(extras.get(Intent.EXTRA_TEXT).toString());
        if (extras.containsKey(Intent.EXTRA_HTML_TEXT))
            txtPost.setText(extras.get(Intent.EXTRA_HTML_TEXT).toString());
        if (isNewPost()) {
            if (extras.containsKey("body"))
                txtPost.setText(extras.get("body").toString());
        }
        txtPost.setSelection(txtPost.getText().length());
    }
    Menu menu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item;

        if (!isNewPost()) {
            item = menu.add("Причина редактирования").setIcon(R.drawable.ic_pencil_white_24dp);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    toggleEditReasonDialog();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        item = menu.add("Поиск по тексту");
        item.setActionView(R.layout.action_collapsible_search);
        searchEditText = (EditText) item.getActionView().findViewById(R.id.editText);
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String text = searchEditText.getText() == null ? "" : searchEditText.getText().toString().trim();
                    startSearch(text, true);
                    searchEditText.requestFocus();
                    return true;
                }

                return false;
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable mEdit) {
                String text = mEdit.toString().trim();
                startSearch(text, false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        // Переделать для appcompat
            /*item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    getInterface().searchEditText.requestFocus();
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    getInterface().txtPost.setText(getInterface().clearPostHighlight());
                    return true;
                }
            });*/
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        /*item = menu.add("Скрыть панели");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                hidePanels();
                return true;
            }
        });

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        */
        this.menu = menu;
    }


    private Boolean isNewPost() {
        return PostApi.NEW_POST_ID.equals(m_EditPost.getId());
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
        if (m_EditPost.getAttaches().size() == 0) {
            new MaterialDialog.Builder(getMainActivity())
                    .content("Нет ни одного вложения, загрузить?")
                    .positiveText("Загрузить")
                    .negativeText("Отмена")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            startAddAttachment();
                        }
                    }).show();
            return;
        }
        AttachesAdapter adapter = new AttachesAdapter(m_EditPost.getAttaches(), getMainActivity());
        mAttachesListDialog = new MaterialDialog.Builder(getMainActivity())
                .cancelable(true)
                .title("Вложения")
                        //.setSingleChoiceItems(adapter, -1, null)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    }
                })
                .neutralText("В спойлер")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        List<String> listItems = new ArrayList<String>();
                        int i = 0;
                        while (i <= (m_EditPost.getAttaches().size()-1)) {
                            listItems.add(m_EditPost.getAttaches().get(i).getName());
                            i++;
                        }
                        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
                        final StringBuilder str = new StringBuilder();
                        new MaterialDialog.Builder(getContext())
                                .title("Добавить в спойлер")
                                .positiveText("Добавить")
                                .negativeText("Отмена")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        int selectionStart = txtPost.getSelectionStart();
                                        if (selectionStart == -1)
                                            selectionStart = 0;
                                        if (txtPost.getText() != null)
                                            //txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getTitle() + "]");
                                            txtPost.getText().insert(selectionStart, "[spoiler]"+str.toString()+"[/spoiler]");

                                    }
                                })
                                .items(items)
                                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                        str.setLength(0);
                                        for (int i = 0; i < which.length; i++) {
                                            str.append("[attachment=" + m_EditPost.getAttaches().get(which[i]).getId() + ":" + m_EditPost.getAttaches().get(which[i]).getName() + "]");
                                        }
                                        return true; // allow selection
                                    }
                                })
                                .alwaysCallMultiChoiceCallback()
                                .show();
                    }
                })
                .negativeText("Отмена")
                .build();
        mAttachesListDialog.show();
    }
    private static final int MY_INTENT_CLICK=302;
    private void startAddAttachment() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Нет прав для данного действия", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence[] items = new CharSequence[]{"Файл", "Изображение"};
        new MaterialDialog.Builder(getContext())
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int i, CharSequence items) {
                        switch (i) {
                            case 0://файл

                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("file/*");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                                    startActivityForResult(intent, MY_INTENT_CLICK);

                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(getMainActivity(), "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    AppLog.e(getMainActivity(), ex);
                                }

                                break;
                            case 1:// Изображение

                                try {
                                    Intent imageintent = new Intent(
                                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                        imageintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    startActivityForResult(imageintent, MY_INTENT_CLICK);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(getMainActivity(), "Ни одно приложение не установлено для выбора изображения!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    AppLog.e(getMainActivity(), ex);
                                }
                                break;
                        }
                    }
                })
                .show();
    }

    private void saveAttachDirPath(String attachFilePath) {
        lastSelectDirPath = FileUtils.getDirPath(attachFilePath);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EditPost.AttachDirPath", lastSelectDirPath);
        editor.commit();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == MY_INTENT_CLICK)
            {
                if (null == data) return;
                Uri selectedImageUri = data.getData();
                String selectedImagePath = ImageFilePath.getPath(getMainActivity().getApplicationContext(), selectedImageUri);
                saveAttachDirPath(selectedImagePath);
                new UpdateTask(getMainActivity(), selectedImagePath).execute();

            }
        }
    }














    private void startLoadPost(String forumId, String topicId, String postId, String authKey) {
        new LoadTask(getMainActivity(), forumId, topicId, postId, authKey).execute();
    }

    private void sendPost(final String text, String editPostReason) {

        if (isNewPost()) {
            new PostTask(getMainActivity(), text, editPostReason,
                    Preferences.Topic.Post.getEnableEmotics(), Preferences.Topic.Post.getEnableSign())
                    .execute();
        } else {
            new AcceptEditTask(getMainActivity(), text, editPostReason,
                    Preferences.Topic.Post.getEnableEmotics(), Preferences.Topic.Post.getEnableSign())
                    .execute();
        }
    }

    public void toggleEditReasonDialog() {
        txtpost_edit_reason.setVisibility(txtpost_edit_reason.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }


    public String getPostText() {
        return txtPost.getText() == null ? "" : txtPost.getText().toString();
    }

    public String getEditReasonText() {
        return txtpost_edit_reason.getText() == null ? "" : txtpost_edit_reason.getText().toString();
    }

    private final String TEMP_EMPTY_TEXT = "<temptext>";


    private class UpdateTask extends AsyncTask<String, Pair<String, Integer>, Boolean> {
        private final MaterialDialog dialog;
        private ProgressState m_ProgressState;

        private List<String> attachFilePaths;

        public UpdateTask(Context context, List<String> attachFilePaths) {

            this.attachFilePaths = attachFilePaths;
            dialog = new MaterialDialog.Builder(context)
                    .progress(false, 100, false)
                    .content("Отправка файла")
                    .show();
        }

        public UpdateTask(Context context, String newAttachFilePath) {
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
                    publishProgress(new Pair<>(String.format("Отправка файла %d из %d", i++, attachFilePaths.size()), 0));
                    editAttach = PostApi.attachFile(Client.getInstance(),
                            m_EditPost.getId(), newAttachFilePath, m_ProgressState);
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
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    m_ProgressState.cancel();
                    cancel(false);
                }
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
                m_EditPost.addAttach(editAttach);
                refreshAttachmentsInfo();
            } else {

                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Boolean success) {
            super.onCancelled(success);
            if (success || (isCancelled() && editAttach != null)) {
                m_EditPost.addAttach(editAttach);
                refreshAttachmentsInfo();
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    public void onDestroy() {
        if (mPopupPanelView != null) {
            mPopupPanelView.destroy();
            mPopupPanelView = null;
        }
        super.onDestroy();
    }

    private class DeleteAttachTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;

        private String attachId;

        public DeleteAttachTask(Context context, String attachId) {

            this.attachId = attachId;

            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .content("Удаление файла")
                    .build();
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                PostApi.deleteAttachedFile(Client.getInstance(), m_EditPost.getId(), attachId);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                m_EditPost.deleteAttach(attachId);
                refreshAttachmentsInfo();
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AcceptEditTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;
        private String postBody;
        private String postEditReason;
        private Boolean enableEmo;
        private Boolean enableSign;

        public AcceptEditTask(Context context,
                              String postBody, String postEditReason, Boolean enableEmo, Boolean enableSign) {
            this.postBody = postBody;
            this.postEditReason = postEditReason;
            this.enableEmo = enableEmo;
            this.enableSign = enableSign;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .content("Редактирование сообщения")
                    .build();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                PostApi.sendPost(Client.getInstance(), m_EditPost.getParams(), postBody,
                        postEditReason, enableSign, enableEmo);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if(App.getInstance().isContainsByTag(parentTag)){
                    ((ThemeFragment)App.getInstance().getTabByTag(parentTag).getFragment())
                            .showTheme(ThemeFragment.getThemeUrl(m_EditPost.getTopicId(), "view=findpost&p=" + m_EditPost.getId()), true);
                    getMainActivity().removeTab(getTag());
                    MainActivity.selectTabByTag(parentTag);
                }else {
                    getMainActivity().removeTab(getTag());
                }

            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void setEditPost(EditPost editPost) {
        m_EditPost = editPost;
        if (!PostApi.NEW_POST_ID.equals(m_EditPost.getId()))
            txtPost.setText(m_EditPost.getBody());
        txtpost_edit_reason.setText(m_EditPost.getPostEditReason());
        refreshAttachmentsInfo();
    }

    private void refreshAttachmentsInfo() {
        btnAttachments.setText(m_EditPost.getAttaches().size() + "");
    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;
        private String forumId;
        private String topicId;
        private String postId;
        private String authKey;

        public LoadTask(Context context, String forumId, String topicId, String postId, String authKey) {
            this.forumId = forumId;
            this.topicId = topicId;
            this.postId = postId;
            this.authKey = authKey;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancel(true);
                        }
                    })
                    .content("Загрузка сообщения")
                    .build();
        }

        private EditPost editPost;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                editPost = PostApi.editPost(Client.getInstance(), forumId, topicId, postId, authKey);

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            this.dialog.show();
        }

        private Throwable ex;

        protected void onCancelled() {
            Toast.makeText(getMainActivity(), "Отменено", Toast.LENGTH_SHORT).show();
            //finish();
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                setEditPost(editPost);

                if (m_AttachFilePaths.size() > 0)
                    new UpdateTask(getMainActivity(), m_AttachFilePaths)
                            .execute();
                m_AttachFilePaths = new ArrayList<>();
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;
        private String mPostResult = null;// при удачной отправке страница топика
        private String mError = null;
        private String postBody;
        private String postEditReason;
        private Boolean enableEmo;
        private Boolean enableSign;

        public PostTask(Context context,
                        String postBody, String postEditReason, Boolean enableEmo, Boolean enableSign) {
            this.postBody = postBody;
            this.postEditReason = postEditReason;
            this.enableEmo = enableEmo;
            this.enableSign = enableSign;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .content("Отправка сообщения")
                    .build();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = PostApi.sendPost(Client.getInstance(), m_EditPost.getParams(), postBody,
                        postEditReason, enableSign, enableEmo);

                mError = PostApi.checkPostErrors(mPostResult);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            this.dialog.show();
        }

        private Exception ex;

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (!TextUtils.isEmpty(mError)) {
                    Toast.makeText(getMainActivity(), "Ошибка: " + mError, Toast.LENGTH_LONG).show();
                    return;
                }
                if(App.getInstance().isContainsByTag(parentTag)){
                    ((ThemeFragment)App.getInstance().getTabByTag(parentTag).getFragment())
                            .showTheme(String.format("http://4pda.ru/forum/index.php?showtopic=%s&%s", m_EditPost.getTopicId(),
                                    isNewPost() ? "view=getlastpost" : "view=findpost&p=" + m_EditPost.getId()), true);
                    getMainActivity().removeTab(getTag());
                    MainActivity.selectTabByTag(parentTag);
                }else {
                    getMainActivity().removeTab(getTag());
                }


            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class AttachesAdapter extends BaseAdapter {
        private Activity activity;
        private final List<EditAttach> content;

        public AttachesAdapter(List<EditAttach> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public EditAttach getItem(int i) {
            return content.get(i);
        }

        public long getItemId(int i) {
            return i;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                final LayoutInflater inflater = activity.getLayoutInflater();

                convertView = inflater.inflate(R.layout.attachment_spinner_item, parent, false);


                holder = new ViewHolder();


                assert convertView != null;
                holder.btnDelete = (ImageButton) convertView
                        .findViewById(R.id.btnDelete);
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();

                        EditAttach attach = (EditAttach) view.getTag();

                        new DeleteAttachTask(getMainActivity(),
                                attach.getId())
                                .execute();
                    }
                });

                holder.btnSpoiler = (ImageButton) convertView
                        .findViewById(R.id.btnSpoiler);
                holder.btnSpoiler.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();

                        int selectionStart = txtPost.getSelectionStart();
                        if (selectionStart == -1)
                            selectionStart = 0;
                        EditAttach attach = (EditAttach) view.getTag();
                        if (txtPost.getText() != null)
                            txtPost.getText().insert(selectionStart, "[spoiler][attachment=" + attach.getId() + ":" + attach.getName() + "][/spoiler]");
                    }
                });

                holder.txtFile = (TextView) convertView
                        .findViewById(R.id.txtFile);
                holder.txtFile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();
                        int selectionStart = txtPost.getSelectionStart();
                        if (selectionStart == -1)
                            selectionStart = 0;
                        EditAttach attach = (EditAttach) view.getTag();
                        if (txtPost.getText() != null)
                            txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            EditAttach attach = this.getItem(position);
            holder.btnDelete.setTag(attach);
            holder.btnSpoiler.setTag(attach);
            holder.txtFile.setText(attach.getName());
            holder.txtFile.setTag(attach);

            return convertView;
        }

        public class ViewHolder {

            ImageButton btnSpoiler;
            ImageButton btnDelete;
            TextView txtFile;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            m_BottomPanel.setVisibility(View.VISIBLE);
        }
        return;
    }

    public void hidePanels() {
        getSupportActionBar().hide();
        m_BottomPanel.setVisibility(View.GONE);
    }

    private final int SEARCH_RESULT_FOUND = 1;
    private final int SEARCH_RESULT_NOTFOUND = 0;
    private final int SEARCH_RESULT_EMPTYTEXT = -1;

    private Spannable clearPostHighlight() {
        int startSearchSelection = txtPost.getSelectionStart();
        Spannable raw = new SpannableString(txtPost.getText() == null ? "" : txtPost.getText());
        BackgroundColorSpan[] spans = raw.getSpans(0,
                raw.length(),
                BackgroundColorSpan.class);

        for (BackgroundColorSpan span : spans) {
            raw.removeSpan(span);
        }
        txtPost.setSelection(startSearchSelection);
        txtPost.setCursorVisible(true);
        return raw;
    }

    private Timer m_SearchTimer = null;

    public void startSearch(final String searchText, final Boolean fromSelection) {

        if (m_SearchTimer != null) {
            m_SearchTimer.cancel();
            m_SearchTimer.purge();
        }
        m_SearchTimer = new Timer();
        m_SearchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (search(searchText, fromSelection) == SEARCH_RESULT_NOTFOUND)
                            searchEditText.setError("Совпадений не найдено");
                        else
                            searchEditText.setError(null);

                    }
                });
                m_SearchTimer.cancel();
                m_SearchTimer.purge();
            }
        }, 1000, 5000);


    }

    public int search(String searchText, Boolean fromSelection) {
        if (TextUtils.isEmpty(searchText)) return SEARCH_RESULT_EMPTYTEXT;
        try {
            progress_search.setVisibility(View.VISIBLE);

            searchText = searchText.toLowerCase();
            Spannable raw = clearPostHighlight();

            int startSearchSelection = 0;
            if (fromSelection)
                startSearchSelection = txtPost.getSelectionStart() + 1;
            String text = raw.toString().toLowerCase();


            int findedStartSelection = TextUtils.indexOf(text, searchText, startSearchSelection);
            if (findedStartSelection == -1 && startSearchSelection != 0)
                findedStartSelection = TextUtils.indexOf(text, searchText);

            if (findedStartSelection == -1)
                return SEARCH_RESULT_NOTFOUND;

            raw.setSpan(new BackgroundColorSpan(0xFF8B008B), findedStartSelection, findedStartSelection
                    + searchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


            txtPost.setText(raw);
            txtPost.setSelection(findedStartSelection);
            txtPost.setCursorVisible(true);
            return SEARCH_RESULT_FOUND;
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
        } finally {
            if (!fromSelection)
                searchEditText.requestFocus();
            progress_search.setVisibility(View.GONE);
        }
        return SEARCH_RESULT_EMPTYTEXT;
    }

    public EditText searchEditText;



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


}
