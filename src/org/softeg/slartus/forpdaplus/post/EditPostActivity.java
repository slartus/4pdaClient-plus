package org.softeg.slartus.forpdaplus.post;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import org.softeg.slartus.forpdaapi.PostApi;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.HtmlUtils;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.topicview.ThemeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.11.11
 * Time: 12:42
 */
public class EditPostActivity extends BaseFragmentActivity {
    public static final int NEW_EDIT_POST_REQUEST_CODE = MyApp.getInstance().getUniqueIntValue();
    public static final String TOPIC_BODY_KEY = "EditPostActivity.TOPIC_BODY_KEY";

    public static final String POST_URL_KEY = "EditPostActivity.POST_URL_KEY";
    private EditText txtPost, txtpost_edit_reason;

    private Button btnAttachments;
    private ProgressBar progress_search;
    private String forumId;
    private ArrayList<String> m_AttachFilePaths = new ArrayList<>();
    private String lastSelectDirPath = Environment.getExternalStorageDirectory().getPath();
    private String themeId;
    private String postId;

    private String authKey;
    private String attachPostKey;
    final Handler uiHandler = new Handler();

    private final int REQUEST_SAVE = 0;
    private final int REQUEST_SAVE_IMAGE = 1;

    private View m_BottomPanel;
    private PopupPanelView mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);

    public static void editPost(Activity context, String forumId, String topicId, String postId, String authKey) {
        Intent intent = new Intent(context, EditPostActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", postId);
        intent.putExtra("authKey", authKey);
        context.startActivityForResult(intent, NEW_EDIT_POST_REQUEST_CODE);
    }

    public static void newPost(Activity context, String forumId, String topicId, String authKey,
                               final String body) {
        Intent intent = new Intent(context, EditPostActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", "-1");
        intent.putExtra("body", body);
        intent.putExtra("authKey", authKey);
        context.startActivityForResult(intent, NEW_EDIT_POST_REQUEST_CODE);
    }

    public static void newPostWithAttach(Context context, String forumId, String topicId, String authKey,
                                         final Bundle extras) {
        Intent intent = new Intent(context, EditPostActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", "-1");
        intent.putExtra("postId", "-1");
        intent.putExtras(extras);
        intent.putExtra("authKey", authKey);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());

        setContentView(R.layout.edit_post_plus);

        progress_search = (ProgressBar) findViewById(R.id.progress_search);
        lastSelectDirPath = prefs.getString("EditPost.AttachDirPath", lastSelectDirPath);

        m_BottomPanel = findViewById(R.id.bottomPanel);

        txtPost = (EditText) findViewById(R.id.txtPost);
        txtpost_edit_reason = (EditText) findViewById(R.id.txtpost_edit_reason);


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

        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), txtPost);
        mPopupPanelView.activityCreated(this);

        try {
            Intent intent = getIntent();
            setDataFromExtras(intent.getExtras());
        } catch (Throwable ex) {
            Log.e(this, ex);
        }

        createActionMenu();


        startLoadPost();
    }


    private boolean sendMail() {
        final String body = getPostText();
        if (TextUtils.isEmpty(body))
            return true;

        if (Preferences.Topic.getConfirmSend()) {
            new AlertDialogBuilder(getContext())
                    .setTitle("Уверены?")
                    .setMessage("Подтвердите отправку")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            sendPost(body, getEditReasonText());
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .create().show();
        } else {
            sendPost(body, getEditReasonText());
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("forumId", forumId);
        if (m_AttachFilePaths != null)
            outState.putStringArray("AttachFilePaths", m_AttachFilePaths.toArray(new String[m_AttachFilePaths.size()]));
        outState.putString("lastSelectDirPath", lastSelectDirPath);
        outState.putString("themeId", themeId);
        outState.putString("postId", postId);
        outState.putString("authKey", authKey);
        outState.putString("attachPostKey", attachPostKey);
        outState.putString("postText", getPostText());
        outState.putString("txtpost_edit_reason", getEditReasonText());
        outState.putParcelableArrayList("attaches", attaches);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;
        forumId = savedInstanceState.getString("forumId");
        if (savedInstanceState.containsKey("AttachFilePaths") && savedInstanceState.getStringArray("AttachFilePaths") != null)
            m_AttachFilePaths = new ArrayList<>(Arrays.asList(savedInstanceState.getStringArray("AttachFilePaths")));
        lastSelectDirPath = savedInstanceState.getString("lastSelectDirPath");
        themeId = savedInstanceState.getString("themeId");
        postId = savedInstanceState.getString("postId");
        authKey = savedInstanceState.getString("authKey");
        attachPostKey = savedInstanceState.getString("attachPostKey");
        txtPost.setText(savedInstanceState.getString("postText"));
        txtpost_edit_reason.setText(savedInstanceState.getString("txtpost_edit_reason"));
        mPopupPanelView.setTopic(forumId, themeId, authKey);
        attaches = savedInstanceState.getParcelableArrayList("attaches");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setDataFromExtras(Bundle extras) {
        forumId = extras.getString("forumId");
        themeId = extras.getString("themeId");
        postId = extras.getString("postId");
        authKey = extras.getString("authKey");

        mPopupPanelView.setTopic(forumId, themeId, authKey);
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            Object attachesObject = extras.get(Intent.EXTRA_STREAM);
            if (attachesObject instanceof Uri) {
                Uri uri = (Uri) extras.get(Intent.EXTRA_STREAM);
                m_AttachFilePaths = new ArrayList<>(Arrays.asList(new String[]{getRealPathFromURI(uri)}));
            } else if (attachesObject instanceof ArrayList<?>) {
                m_AttachFilePaths = new ArrayList<>();
                ArrayList<?> list = (ArrayList<?>) attachesObject;
                for (Object item : list) {
                    Uri uri = (Uri) item;
                    m_AttachFilePaths.add(getRealPathFromURI(uri));
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

    }


    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MenuFragment mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private Boolean isNewPost() {
        return postId.equals("-1");
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
        if (attaches.size() == 0) {
            startAddAttachment();
            return;
        }
        AttachesAdapter adapter = new AttachesAdapter(attaches, this);
        mAttachesListDialog = new AlertDialogBuilder(this)
                .setCancelable(true)
                .setTitle("Вложения")
                .setSingleChoiceItems(adapter, -1, null)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startAddAttachment();
                    }
                })
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        mAttachesListDialog.show();
    }

    private void startAddAttachment() {
        CharSequence[] items = new CharSequence[]{"Файл", "Изображение"};
        new AlertDialogBuilder(getContext())
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        switch (i) {
                            case 0://файл

                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("file/*");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                                    startActivityForResult(intent, REQUEST_SAVE);

                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(EditPostActivity.this, "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(EditPostActivity.this, ex);
                                }

                                break;
                            case 1:// Изображение

                                try {
                                    Intent imageintent = new Intent(
                                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                        imageintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    startActivityForResult(imageintent, REQUEST_SAVE_IMAGE);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(EditPostActivity.this, "Ни одно приложение не установлено для выбора изображения!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(EditPostActivity.this, ex);
                                }
                                break;
                        }
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {

                if (requestCode == REQUEST_SAVE || requestCode == REQUEST_SAVE_IMAGE) {
                    String attachFilePath = getRealPathFromURI(data.getData());
                    saveAttachDirPath(attachFilePath);
                    new UpdateTask(EditPostActivity.this, getPostText(), getEditReasonText(), attachFilePath)
                            .execute();
                }
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }

    }

    public String getRealPathFromURI(Uri contentUri) {
        if (!contentUri.toString().startsWith("content://"))
            return contentUri.getPath();

        // can post image
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,
                filePathColumn, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private void saveAttachDirPath(String attachFilePath) {
        lastSelectDirPath = FileUtils.getDirPath(attachFilePath);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EditPost.AttachDirPath", lastSelectDirPath);
        editor.commit();
    }

    private void startLoadPost() {
        new LoadTask(this).execute();
    }

    private void sendPost(final String text, String editPostReason) {

        if (isNewPost()) {
            new PostTask(EditPostActivity.this).execute(text, editPostReason);
        } else {
            new AcceptEditTask(EditPostActivity.this).execute(text, editPostReason);
        }
    }

    private void parsePody(String body) {
        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = body.indexOf(startFlag);
        startIndex += startFlag.length();
        int endIndex = body.indexOf("</textarea>", startIndex);

        if (TextUtils.isEmpty(getPostText()))
            txtPost.setText(HtmlUtils.modifyHtmlQuote(body.substring(startIndex, endIndex)));

        EditPostActivity.this.attachPostKey = null;
        Matcher m = Pattern.compile("name='attach_post_key' value='([^']*)'", Pattern.CASE_INSENSITIVE).matcher(body);
        if (m.find()) {
            EditPostActivity.this.attachPostKey = m.group(1);
        }

        txtpost_edit_reason.setText(null);
        m = Pattern.compile("name=(?:'|\")post_edit_reason(?:'|\") value=(?:'|\")([^\"']*)(?:'|\")", Pattern.CASE_INSENSITIVE).matcher(body);
        if (m.find()) {
            txtpost_edit_reason.setText(m.group(1));
            if (!TextUtils.isEmpty(m.group(1)))
                txtpost_edit_reason.setVisibility(View.VISIBLE);
        }
        parseAttachesUI(body);
    }

    private ArrayList<Attach> attaches = new ArrayList<>();

    private void parseAttaches(String body) {

        if (body == null)
            return;
        attaches = new Attaches();
        Pattern pattern = Pattern.compile("onclick=\"insText\\('\\[attachment=(\\d+):([^]]*)", Pattern.CASE_INSENSITIVE);
        Pattern attachBodyPattern = Pattern.compile("<!-- ATTACH -->([\\s\\S]*?)</i>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = attachBodyPattern.matcher(body);

        if (m.find()) {
            Matcher m1 = pattern.matcher(m.group(1));
            while (m1.find()) {
                attaches.add(new Attach(m1.group(1), m1.group(2)));
            }
        } else {
            Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                    "\n" +
                    "\t\t<p>(.*?)</p>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            m = checkPattern.matcher(body);
            if (m.find()) {
                Toast.makeText(this, m.group(1), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void parseAttachesUI(String body) {
        parseAttaches(body);
        btnAttachments.setText(attaches.size() + "");

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

    private String getAttachIdsString() {
        String res = "0";
        for (Attach attach : attaches) {
            res += "," + attach.getId();
        }
        return res;
    }

    private class UpdateTask extends AsyncTask<String, Pair<String, Integer>, Boolean> {
        private final ProgressDialog dialog;
        private ProgressState m_ProgressState;
        private String postBody;
        private String editReason;
        private List<String> attachFilePaths;

        public UpdateTask(Context context, String postBody, String editReason, List<String> attachFilePaths) {
            this.postBody = postBody;
            this.editReason = editReason;
            this.attachFilePaths = attachFilePaths;
            dialog = new AppProgressDialog(context);

        }

        public UpdateTask(Context context, String postBody, String editReason, String newAttachFilePath) {
            this(context, postBody, editReason, new ArrayList<>(Arrays.asList(new String[]{newAttachFilePath})));

        }

        String body = null;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_ProgressState = new ProgressState() {
                    @Override
                    public void update(String message, int percents) {
                        publishProgress(new Pair<>("", percents));
                    }
                };

                if (TextUtils.isEmpty(postBody))
                    postBody = TEMP_EMPTY_TEXT;

                int i = 1;
                for (String newAttachFilePath : attachFilePaths) {
                    publishProgress(new Pair<>(String.format("Отправка файла %d из %d", i++, attachFilePaths.size()), 0));
                    body = Client.getInstance().attachFilePost(forumId, themeId, authKey, attachPostKey,
                            postId, Preferences.Topic.Post.getEnableSign(), Preferences.Topic.Post.getEnableEmotics(), postBody, newAttachFilePath, getAttachIdsString(),
                            m_ProgressState, editReason);
                    parseAttaches(body);
                }

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        protected void onProgressUpdate(Pair<String, Integer>... values) {
            super.onProgressUpdate(values);
            if (!TextUtils.isEmpty(values[0].first))
                dialog.setMessage(values[0].first);
            dialog.setProgress(values[0].second);
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка файла..");
            this.dialog.setCancelable(true);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setMax(100);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    m_ProgressState.cancel();
                    cancel(false);
                }
            });
            this.dialog.setProgress(0);
            this.dialog.setIndeterminate(false);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success || (isCancelled() && body != null)) {
                //if (attachFilePaths.size() > 0)
                parseAttachesUI(body);
            } else {
                if (ex != null)
                    Log.e(EditPostActivity.this, ex);
                else
                    Toast.makeText(EditPostActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Boolean success) {
            super.onCancelled(success);
            if (success || (isCancelled() && body != null)) {
                parseAttachesUI(body);
            } else {
                if (ex != null)
                    Log.e(EditPostActivity.this, ex);
                else
                    Toast.makeText(EditPostActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class DeleteAttachTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;
        private String postBody;
        private String reasonText;
        private String attachId;

        public DeleteAttachTask(Context context, String postBody,
                                String reasonText, String attachId) {
            this.postBody = postBody;
            this.reasonText = reasonText;
            this.attachId = attachId;

            dialog = new AppProgressDialog(context);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String post_edit_reason = reasonText;
                body = Client.getInstance().deleteAttachFilePost(forumId, themeId, authKey, postId,
                        Preferences.Topic.Post.getEnableSign(), Preferences.Topic.Post.getEnableEmotics(),
                        postBody,
                        attachId, getAttachIdsString(), post_edit_reason);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление файла...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parseAttachesUI(body);
            } else {
                if (ex != null)
                    Log.e(EditPostActivity.this, ex);
                else
                    Toast.makeText(EditPostActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AcceptEditTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        public AcceptEditTask(Context context) {
            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String post_edit_reason = params.length > 1 ? params[1] : "";
                Client.getInstance().editPost(forumId, themeId, authKey, postId,
                        Preferences.Topic.Post.getEnableSign(), Preferences.Topic.Post.getEnableEmotics(),
                        params[0], getAttachIdsString(), post_edit_reason);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Редактирование сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (getIntent() != null && getIntent().getExtras() != null &&
                        !ThemeActivity.class.toString().equals(getIntent().getExtras().get(BaseFragmentActivity.SENDER_ACTIVITY)))

                    ExtTopic.showActivity(EditPostActivity.this, themeId, "view=findpost&p=" + postId);

                else {
                    Intent intent = new Intent();
                    String url = String.format("http://4pda.ru/forum/index.php?showtopic=%s&view=findpost&p=%s", themeId, postId);
                    intent.putExtra(POST_URL_KEY, url);
                    EditPostActivity.this.setResult(Activity.RESULT_OK, intent);
                }
                finish();

            } else {
                if (ex != null)
                    Log.e(EditPostActivity.this, ex);
                else
                    Toast.makeText(EditPostActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }
    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        public LoadTask(Context context) {
            dialog = new AppProgressDialog(context);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        String body = null;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Map<String, String> outParams = new HashMap<>();
                body = Client.getInstance().getEditPostPlus(forumId, themeId, postId, authKey, outParams);
                if (outParams.size() > 0) {
                    forumId = outParams.get("forumId");
                    authKey = outParams.get("authKey");
                }
                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        protected void onCancelled() {
            Toast.makeText(EditPostActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
            finish();
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parsePody(body);

                if (m_AttachFilePaths.size() > 0)
                    new UpdateTask(EditPostActivity.this, getPostText(), getEditReasonText(), m_AttachFilePaths)
                            .execute();
                m_AttachFilePaths = new ArrayList<>();
            } else {
                if (ex != null)
                    Log.e(EditPostActivity.this, ex);
                else
                    Toast.makeText(EditPostActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;
        private String mPostResult = null;
        private String mError = null;

        public PostTask(Context context) {
            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = Client.getInstance().reply(forumId, themeId, authKey, attachPostKey,
                        params[0], Preferences.Topic.Post.getEnableSign(), Preferences.Topic.Post.getEnableEmotics(),
                        false, getAttachIdsString());

                mError = PostApi.checkPostErrors(mPostResult);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (!TextUtils.isEmpty(mError)) {
                    Toast.makeText(EditPostActivity.this, "Ошибка: " + mError, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(TOPIC_BODY_KEY, mPostResult);
                String url = String.format("http://4pda.ru/forum/index.php?showtopic=%s&%s", themeId, isNewPost() ? "view=getlastpost" : "view=findpost&p=" + postId);
                intent.putExtra(POST_URL_KEY, url);
                EditPostActivity.this.setResult(Activity.RESULT_OK, intent);

                finish();
            } else {
                if (ex != null)
                    Log.e(EditPostActivity.this, ex);
                else
                    Toast.makeText(EditPostActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class AttachesAdapter extends BaseAdapter {
        private Activity activity;
        private final ArrayList<Attach> content;

        public AttachesAdapter(ArrayList<Attach> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public Attach getItem(int i) {
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

                        Attach attach = (Attach) view.getTag();

                        new DeleteAttachTask(EditPostActivity.this,
                                getPostText(), getEditReasonText(),
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
                        Attach attach = (Attach) view.getTag();
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
                        Attach attach = (Attach) view.getTag();
                        if (txtPost.getText() != null)
                            txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Attach attach = this.getItem(position);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            m_BottomPanel.setVisibility(View.VISIBLE);
        }
        return true;
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
            //   txtPost.setSelection(findedStartSelection, findedStartSelection + searchText.length());


            raw.setSpan(new BackgroundColorSpan(0xFF8B008B), findedStartSelection, findedStartSelection
                    + searchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


            txtPost.setText(raw);
            txtPost.setSelection(findedStartSelection);
            txtPost.setCursorVisible(true);
            return SEARCH_RESULT_FOUND;
        } catch (Throwable ex) {
            Log.e(this, ex);
        } finally {
            if (!fromSelection)
                searchEditText.requestFocus();
            progress_search.setVisibility(View.GONE);
        }
        return SEARCH_RESULT_EMPTYTEXT;
    }

    public EditText searchEditText;

    public static final class MenuFragment extends Fragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle saveInstance) {
            super.onCreate(saveInstance);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem item;

            if (!getInterface().isNewPost()) {
                item = menu.add("Причина редактирования").setIcon(R.drawable.ic_menu_edit);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        getInterface().toggleEditReasonDialog();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            item = menu.add("Поиск по тексту").setIcon(R.drawable.ic_menu_search);
            item.setActionView(R.layout.action_collapsible_search);
            getInterface().searchEditText = (EditText) item.getActionView().findViewById(R.id.editText);
            getInterface().searchEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                            && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        String text = getInterface().searchEditText.getText() == null ? "" : getInterface().searchEditText.getText().toString().trim();
                        getInterface().startSearch(text, true);
                        getInterface().searchEditText.requestFocus();
                        return true;
                    }

                    return false;
                }
            });
            getInterface().searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable mEdit) {
                    String text = mEdit.toString().trim();
                    getInterface().startSearch(text, false);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
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
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            item = menu.add("Скрыть панели").setIcon(R.drawable.ic_media_fullscreen);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().hidePanels();
                    return true;
                }
            });

            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        public EditPostActivity getInterface() {
            return (EditPostActivity) getActivity();
        }
    }

}

