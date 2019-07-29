package org.softeg.slartus.forpdaplus.fragments.topic;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.post.EditAttach;
import org.softeg.slartus.forpdaapi.post.EditPost;
import org.softeg.slartus.forpdaapi.post.PostApi;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ImageFilePath;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.tabs.TabItem;

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
    private ProgressBar progress_search;
    private EditPost m_EditPost;

    private ArrayList<String> m_AttachFilePaths = new ArrayList<>();
    private String lastSelectDirPath = Environment.getExternalStorageDirectory().getPath();

    final Handler uiHandler = new Handler();

    private String parentTag = "";
    private boolean emptyText = true;

    private View m_BottomPanel;
    private PopupPanelView mPopupPanelView;


    public static final String thisFragmentUrl = "EditPostFragment";

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        mPopupPanelView.hidePopupWindow();
    }

    public static EditPostFragment newInstance(Bundle args) {
        EditPostFragment fragment = new EditPostFragment();

        fragment.setArguments(args);
        return fragment;
    }

    public static void editPost(Activity context, String forumId, String topicId, String postId, String authKey, String tag) {
        String url = thisFragmentUrl + forumId + topicId + postId;
        Bundle args = new Bundle();
        args.putString("forumId", forumId);
        args.putString("themeId", topicId);
        args.putString("postId", postId);
        args.putString("authKey", authKey);
        args.putString("parentTag", tag);
        MainActivity.addTab(context.getString(R.string.edit_post_combined) + context.getString(R.string.combined_in) + App.getInstance().getTabByTag(tag).getTitle(), url, newInstance(args));
    }

    public static void newPost(Activity context, String forumId, String topicId, String authKey,
                               final String body, String tag) {
        String url = thisFragmentUrl + forumId + topicId + PostApi.NEW_POST_ID;
        Bundle args = new Bundle();
        args.putString("forumId", forumId);
        args.putString("themeId", topicId);
        args.putString("postId", PostApi.NEW_POST_ID);
        args.putString("body", body);
        args.putString("authKey", authKey);
        args.putString("parentTag", tag);
        MainActivity.addTab(context.getString(R.string.answer) + context.getString(R.string.combined_in) + App.getInstance().getTabByTag(tag).getTitle(), url, newInstance(args));
    }

    public static void newPostWithAttach(Context context, String forumId, String topicId, String authKey,
                                         final Bundle extras) {
        String url = thisFragmentUrl + forumId + topicId + PostApi.NEW_POST_ID;
        Bundle args = new Bundle();
        args.putString("forumId", forumId);
        args.putString("themeId", topicId);
        args.putString("postId", PostApi.NEW_POST_ID);
        args.putBundle("extras", extras);
        args.putString("authKey", authKey);
        MainActivity.addTab(context.getString(R.string.edit_post_combined), url, newInstance(args));
    }

    public ActionBar getSupportActionBar() {
        return getMainActivity().getSupportActionBar();
    }

    @Override
    public boolean closeTab() {
        if (!TextUtils.isEmpty(txtPost.getText())) {
            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.confirm_action)
                    .content(R.string.text_not_empty)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> getMainActivity().tryRemoveTab(getTag()))
                    .negativeText(R.string.cancel)
                    .show();
            return true;
        } else {
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
        if (mPopupPanelView != null)
            mPopupPanelView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPopupPanelView != null)
            mPopupPanelView.pause();
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_post_plus, container, false);

        progress_search = (ProgressBar) findViewById(R.id.progress_search);
        lastSelectDirPath = App.getInstance().getPreferences().getString("EditPost.AttachDirPath", lastSelectDirPath);

        m_BottomPanel = findViewById(R.id.bottomPanel);

        final Button send_button = view.findViewById(R.id.btnSendPost);
        send_button.setOnClickListener(view -> sendMail());

        txtPost = (EditText) findViewById(R.id.txtPost);

        txtpost_edit_reason = (EditText) findViewById(R.id.txtpost_edit_reason);
        txtPost.setOnEditorActionListener((v, actionId, event) -> false);
        txtPost.addTextChangedListener(new TextWatcher() {
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
                        send_button.setTextColor(ContextCompat.getColor(App.getContext(), R.color.accentGray));
                        emptyText = true;
                    }
                } else {
                    if (emptyText) {
                        send_button.setTextColor(ContextCompat.getColor(App.getContext(), R.color.accent));
                        emptyText = false;
                    }
                }
            }
        });


        btnAttachments = (Button) findViewById(R.id.btnAttachments);
        btnAttachments.setOnClickListener(view -> showAttachesListDialog());

        ImageButton btnUpload = (ImageButton) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(view -> startAddAttachment());

        if (mPopupPanelView == null)
            mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);
        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), txtPost);
        mPopupPanelView.activityCreated(getMainActivity(), view);


        try {
            Bundle args = getArguments();
            assert args != null;
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
                if (args.getString("body") != null) {
                    txtPost.setText(args.getString("body"));
                    txtPost.setSelection(txtPost.getText().length());
                }
            }
            setDataFromExtras(args.getBundle("extras"));

            startLoadPost(forumId, topicId, postId, authKey);
        } catch (Throwable ex) {
            AppLog.e(getMainActivity(), ex);
            getMainActivity().tryRemoveTab(getTag());
        }
        //createActionMenu();
        return view;
    }


    @Override
    public boolean onBackPressed() {
        if (!TextUtils.isEmpty(txtPost.getText())) {
            new MaterialDialog.Builder(getMainActivity())
                    .title(R.string.confirm_action)
                    .content(getString(R.string.text_not_empty))
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> getMainActivity().tryRemoveTab(getTag()))
                    .negativeText(R.string.cancel)
                    .show();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    private void sendMail() {
        if (emptyText) {
            Toast toast = Toast.makeText(getContext(), R.string.enter_message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, App.getInstance().getResources().getDisplayMetrics()));
            toast.show();
            return;
        }
        final String body = getPostText();
        if (Preferences.Topic.getConfirmSend()) {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.is_sure)
                    .content(R.string.confirm_sending)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> sendPost(body, getEditReasonText()))
                    .negativeText(R.string.cancel)
                    .show();
        } else {
            sendPost(body, getEditReasonText());
        }
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setDataFromExtras(Bundle extras) {
        if (extras == null) return;
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            Object attachesObject = extras.get(Intent.EXTRA_STREAM);
            if (attachesObject instanceof Uri) {
                Uri uri = (Uri) extras.get(Intent.EXTRA_STREAM);
                String path = ImageFilePath.getPath(getMainActivity().getApplicationContext(), uri);
                if (path != null)
                    m_AttachFilePaths = new ArrayList<>(Arrays.asList(path));
                else
                    Toast.makeText(getContext(), "Не могу прикрепить файл", Toast.LENGTH_SHORT).show();
            } else if (attachesObject instanceof ArrayList<?>) {
                m_AttachFilePaths = new ArrayList<>();
                ArrayList<?> list = (ArrayList<?>) attachesObject;
                for (Object item : list) {
                    Uri uri = (Uri) item;
                    String path = ImageFilePath.getPath(getMainActivity().getApplicationContext(), uri);
                    if (path != null)
                        m_AttachFilePaths.add(ImageFilePath.getPath(getMainActivity().getApplicationContext(), uri));
                    else
                        Toast.makeText(getContext(), "Не могу прикрепить файл", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item;

        if (!isNewPost()) {
            item = menu.add(R.string.reason_for_editing).setIcon(R.drawable.pencil);
            item.setOnMenuItemClickListener(menuItem -> {
                toggleEditReasonDialog();
                return true;
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        menu.add(R.string.preview).setOnMenuItemClickListener(item1 -> {
            TabItem tabItem = App.getInstance().getTabByUrl("preview_" + getTag());
            if (tabItem == null) {
                PostPreviewFragment.showSpecial(getPostText(), getTag());
            } else {
                ((PostPreviewFragment) tabItem.getFragment()).load(getPostText());
                getMainActivity().selectTab(tabItem);
                getMainActivity().hidePopupWindows();
            }
            return true;
        });
        item = menu.add(R.string.find_in_text);
        item.setActionView(R.layout.action_collapsible_search);
        searchEditText = item.getActionView().findViewById(R.id.editText);
        searchEditText.setOnKeyListener((view, keyCode, keyEvent) -> {
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String text = searchEditText.getText() == null ? "" : searchEditText.getText().toString().trim();
                startSearch(text, true);
                searchEditText.requestFocus();
                return true;
            }

            return false;
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

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    }


    private Boolean isNewPost() {
        return PostApi.NEW_POST_ID.equals(m_EditPost.getId());
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
        if (m_EditPost.getAttaches().size() == 0) {
            new MaterialDialog.Builder(getMainActivity())
                    .content(R.string.no_attachments)
                    .positiveText(R.string.do_download)
                    .negativeText(R.string.cancel)
                    .onPositive((dialog, which) -> startAddAttachment())
                    .show();
            return;
        }
        AttachesAdapter adapter = new AttachesAdapter(m_EditPost.getAttaches());
        mAttachesListDialog = new MaterialDialog.Builder(getMainActivity())
                .cancelable(true)
                .title(R.string.attachments)
                //.setSingleChoiceItems(adapter, -1, null)
                .adapter(adapter, new LinearLayoutManager(getActivity()))
                .neutralText(R.string.in_spoiler)
                .onNeutral((dialog, which) -> {
                            List<String> listItems = new ArrayList<>();
                            int i = 0;
                            while (i <= (m_EditPost.getAttaches().size() - 1)) {
                                listItems.add(m_EditPost.getAttaches().get(i).getName());
                                i++;
                            }
                            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
                            final StringBuilder str = new StringBuilder();
                            new MaterialDialog.Builder(getContext())
                                    .title(R.string.add_in_spoiler)
                                    .positiveText(R.string.add)
                                    .negativeText(R.string.cancel)
                                    .onPositive((dialog1, which1) -> {
                                        int selectionStart = txtPost.getSelectionStart();
                                        if (selectionStart == -1)
                                            selectionStart = 0;
                                        if (txtPost.getText() != null)
                                            //txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getTitle() + "]");
                                            txtPost.getText().insert(selectionStart, "[spoiler]" + str.toString() + "[/spoiler]");
                                    })
                                    .items(items)
                                    .itemsCallbackMultiChoice(null, (dialog12, which12, text) -> {
                                        str.setLength(0);
                                        for (Integer which1 : which12) {
                                            str.append("[attachment=")
                                                    .append(m_EditPost.getAttaches().get(which1).getId())
                                                    .append(":")
                                                    .append(m_EditPost.getAttaches().get(which1).getName())
                                                    .append("]");
                                        }
                                        return true; // allow selection
                                    })
                                    .alwaysCallMultiChoiceCallback()
                                    .show();
                        }
                )
                .negativeText(R.string.cancel)
                .build();
        mAttachesListDialog.show();
    }

    private static final int MY_INTENT_CLICK_I = 302;
    private static final int MY_INTENT_CLICK_F = 303;

    private void startAddAttachment() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), R.string.no_permission, Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence[] items = new CharSequence[]{getString(R.string.file), getString(R.string.image)};
        new MaterialDialog.Builder(getContext())
                .items(items)
                .itemsCallback((dialog, view, i, items1) -> {
                    switch (i) {
                        case 0://файл
                            try {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                startActivityForResult(intent, MY_INTENT_CLICK_F);

                            } catch (ActivityNotFoundException ex) {
                                Toast.makeText(getMainActivity(), R.string.no_app_for_get_file, Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                AppLog.e(getMainActivity(), ex);
                            }

                            break;
                        case 1:// Изображение

                            try {
                                Intent imageintent = new Intent(
                                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                    imageintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                startActivityForResult(imageintent, MY_INTENT_CLICK_I);
                            } catch (ActivityNotFoundException ex) {
                                Toast.makeText(getMainActivity(), R.string.no_app_for_get_image_file, Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                AppLog.e(getMainActivity(), ex);
                            }
                            break;
                    }
                })
                .show();
    }

    private void saveAttachDirPath(String attachFilePath) {
        lastSelectDirPath = FileUtils.getDirPath(attachFilePath);
        App.getInstance().getPreferences().edit().putString("EditPost.AttachDirPath", lastSelectDirPath).apply();
    }

    private void helperTask(String path) {
        saveAttachDirPath(path);
        new UpdateTask(getMainActivity(), path).execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_INTENT_CLICK_I) {
                if (null == data) return;
                Uri selectedImageUri = data.getData();
                String selectedImagePath = ImageFilePath.getPath(getMainActivity().getApplicationContext(), selectedImageUri);
                if (selectedImagePath != null)
                    helperTask(selectedImagePath);
                else
                    Toast.makeText(getContext(), "Не могу прикрепить файл", Toast.LENGTH_SHORT).show();


            } else if (requestCode == MY_INTENT_CLICK_F) {
                if (null == data) return;
                String path = ImageFilePath.getPath(getMainActivity().getApplicationContext(), data.getData());
                if (path != null) {
                    if (path.matches("(?i)(.*)(7z|zip|rar|tar.gz|exe|cab|xap|txt|log|jpeg|jpg|png|gif|mp3|mp4|apk|ipa|img|.mtz)$")) {
                        helperTask(path);
                    } else {
                        Toast.makeText(getMainActivity(), R.string.file_not_support_forum, Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getContext(), "Не могу прикрепить файл", Toast.LENGTH_SHORT).show();


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
            this(context, new ArrayList<>(Arrays.asList(newAttachFilePath)));
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
                m_EditPost.addAttach(editAttach);
                refreshAttachmentsInfo();

//                if (!pathToFile.isEmpty()) {
//                    File deleteFile = new File(pathToFile);
//                    if (deleteFile.exists()) {
//                        deleteFile.delete();
//                    }
//                }

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
                m_EditPost.addAttach(editAttach);
                refreshAttachmentsInfo();
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error, Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    public void onDestroy() {
        if (mPopupPanelView != null) {
            mPopupPanelView.destroy();
            mPopupPanelView = null;
        }
        TabItem tabItem = App.getInstance().getTabByUrl("preview_" + getTag());
        if (tabItem != null) getMainActivity().tryRemoveTab(tabItem.getTag());
        super.onDestroy();
    }

    private class DeleteAttachTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;

        private String attachId;

        DeleteAttachTask(Context context, String attachId) {
            this.attachId = attachId;

            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.deleting_file)
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
                    Toast.makeText(getMainActivity(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AcceptEditTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;
        private String postBody;
        private String postEditReason;
        private Boolean enableEmo;
        private Boolean enableSign;

        AcceptEditTask(Context context,
                       String postBody, String postEditReason, Boolean enableEmo, Boolean enableSign) {
            this.postBody = postBody;
            this.postEditReason = postEditReason;
            this.enableEmo = enableEmo;
            this.enableSign = enableSign;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.edit_message)
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
                if (App.getInstance().isContainsByTag(parentTag)) {
                    ((ThemeFragment) App.getInstance().getTabByTag(parentTag).getFragment())
                            .showTheme(ThemeFragment.getThemeUrl(m_EditPost.getTopicId(), "view=findpost&p=" + m_EditPost.getId()), true);
                }
                getMainActivity().tryRemoveTab(getTag());
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error,
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

        LoadTask(Context context, String forumId, String topicId, String postId, String authKey) {
            this.forumId = forumId;
            this.topicId = topicId;
            this.postId = postId;
            this.authKey = authKey;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .cancelListener(dialog -> cancel(true))
                    .content(R.string.loading_message)
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
            Toast.makeText(getMainActivity(), R.string.canceled, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getMainActivity(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
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

        PostTask(Context context,
                 String postBody, String postEditReason, Boolean enableEmo, Boolean enableSign) {
            this.postBody = postBody;
            this.postEditReason = postEditReason;
            this.enableEmo = enableEmo;
            this.enableSign = enableSign;
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .content(R.string.sending_message)
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
                    Toast.makeText(getMainActivity(), App.getContext().getString(R.string.error) + ": " + mError, Toast.LENGTH_LONG).show();
                    return;
                }
                if (App.getInstance().isContainsByTag(parentTag)) {
                    ((ThemeFragment) App.getInstance().getTabByTag(parentTag).getFragment())
                            .showTheme(String.format("http://4pda.ru/forum/index.php?showtopic=%s&%s", m_EditPost.getTopicId(),
                                    isNewPost() ? "view=getlastpost" : "view=findpost&p=" + m_EditPost.getId()), true);
                }
                getMainActivity().tryRemoveTab(getTag());

            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error,
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class AttachesAdapter extends RecyclerView.Adapter<AttachesAdapter.AttachViewHolder> {
        private final List<EditAttach> content;

        AttachesAdapter(List<EditAttach> content) {
            super();
            this.content = content;
        }

        public EditAttach getItem(int i) {
            return content.get(i);
        }

        @Override
        public AttachViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewGroup group = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_spinner_item, parent, false);
            return new AttachViewHolder(group);
        }

        @Override
        public void onBindViewHolder(AttachViewHolder holder, int position) {
            EditAttach attach = content.get(position);
            holder.btnSpoiler.setTag(attach);
            holder.txtFile.setText(attach.getName());
            holder.txtFile.setTag(attach);

            holder.btnDelete.setOnClickListener(view13 -> {
                mAttachesListDialog.dismiss();
                new DeleteAttachTask(getMainActivity(),
                        attach.getId())
                        .execute();
            });

            holder.btnSpoiler.setOnClickListener(view12 -> {
                mAttachesListDialog.dismiss();

                int selectionStart = txtPost.getSelectionStart();
                if (selectionStart == -1)
                    selectionStart = 0;
                if (txtPost.getText() != null)
                    txtPost.getText().insert(selectionStart, "[spoiler][attachment=" + attach.getId() + ":" + attach.getName() + "][/spoiler]");
            });

            holder.txtFile.setOnClickListener(view1 -> {
                mAttachesListDialog.dismiss();
                int selectionStart = txtPost.getSelectionStart();
                if (selectionStart == -1)
                    selectionStart = 0;
                if (txtPost.getText() != null)
                    txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
            });
        }

        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return content.size();
        }

        class AttachViewHolder extends RecyclerView.ViewHolder {

            ImageButton btnSpoiler;
            ImageButton btnDelete;
            TextView txtFile;

            AttachViewHolder(View convertView) {
                super(convertView);
                btnDelete = convertView.findViewById(R.id.btnDelete);
                btnSpoiler = convertView.findViewById(R.id.btnSpoiler);
                txtFile = convertView.findViewById(R.id.txtFile);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            m_BottomPanel.setVisibility(View.VISIBLE);
        }
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
                uiHandler.post(() -> {
                    if (search(searchText, fromSelection) == SEARCH_RESULT_NOTFOUND)
                        searchEditText.setError(getString(R.string.no_matches_found));
                    else
                        searchEditText.setError(null);

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
