package org.softeg.slartus.forpdaplus.common;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.acra.ACRA;
import org.apache.http.conn.ConnectTimeoutException;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.Exceptions.MessageInfoException;
import org.softeg.slartus.forpdaplus.classes.ShowInBrowserDialog;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import ru.slartus.http.HttpException;

public final class AppLog {

    private static final String TAG = "AppLog";

    public static void e(Throwable ex) {
        e(null, ex, null);
    }

    public static void e(Context context, Throwable ex) {
        e(context, ex, null);
    }

    public static void toastE(Context context, Throwable ex) {
        String message = ex.getLocalizedMessage();
        if (TextUtils.isEmpty(message))
            message = ex.getMessage();
        if (TextUtils.isEmpty(message))
            message = ex.toString();
        android.util.Log.e(TAG, ex.toString());
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) {

        }
    }

    public static void e(Context context, Throwable ex, Runnable netExceptionAction) {
        try {
            String exLocation = getLocation();
            android.util.Log.e(TAG, exLocation + ex);
            if (tryShowNetException(context != null ? context : App.getInstance(), ex, netExceptionAction))
                return;

            String message = ex.getMessage();
            if (TextUtils.isEmpty(message))
                message = ex.toString();
            if (ex.getClass() == ShowInBrowserException.class) {
                ShowInBrowserDialog.showDialog(context, (ShowInBrowserException) ex);
            } else if (ex instanceof NotReportException) {
                assert context != null;
                new MaterialDialog.Builder(context)
                        .title(R.string.error)
                        .content(message)
                        .positiveText(R.string.ok)
                        .show();
            } else if (ex.getClass() == MessageInfoException.class) {
                MessageInfoException messageInfoException = (MessageInfoException) ex;
                assert context != null;
                new MaterialDialog.Builder(context)
                        .title(messageInfoException.Title)
                        .content(messageInfoException.Text)
                        .positiveText(R.string.ok)
                        .show();
            } else {
                ACRA.getErrorReporter().handleException(ex);
            }
        } catch (Throwable error) {
            android.util.Log.e(TAG, error.toString());
        }
    }

    private static boolean tryShowNetException(Context context, Throwable ex, final Runnable netExceptionAction) {
        String message = getLocalizedMessage(ex, null);
        if (message == null)
            return false;
        try {

            if (context != null && context != App.getContext()) {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                        .title(R.string.check_connection)
                        .content(message)
                        .positiveText(R.string.ok);


                if (netExceptionAction != null) {
                    builder.negativeText(R.string.repeat)
                            .onNegative((dialog, which) ->
                                    netExceptionAction.run());

                }
                builder.show();
            } else {
                Toast.makeText(App.getInstance(), message, Toast.LENGTH_SHORT).show();
            }
            return true;

        } catch (Throwable loggedEx) {

            try {
                Toast.makeText(App.getInstance(), message, Toast.LENGTH_SHORT).show();
            } catch (Throwable toastEx) {
                toastEx.printStackTrace();
            }

            android.util.Log.e(TAG, ex.toString());
            return true;
        }
    }

    public static String getLocalizedMessage(Throwable ex, String defaultValue) {
        if (isHostUnavailableException(ex))
            return App.getContext().getString(R.string.server_not_available_or_not_respond);
        if (isTimeOutException(ex))
            return App.getContext().getString(R.string.exceeded_timeout);
        if (isException(ex, ConnectException.class))
            return ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getMessage();
        if (isException(ex, HttpException.class))
            return ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getMessage();
        if (isException(ex, SocketException.class))
            return App.getContext().getString(R.string.connection_lost);
        if (isException(ex, ProtocolException.class))
            return ex.getMessage();

        return defaultValue;
    }

    private static Boolean isException(Throwable ex, Class c) {
        return isException(ex, false, c);
    }

    private static Boolean isException(Throwable ex, Boolean isCause, Class c) {
        return ex != null && (ex.getClass() == c || (!isCause && isException(ex.getCause(), true, c)));
    }

    private static Boolean isHostUnavailableException(Throwable ex) {
        return isHostUnavailableException(ex, false);
    }

    private static Boolean isHostUnavailableException(Throwable ex, Boolean isCause) {

        if (ex == null) return false;
        Class clazz = ex.getClass();

        return clazz == java.net.UnknownHostException.class ||
                (!isCause && isHostUnavailableException(ex.getCause(), true));
    }

    private static Boolean isTimeOutException(Throwable ex) {
        return isTimeOutException(ex, false);
    }

    private static Boolean isTimeOutException(Throwable ex, Boolean isCause) {
        if (ex == null) return false;

        return (isException(ex, ConnectTimeoutException.class)) || isException(ex, SocketTimeoutException.class) ||
                (ex.getClass() == SocketException.class
                        && "recvfrom failed: ETIMEDOUT (Connection timed out)".equals(ex.getMessage())) ||
                (!isCause && isTimeOutException(ex.getCause(), true));
    }

    public static void i(@SuppressWarnings("unused") Context mContext, Throwable ex) {
        Log.i(TAG, ex.toString());
    }

    public static void eToast(Context context, Throwable e) {
        toastE(context, e);
    }

    private static String getLocation() {
        final String className = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread()
                .getStackTrace();
        boolean found = false;

        for (StackTraceElement trace : traces) {
            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":"
                                + trace.getMethodName() + ":"
                                + trace.getLineNumber() + "]: ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getName())) {
                return clazz.getName();
            }
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }
}
