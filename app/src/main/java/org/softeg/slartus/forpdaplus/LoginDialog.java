package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdaapi.classes.LoginForm;
import org.softeg.slartus.forpdaplus.common.AppLog;

/**
 * User: slinkin
 * Date: 08.02.12
 * Time: 7:18
 */
public class LoginDialog {
    String capD;
    String capS;
    String session;
    EditText username_edit;
    final EditText password_edit;
    CheckBox privacy_checkbox;
    View mView;

    private Context mContext;

    public LoginDialog(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.login, null);

        username_edit = (EditText) mView.findViewById(R.id.username_edit);
        password_edit = (EditText) mView.findViewById(R.id.password_edit);
        privacy_checkbox = (CheckBox) mView.findViewById(R.id.privacy_checkbox);
        new CapTask().execute();
        loadData();
    }

    protected View getView() {
        return mView;
    }

    protected void connect(Client.OnUserChangedListener onConnectResult) {
        saveData();
        LoginTask loginTask = new LoginTask(mContext,
                username_edit.getText().toString(), password_edit.getText().toString(),
                privacy_checkbox.isChecked(),
                ((EditText) mView.findViewById(R.id.cap_value_ed)).getText().toString(),
                capD, capS,session,
                onConnectResult);
        loginTask.execute(username_edit.getText().toString(), password_edit.getText().toString(),
                Boolean.toString(privacy_checkbox.isChecked()));
    }

    protected void saveData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("LoginPrivacy", privacy_checkbox.isChecked());
        editor.putString("Login", username_edit.getText().toString());
        editor.apply();
    }

    protected void loadData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        privacy_checkbox.setChecked(preferences.getBoolean("LoginPrivacy", false));
        username_edit.setText(preferences.getString("Login", ""));

    }


    public static void showDialog(final Context context, final Client.OnUserChangedListener onConnectResult) {
        final LoginDialog loginDialog = new LoginDialog(context);
        new MaterialDialog.Builder(context)
                .title("Вход")
                .customView(loginDialog.getView(),true)
                .positiveText("Вход")
                .negativeText("Отмена")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        loginDialog.connect(onConnectResult);
                    }
                })
                .show();
    }

    public static void logout(Context context) {
        LogoutTask logoutTask = new LogoutTask(context);
        logoutTask.execute();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean("isRecdRepImage", false)
                .putString("repPlusImage", "http://s.4pda.to/ShmfPSURw3VD2aNlTerb3hvYwGCMxd4z0muJ.gif")
                .apply();
    }

    public class CapTask extends AsyncTask<String, Void, LoginForm> {

        public CapTask() {

        }

        @Override
        protected LoginForm doInBackground(String... params) {
            try {


                return ProfileApi.getLoginForm(Client.getInstance());
            } catch (Exception e) {
                LoginForm loginForm = new LoginForm();
                loginForm.setError(e);
                return loginForm;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            mView.findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
        }

        protected void onCancelled() {
            Toast.makeText(mContext, "Отменено",
                    Toast.LENGTH_SHORT).show();
        }


        // can use UI thread here
        protected void onPostExecute(final LoginForm loginForm) {
            mView.findViewById(R.id.progressBar2).setVisibility(View.GONE);

            if (loginForm.getError() == null) {
                Picasso.with(mContext).load(loginForm.getCapPath()).into((ImageView) mView.findViewById(R.id.cap_img));

                capD = loginForm.getCapD();
                capS = loginForm.getCapS();
                session=loginForm.getSession();
            } else {

                AppLog.e(mContext, loginForm.getError());

            }
        }

    }

    public static class LoginTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final MaterialDialog dialog;
        private String login;
        private String password;
        private Boolean privacy;
        private String capA;
        private String capD;
        private String capS;
        private String session;
        private Client.OnUserChangedListener m_OnConnectResult;

        public LoginTask(Context context,
                         String login, String password, Boolean privacy,
                         String capA, String capD, String capS,String session,
                         Client.OnUserChangedListener onConnectResult) {
            mContext = context;
            this.login = login;
            this.password = password;
            this.privacy = privacy;
            this.capA = capA;
            this.capD = capD;
            this.capS = capS;
            this.session = session;
            m_OnConnectResult = onConnectResult;
            dialog = new MaterialDialog.Builder(mContext)
                    .progress(true,0)
                    .cancelable(false)
                    .content("Вход...")
                    .build();
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {

                return Client.getInstance().login(login, password, privacy, capA, capD, capS,session);
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        protected void onCancelled() {
            Toast.makeText(mContext, "Отменено",
                    Toast.LENGTH_SHORT).show();
        }

        private Exception ex;


        private void doOnUserChangedListener(String user, Boolean success) {
            if (m_OnConnectResult != null)
                m_OnConnectResult.onUserChanged(user,success);
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            doOnUserChangedListener(login, success);
            Client.getInstance().doOnUserChangedListener(login, success);
            if (success) {
                Toast.makeText(mContext, "Вход выполнен",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (ex != null)
                    AppLog.e(mContext, ex);
                else
                    new MaterialDialog.Builder(mContext)
                        .title("Ошибка")
                        .content(Client.getInstance().getLoginFailedReason())
                        .positiveText("Ок")
                        .show();
            }
        }

    }

    public static class LogoutTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final MaterialDialog dialog;

        public LogoutTask(Context context) {
            mContext = context;
            dialog = new MaterialDialog.Builder(mContext)
                    .progress(true,0)
                    .cancelable(true)
                    .content("Выход...")
                    .build();
        }

        private String m_Login;
        private String m_Password;

        private Boolean m_LoginRemember;
        private Boolean m_AutoLogin;


        @Override
        protected Boolean doInBackground(String... params) {
            try {


                return Client.getInstance().logout();
            } catch (Throwable e) {
                AppLog.e(mContext, e);
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        protected void onCancelled() {
            Toast.makeText(mContext, "Отменено",
                    Toast.LENGTH_SHORT).show();
        }

        private Throwable ex;


        private void doOnUserChangedListener(String user, Boolean success) {

        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            doOnUserChangedListener(m_Login, success);

            if (success) {
                Toast.makeText(mContext, "Выход выполнен",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (ex != null)
                    AppLog.i(mContext, ex);

            }
        }


    }

}