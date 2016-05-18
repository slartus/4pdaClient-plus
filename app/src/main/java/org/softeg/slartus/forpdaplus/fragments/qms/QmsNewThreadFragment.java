package org.softeg.slartus.forpdaplus.fragments.qms;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdaapi.qms.QmsApi;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.quickpost.PopupPanelView;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by radiationx on 15.11.15.
 */
public class QmsNewThreadFragment extends GeneralFragment {
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NICK_KEY = "user_nick";
    private EditText username, title, message;
    private String m_Id;
    private String m_Nick;
    private boolean emptyText = true;
    private PopupPanelView mPopupPanelView;

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        mPopupPanelView.hidePopupWindow();
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    public static QmsNewThreadFragment newInstance(Bundle args){
        QmsNewThreadFragment fragment = new QmsNewThreadFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public static void showUserNewThread(Context activity, String userId, String userNick) {
        Bundle args = new Bundle();
        args.putString(USER_ID_KEY, userId);
        args.putString(USER_NICK_KEY, userNick);
        MainActivity.addTab(userNick, newInstance(args));
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPopupPanelView!=null)
            mPopupPanelView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
        if(mPopupPanelView!=null)
            mPopupPanelView.resume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setArrow();
        view = inflater.inflate(R.layout.qms_new_thread, container, false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        username = (EditText) findViewById(R.id.username);
        title = (EditText) findViewById(R.id.title);
        message = (EditText) findViewById(R.id.message);
        final Button send_button = (Button) view.findViewById(R.id.btnSendPost);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });
        message.addTextChangedListener(new TextWatcher() {
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
        if(mPopupPanelView==null)
            mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);
        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), message);
        mPopupPanelView.activityCreated(getMainActivity(), view);

        Bundle extras = getArguments();

        assert extras != null;
        m_Id = extras.getString(USER_ID_KEY);
        m_Nick = extras.getString(USER_NICK_KEY);
        if (!TextUtils.isEmpty(m_Nick)) {
            username.setText(m_Nick);
            username.setVisibility(View.GONE);
            setTitle(m_Nick + ":"+getString(R.string.qms_title_new_thread));
            App.getInstance().getTabByTag(getTag()).setTitle(m_Nick + ":"+getString(R.string.qms_title_new_thread));

        } else if (!TextUtils.isEmpty(m_Id)) {
            setTitle(getString(R.string.qms_title_new_thread));
            App.getInstance().getTabByTag(getTag()).setTitle(getString(R.string.qms_title_new_thread));
            new GetUserTask(m_Id).execute();
        } else {
            setTitle(getString(R.string.qms_title_new_thread));
            App.getInstance().getTabByTag(getTag()).setTitle(getString(R.string.qms_title_new_thread));
        }
        getMainActivity().notifyTabAdapter();
        return view;
    }

    @Override
    public void onDestroy() {
        if (mPopupPanelView != null) {
            mPopupPanelView.destroy();
            mPopupPanelView = null;
        }
        super.onDestroy();
    }



    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(USER_ID_KEY,m_Id);
        outState.putString(USER_NICK_KEY,m_Nick);
        outState.putString("USER_NAME_TEXT",username.getText().toString());
        outState.putString("TITLE_TEXT",title.getText().toString());
        outState.putString("MESSAGE_TEXT",message.getText().toString());
        super.onSaveInstanceState(outState);
    }

    /*@Override
    protected void onRestoreInstanceState(android.os.Bundle outState) {
        args = outState;
        m_Id=outState.getString(USER_ID_KEY,m_Id);
        m_Nick=outState.getString(USER_NICK_KEY,m_Nick);

        username.setText(outState.getString("USER_NAME_TEXT",m_Nick));
        title.setText(outState.getString("TITLE_TEXT",""));
        message.setText(outState.getString("MESSAGE_TEXT",""));
        super.onRestoreInstanceState(outState);
    }*/


    private void send() {
        m_Nick = username.getText().toString();
        String theme = title.getText().toString();
        String post = message.getText().toString();

        if (TextUtils.isEmpty(m_Nick)) {
            username.setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(getContext(), R.string.enter_nick, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, App.getInstance().getResources().getDisplayMetrics()));
            toast.show();
            return;
        }

        if (TextUtils.isEmpty(post)) {
            Toast toast = Toast.makeText(getContext(), R.string.enter_message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, App.getInstance().getResources().getDisplayMetrics()));
            toast.show();
            return;
        }
        new SendTask(getMainActivity(), m_Id, m_Nick, theme, post).execute();
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
            Toast.makeText(getContext(), R.string.getting_user_nick, Toast.LENGTH_SHORT).show();
            //setSupportProgressBarIndeterminateVisibility(true);
        }

        private Exception ex;

        protected void onPostExecute(final Boolean success) {
            //setSupportProgressBarIndeterminateVisibility(false);
            if (success && !TextUtils.isEmpty(userNick)) {
                m_Nick = userNick;
                Toast.makeText(getContext(), App.getContext().getString(R.string.nick_received)+": " + m_Nick, Toast.LENGTH_SHORT).show();
                username.setText(m_Nick);
                username.setVisibility(View.GONE);
                setTitle(m_Nick + ":"+App.getContext().getString(R.string.qms_title_new_thread));
                App.getInstance().getTabByTag(getTag()).setTitle(m_Nick + ":"+App.getContext().getString(R.string.qms_title_new_thread));
                getMainActivity().notifyTabAdapter();
            } else {
                username.setVisibility(View.VISIBLE);
                if (ex != null)
                    AppLog.e(getMainActivity(), ex, new Runnable() {
                        @Override
                        public void run() {
                            new GetUserTask(userId).execute();
                        }
                    });
                else if (TextUtils.isEmpty(userNick))
                    Toast.makeText(getMainActivity(), R.string.error_getting_nick,
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> {
        private final MaterialDialog dialog;
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

            dialog = new MaterialDialog.Builder(context)
                    .progress(true,0)
                    .content(App.getContext().getString(R.string.sending_message))
                    .build();
        }

        private Map<String, String> outParams;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                outParams = new HashMap<>();
                m_ChatBody = QmsApi.createThread(Client.getInstance(), userId, userNick, title, body,
                        outParams, QmsChatFragment.getEncoding());

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
                getMainActivity().removeTab(getTag());
                QmsChatFragment.openChat(outParams.get("mid"), outParams.get("user"),
                        outParams.get("t"), outParams.get("title"), m_ChatBody);
            } else {
                if (ex != null)
                    AppLog.e(getMainActivity(), ex);
                else
                    Toast.makeText(getMainActivity(), R.string.unknown_error,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }


}
