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
import org.softeg.slartus.forpdaapi.classes.LoginFormData;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.lang.ref.WeakReference;

/**
 * User: slinkin
 * Date: 08.02.12
 * Time: 7:18
 */
public class LoginDialog {
    private String capTime;
    private String capSig;
    private final EditText username_edit;
    private final EditText password_edit;
    private final CheckBox privacy_checkbox;
    private final View mView;
    private final ImageView mImageView;
    private final ProgressBar mProgressBar;

    private final Context mContext;

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
        new CapTask(App.getContext()).execute();
        loadData();
    }

    protected View getView() {
        return mView;
    }

    void connect() {
        saveData();
        LoginTask loginTask = new LoginTask(mContext,
                username_edit.getText().toString(), password_edit.getText().toString(),
                privacy_checkbox.isChecked(),
                ((EditText) mView.findViewById(R.id.cap_value_ed)).getText().toString(),
                capTime, capSig);
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


    public static void showDialog(final Context context) {
        final LoginDialog loginDialog = new LoginDialog(context);
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.login)
                .customView(loginDialog.getView(), true)
                .positiveText(R.string.login)
                .negativeText(R.string.cancel)
                .onPositive((dialog1, which) -> loginDialog.connect())
                .build();
        Window window = dialog.getWindow();
        assert window != null;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
    }

    public static void logout(Context context) {
        LogoutTask logoutTask = new LogoutTask(context);
        logoutTask.execute();
        App.getInstance().getPreferences().edit()
                .putBoolean("isRecdRepImage", false)
                .putString("repPlusImage", "https://s.4pda.to/ShmfPSURw3VD2aNlTerb3hvYwGCMxd4z0muJ.gif")
                .apply();
    }

    public class CapTask extends AsyncTask<String, Void, LoginFormData> {

        private final WeakReference<Context> context;

        CapTask(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected LoginFormData doInBackground(String... params) {
            try {
                return ProfileApi.getLoginForm(this.context.get());
            } catch (Exception e) {
                LoginFormData loginFormData = new LoginFormData();
                loginFormData.setError(e);
                return loginFormData;
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
        protected void onPostExecute(final LoginFormData loginFormData) {

            if (loginFormData.getError() == null) {
                ImageLoader.getInstance().displayImage(loginFormData.getCapPath(), mImageView, new SimpleImageLoadingListener() {
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
                capTime = loginFormData.getCapTime();
                capSig = loginFormData.getCapSig();
            } else {

                AppLog.e(mContext, loginFormData.getError());

            }
        }

    }

    public static class LoginTask extends AsyncTask<String, Void, Boolean> {

        WeakReference<Context> mContext;
        private final MaterialDialog dialog;
        private final String login;
        private final String password;
        private final Boolean privacy;
        private final String capVal;
        private final String capTime;
        private final String capSig;

        LoginTask(Context context,
                  String login, String password, Boolean privacy,
                  String capVal, String capTime, String capSig) {
            mContext = new WeakReference<>(context);
            this.login = login;
            this.password = password;
            this.privacy = privacy;
            this.capVal = capVal;
            this.capTime = capTime;
            this.capSig = capSig;
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


        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

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