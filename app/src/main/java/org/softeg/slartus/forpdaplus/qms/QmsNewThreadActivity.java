package org.softeg.slartus.forpdaplus.qms;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 05.02.13
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class QmsNewThreadActivity extends BaseFragmentActivity {
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NICK_KEY = "user_nick";
    private EditText username, title, message;
    private String m_Id;
    private String m_Nick;
    private PopupPanelView mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.qms_new_thread);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        username = (EditText) findViewById(R.id.username);
        title = (EditText) findViewById(R.id.title);
        message = (EditText) findViewById(R.id.message);
        findViewById(R.id.btnSendPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });
        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), message);
        mPopupPanelView.activityCreated(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        assert extras != null;
        m_Id = extras.getString(USER_ID_KEY);
        m_Nick = extras.getString(USER_NICK_KEY);
        if (!TextUtils.isEmpty(m_Nick)) {
            username.setText(m_Nick);
            username.setVisibility(View.GONE);
            setTitle(m_Nick + ":QMS:Новая тема");
        } else if (!TextUtils.isEmpty(m_Id)) {
            setTitle("QMS:Новая тема");
            new GetUserTask(m_Id).execute();
        } else {
            setTitle("QMS:Новая тема");
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

    public static void showUserNewThread(Context activity, String userId, String userNick) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsNewThreadActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        intent.putExtra(USER_NICK_KEY, userNick);
        activity.startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(USER_ID_KEY,m_Id);
        outState.putString(USER_NICK_KEY,m_Nick);
        outState.putString("USER_NAME_TEXT",username.getText().toString());
        outState.putString("TITLE_TEXT",title.getText().toString());
        outState.putString("MESSAGE_TEXT",message.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(android.os.Bundle outState) {
        args = outState;
        m_Id=outState.getString(USER_ID_KEY,m_Id);
        m_Nick=outState.getString(USER_NICK_KEY,m_Nick);

        username.setText(outState.getString("USER_NAME_TEXT",m_Nick));
        title.setText(outState.getString("TITLE_TEXT",""));
        message.setText(outState.getString("MESSAGE_TEXT",""));
        super.onRestoreInstanceState(outState);
    }


    private void send() {
        m_Nick = username.getText().toString();
        String theme = title.getText().toString();
        String post = message.getText().toString();

        if (TextUtils.isEmpty(m_Nick)) {
            username.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Укажите получателя", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(post)) {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }
        new SendTask(this, m_Id, m_Nick, theme, post).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private class GetUserTask extends AsyncTask<String, Void, Boolean> {
        private String userId;
        private String userNick;

        public GetUserTask(String userId) {
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                userNick = ProfileApi.getUserNick(Client.getInstance(), userId);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            username.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Получение ника пользователя..", Toast.LENGTH_SHORT).show();
            setSupportProgressBarIndeterminateVisibility(true);
        }

        private Exception ex;

        protected void onPostExecute(final Boolean success) {
            setSupportProgressBarIndeterminateVisibility(false);
            if (success && !TextUtils.isEmpty(userNick)) {
                m_Nick = userNick;
                Toast.makeText(getContext(), "Ник получен: " + m_Nick, Toast.LENGTH_SHORT).show();
                username.setText(m_Nick);
                username.setVisibility(View.GONE);
                setTitle(m_Nick + ":QMS:Новая тема");
            } else {
                username.setVisibility(View.VISIBLE);
                if (ex != null)
                    Log.e(QmsNewThreadActivity.this, ex, new Runnable() {
                        @Override
                        public void run() {
                            new GetUserTask(userId).execute();
                        }
                    });
                else if (TextUtils.isEmpty(userNick))
                    Toast.makeText(QmsNewThreadActivity.this, "Не удалось получить ник пользователя",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(QmsNewThreadActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;
        public String m_ChatBody;
        private String userId;
        private String userNick;
        private String title;
        private String body;


        public SendTask(Context context, String userId, String userNick, String title, String body) {
            this.userId = userId;
            this.userNick = userNick;
            this.title = title;
            this.body = body;

            dialog = new AppProgressDialog(context);
        }

        private Map<String, String> outParams;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                outParams = new HashMap<>();
                m_ChatBody = QmsApi.createThread(Client.getInstance(), userId, userNick, title, body,
                        outParams, QmsChatActivity.getEncoding());

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
                finish();
                QmsChatActivity.openChat(QmsNewThreadActivity.this, outParams.get("mid"), outParams.get("user"),
                        outParams.get("t"), outParams.get("title"), m_ChatBody);
            } else {
                if (ex != null)
                    Log.e(QmsNewThreadActivity.this, ex);
                else
                    Toast.makeText(QmsNewThreadActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }


}
