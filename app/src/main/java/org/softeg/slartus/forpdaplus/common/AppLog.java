package org.softeg.slartus.forpdaplus.common;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.Exceptions.MessageInfoException;
import org.softeg.slartus.forpdaplus.classes.ShowInBrowserDialog;

import java.net.SocketException;
import java.net.SocketTimeoutException;

public final class AppLog {

    private static final String TAG = "AppLog";

    public static void e(Throwable ex) {
        e(null, ex, null);
    }

    public static void e(Context context, Throwable ex) {
        e(context, ex, null);
    }

    public static void toastE(Context context, Throwable ex) {
        String message=ex.getLocalizedMessage();
        if(TextUtils.isEmpty(message))
            message=ex.getMessage();
        if(TextUtils.isEmpty(message))
            message=ex.toString();
        android.util.Log.e(TAG, ex.toString());
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Throwable ignoredEx) {

        }
    }

    public static void e(Context context, Throwable ex, Runnable netExceptionAction) {
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
            new MaterialDialog.Builder(context)
                    .title("Ошибка")
                    .content(message)
                    .positiveText("ОК")
                    .show();
        } else if (ex.getClass() == MessageInfoException.class) {
            MessageInfoException messageInfoException = (MessageInfoException) ex;
            new MaterialDialog.Builder(context)
                    .title(messageInfoException.Title)
                    .content(messageInfoException.Text)
                    .positiveText("ОК")
                    .show();
        } else {
            org.acra.ACRA.getErrorReporter().handleException(ex);

        }
    }

    public static boolean tryShowNetException(Context context, Throwable ex, final Runnable netExceptionAction) {
        try {

            String message = getLocalizedMessage(ex, null);
            if (message == null)
                return false;
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .title("Проверьте соединение")
                    .content(message)
                    .positiveText("ОК");


            if (netExceptionAction != null) {
                builder.negativeText("Повторить")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        netExceptionAction.run();
                    }
                });
            }
            builder.show();
            return true;

        } catch (Throwable loggedEx) {
            android.util.Log.e(TAG, ex.toString());
            return true;
        }
    }

    public static String getLocalizedMessage(Throwable ex, String defaultValue) {
        if (isHostUnavailableException(ex))
            return "Сервер недоступен или не отвечает";
        if (isTimeOutException(ex))
            return "Превышен таймаут ожидания";
        if (isException(ex, MalformedChunkCodingException.class))
            return "Целевой сервер не в состоянии ответить";
        if (isException(ex, SocketException.class))
            return "Соединение разорвано";
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
                clazz == HttpHostConnectException.class ||
                clazz == ClientProtocolException.class ||
                clazz == NoHttpResponseException.class ||
                clazz == org.apache.http.conn.HttpHostConnectException.class ||
                (!isCause && isHostUnavailableException(ex.getCause(), true));
    }

    private static Boolean isTimeOutException(Throwable ex) {
        return isTimeOutException(ex, false);
    }

    private static Boolean isTimeOutException(Throwable ex, Boolean isCause) {
        if (ex == null) return false;
        return (ex.getClass() == ConnectTimeoutException.class) || ex.getClass() == SocketTimeoutException.class ||
                (ex.getClass() == SocketException.class
                        && "recvfrom failed: ETIMEDOUT (Connection timed out)".equals(ex.getMessage())) ||
                (!isCause && isTimeOutException(ex.getCause(), true));
    }

    public static void i(Context mContext, Throwable ex) {
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

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

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
                    continue;
                }
            } catch (ClassNotFoundException e) {
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
