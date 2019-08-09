package org.softeg.slartus.forpdaapi;

/**
 * Created by slinkin on 07.02.14.
 */
public class LoginResult {
    private CharSequence k = "";
    private CharSequence userLogin = "гость";
    private CharSequence userId = "";
    private Boolean success = false;
    private CharSequence loginError="";
    private CharSequence userAvatarUrl;

    public CharSequence getK() {
        return k;
    }

    public void setK(CharSequence k) {
        this.k = k;
    }

    public CharSequence getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(CharSequence login) {
        this.userLogin = login;
    }

    public CharSequence getUserId() {
        return userId;
    }

    public void setUserId(CharSequence id) {
        this.userId = id;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public CharSequence getLoginError() {
        return loginError;
    }

    public void setLoginError(CharSequence loginError) {
        this.loginError = loginError;
    }

    public CharSequence getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(CharSequence userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
}