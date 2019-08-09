package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdaapi.classes.LoginForm;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository;

import java.lang.ref.WeakReference;

/**
 * User: slinkin
 * Date: 08.02.12
 * Time: 7:18
 */
public class LoginDialog {
    private String capTime;
    private String capSig;
    private String session;
    private EditText username_edit;
    private final EditText password_edit;
    private CheckBox privacy_checkbox;
    private View mView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Context mContext;

    LoginDialog(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        mView = inflater.inflate(R.layout.login, null);

        mImageView = mView.findViewById(R.id.cap_img);
        mProgressBar = mView.findViewById(R.id.progressBar2);
        username_edit = mView.findViewById(R.id.username_edit);
        password_edit = mView.findViewById(R.id.password_edit);
        privacy_checkbox = mView.findViewById(R.id.privacy_checkbox);
        new CapTask().execute();
        loadData();
    }

    protected View getView() {
        return mView;
    }

    void connect(Client.OnUserChangedListener onConnectResult) {
        saveData();
        LoginTask loginTask = new LoginTask(mContext,
                username_edit.getText().toString(), password_edit.getText().toString(),
                privacy_checkbox.isChecked(),
                ((EditText) mView.findViewById(R.id.cap_value_ed)).getText().toString(),
                capTime, capSig, session,
                onConnectResult);
        loginTask.execute(username_edit.getText().toString(), password_edit.getText().toString(),
                Boolean.toString(privacy_checkbox.isChecked()));
    }

    private void saveData() {
        App.getInstance().getPreferences().edit()
                .putBoolean("LoginPrivacy", privacy_checkbox.isChecked())
                .putString("Login", username_edit.getText().toString())
                .apply();
    }

    protected void loadData() {
        privacy_checkbox.setChecked(App.getInstance().getPreferences().getBoolean("LoginPrivacy", false));
        username_edit.setText(App.getInstance().getPreferences().getString("Login", ""));
    }


    public static void showDialog(final Context context, final Client.OnUserChangedListener onConnectResult) {
        final LoginDialog loginDialog = new LoginDialog(context);
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.login)
                .customView(loginDialog.getView(), true)
                .positiveText(R.string.login)
                .negativeText(R.string.cancel)
                .onPositive((dialog1, which) ->   loginDialog.connect(onConnectResult))
                .build();
        Window window=dialog.getWindow();
        assert window != null;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
    }

    static void logout(Context context) {
        LogoutTask logoutTask = new LogoutTask(context);
        logoutTask.execute();
        App.getInstance().getPreferences().edit()
                .putBoolean("isRecdRepImage", false)
                .putString("repPlusImage", "http://s.4pda.to/ShmfPSURw3VD2aNlTerb3hvYwGCMxd4z0muJ.gif")
                .apply();
    }

    public class CapTask extends AsyncTask<String, Void, LoginForm> {

        CapTask() {

        }

        @Override
        protected LoginForm doInBackground(String... params) {
            try {
                return ProfileApi.getLoginForm();
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
            Toast.makeText(mContext, R.string.canceled,
                    Toast.LENGTH_SHORT).show();
        }


        // can use UI thread here
        protected void onPostExecute(final LoginForm loginForm) {

            if (loginForm.getError() == null) {
                ImageLoader.getInstance().displayImage(loginForm.getCapPath(), mImageView, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Toast.makeText(mContext, R.string.failed_load_captcha, Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
                capTime = loginForm.getCapTime();
                capSig = loginForm.getCapSig();
                session = loginForm.getSession();
            } else {

                AppLog.e(mContext, loginForm.getError());

            }
        }

    }

    public static class LoginTask extends AsyncTask<String, Void, Boolean> {

        WeakReference<Context> mContext;
        private final MaterialDialog dialog;
        private String login;
        private String password;
        private Boolean privacy;
        private String capVal;
        private String capTime;
        private String capSig;
        private String session;
        private Client.OnUserChangedListener m_OnConnectResult;

        LoginTask(Context context,
                  String login, String password, Boolean privacy,
                  String capVal, String capTime, String capSig, String session,
                  Client.OnUserChangedListener onConnectResult) {
            mContext = new WeakReference<>(context);
            this.login = login;
            this.password = password;
            this.privacy = privacy;
            this.capVal = capVal;
            this.capTime = capTime;
            this.capSig = capSig;
            this.session = session;
            m_OnConnectResult = onConnectResult;
            dialog = new MaterialDialog.Builder(mContext.get())
                    .progress(true, 0)
                    .cancelable(false)
                    .content(R.string.performing_login)
                    .build();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                return Client.getInstance().login(login, password, privacy, capVal, capTime, capSig);
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
            Toast.makeText(mContext.get(), R.string.canceled,
                    Toast.LENGTH_SHORT).show();
        }

        private Exception ex;


        private void doOnUserChangedListener(String user, Boolean success) {
            if (m_OnConnectResult != null)
                m_OnConnectResult.onUserChanged(user, success);
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }


            doOnUserChangedListener(login, success);
            Client.getInstance().doOnUserChangedListener();
            App.getInstance().getPreferences().edit().putBoolean("needLoadRepImage", success).apply();
            if (success) {
                Toast.makeText(mContext.get(), R.string.login_performed,
                        Toast.LENGTH_SHORT).show();
            } else {
                if (ex != null)
                    AppLog.e(mContext.get(), ex);
                else
                    new MaterialDialog.Builder(mContext.get())
                            .title(R.string.error)
                            .content(Client.getInstance().getLoginFailedReason())
                            .positiveText(R.string.ok)
                            .show();
            }
        }

    }

    public static class LogoutTask extends AsyncTask<String, Void, Boolean> {

        WeakReference<Context> mContext;
        private final MaterialDialog dialog;

        LogoutTask(Context context) {
            mContext = new WeakReference<>(context);
            dialog = new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .cancelable(true)
                    .content(R.string.performing_logout)
                    .build();
        }



        @Override
        protected Boolean doInBackground(String... params) {
            try {


                return Client.getInstance().logout();
            } catch (Throwable e) {
                AppLog.e(mContext.get(), e);
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.show();
        }

        protected void onCancelled() {
            Toast.makeText(mContext.get(), R.string.canceled,
                    Toast.LENGTH_SHORT).show();
        }

        private Throwable ex;


        private void doOnUserChangedListener() {

        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            doOnUserChangedListener();
            App.getInstance().getPreferences().edit().putBoolean("needLoadRepImage", !success).apply();
            if (success) {
                Toast.makeText(mContext.get(), R.string.logout_performed,
                        Toast.LENGTH_SHORT).show();
            } else {
                if (ex != null)
                    AppLog.i(mContext.get(), ex);

            }
        }
    }
}