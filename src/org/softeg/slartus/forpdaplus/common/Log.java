package org.softeg.slartus.forpdaplus.common;


import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.Exceptions.AdditionalInfoException;
import org.softeg.slartus.forpdaplus.classes.Exceptions.MessageInfoException;
import org.softeg.slartus.forpdaplus.classes.ShowInBrowserDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public final class Log {
    public static final String EMAIL = "slartus+4pda.Bug@gmail.com";
    public static final String EMAIL_SUBJECT = "4pda.Bug";
    public static String TAG = "org.softeg.slartus.forpdaplus.LOG";

    public static Boolean isHostUnavailableException(Throwable ex) {
        return isHostUnavailableException(ex, false);
    }

    public static Boolean isHostUnavailableException(Throwable ex, Boolean isCause) {

        if (ex == null) return false;
        return ex.getClass() == java.net.UnknownHostException.class ||
                ex.getClass() == HttpHostConnectException.class ||
                ex.getClass() == ClientProtocolException.class ||
                ex.getClass() == NoHttpResponseException.class ||
                (!isCause && isHostUnavailableException(ex.getCause(), true));
    }

    public static Boolean isTimeOutException(Throwable ex) {
        return isTimeOutException(ex, false);
    }

    public static Boolean isTimeOutException(Throwable ex, Boolean isCause) {
        if (ex == null) return false;
        return ex.getClass() == ConnectTimeoutException.class || ex.getClass() == SocketTimeoutException.class ||
                (ex.getClass() == SocketException.class
                        && "recvfrom failed: ETIMEDOUT (Connection timed out)".equals(ex.getMessage())) ||
                (!isCause && isTimeOutException(ex.getCause(), true));
    }

    public static Boolean isException(Throwable ex, Class c) {
        return isException(ex, false, c);
    }

    public static Boolean isException(Throwable ex, Boolean isCause, Class c) {
        if (ex == null) return false;
        return ex.getClass() == c || (!isCause && isException(ex.getCause(), true, c));
    }

    public static void i(Context context, Throwable ex) {
        android.util.Log.i(TAG, getLocation() + ex);
        if (tryShowNetException(context, ex, null)) return;
        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
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

    public static boolean tryShowNetException(Context context, Throwable ex, final Runnable netExceptionAction) {
        try {

            String message = getLocalizedMessage(ex, null);
            if (message == null)
                return false;
            AlertDialog.Builder builder = new AlertDialogBuilder(context)
                    .setTitle(context.getString(org.softeg.slartus.forpdaplus.R.string.CheckConnection))
                    .setMessage(message)
                    .setPositiveButton("ОК", null);


            if (netExceptionAction != null) {
                builder.setNegativeButton(context.getString(org.softeg.slartus.forpdaplus.R.string.Repeat), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        netExceptionAction.run();
                    }
                });
            }
            builder.create().show();
            return true;

        } catch (Throwable loggedEx) {
            android.util.Log.e(TAG, getLocation() + ex);
        }

        return false;
    }

    public static void d(String msg) {
        if (MyApp.getIsDebugMode())
            android.util.Log.d(TAG, getLocation() + msg);
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, getLocation() + msg);
    }

    public static void w(String msg) {
        android.util.Log.w(TAG, getLocation() + msg);
    }

    public static void e(Context context, Throwable ex) {
        e(context, ex, true);
    }

    public static void e(Context context, Throwable ex, Runnable runnable) {
        e(context, ex.getMessage(), ex, runnable, true);
    }

    public static void eToast(Context context, Throwable ex) {
        e(context, ex.getMessage(), ex);
    }

    public static void e(Context context, String message, Throwable ex) {
        String exLocation = getLocation();
        android.util.Log.e(TAG, exLocation + ex);
        try {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
        } catch (Throwable ignoredEx) {

        }
    }

    public static void e(Context context, Throwable ex, Boolean sendReport) {
        e(context, ex.getMessage(), ex, null, sendReport);
    }

    public static void e(Context context, String message, Throwable ex,
                         Runnable netExceptionAction,
                         Boolean sendReport) {
        String exLocation = getLocation();
        android.util.Log.e(TAG, exLocation + ex);

        if (tryShowNetException(context, ex, netExceptionAction)) return;


        if (TextUtils.isEmpty(message))
            message = ex.getMessage();
        if (TextUtils.isEmpty(message))
            message = ex.toString();
        if (context == null) {
//            context = MyApp.getContext();
//            if (context != null)
//                Toast.makeText(context, "4PDA_" + ex.getMessage() + ": " + ex.toString(),
//                        Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (ex.getClass() == ShowInBrowserException.class) {
                ShowInBrowserDialog.showDialog(context, (ShowInBrowserException) ex);
            } else if (ex.getClass() == NotReportException.class) {
                new AlertDialogBuilder(context)
                        .setTitle("Ошибка")
                        .setMessage(message)
                        .setPositiveButton("ОК", null)
                        .create().show();
            } else if (ex.getClass() == MessageInfoException.class) {
                MessageInfoException messageInfoException = (MessageInfoException) ex;
                new AlertDialogBuilder(context)
                        .setTitle(messageInfoException.Title)
                        .setMessage(messageInfoException.Text)
                        .setPositiveButton("ОК", null)
                        .create().show();
            } else if (sendReport) {
                sendReportDialog(context, message, message + "\n " + exLocation + ex, ex);
            }
        } catch (Exception e) {
            e(null, e, false);
        }

    }

    private static void sendReportDialog(final Context context, final String message, final String fullExceptionText, final Throwable ex) {
        try {

            new AlertDialogBuilder(context)
                    .setTitle(context.getString(org.softeg.slartus.forpdaplus.R.string.Error))
                    .setMessage(message)
                    .setIcon(R.drawable.ic_dialog_alert)

                    .setPositiveButton(context.getString(org.softeg.slartus.forpdaplus.R.string.SendReport), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendReport(context, fullExceptionText, ex);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        } catch (Exception e) {
            e(null, e, false);
        }
    }


    public static void sendReport(final Context context, final String fullExceptionText, final Throwable ex) {
        StringBuffer body = purchaseOrder(context, fullExceptionText);

        String addFileBody = null;
        if (ex.getClass() == AdditionalInfoException.class) {
            AdditionalInfoException additionalInfoException = (AdditionalInfoException) ex;
            Bundle args = additionalInfoException.getArgs();

            Boolean addStarted = false;
            for (String key : args.keySet()) {
                if (AdditionalInfoException.ARG_ATTACH_BODY.equals(key)) {
                    addFileBody = "**" + key + "**\n" + args.getString(key);
                    continue;
                }
                if (!addStarted) {
                    body.append("**Дополнительные сведения**").append('\n');
                    addStarted = true;
                }
                body.append(key + ": " + args.get(key)).append('\n');
            }
        }
        String logCatPath = createLogFile(context, addFileBody);

        Email.send(context, EMAIL_SUBJECT, "[spoiler=лог ошибки]\n" + body + "\n[/spoiler]", logCatPath);
    }


    private static StringBuffer purchaseOrder(Context context, final String fullExceptionText) {
        StringBuffer sb = new StringBuffer();
        String packageName = context.getPackageName();
        String version = "unknown";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e(context, e, false);
        }

        sb.append("v=").append(version).append(' ').append(fullExceptionText.substring(50))
                .append("**Опишите действия, приведшие к ошибке**").append('\n')
                .append('\n')
                .append('\n')
                .append("**Приложение**").append('\n')
                .append("app.package=").append(packageName).append('\n')
                .append("app.version=").append(version).append('\n')
                .append('\n')
                .append("**Устройство**").append('\n')
                .append("Build.ID=").append(Build.ID).append('\n')
                .append("Build.DISPLAY=").append(Build.DISPLAY).append('\n')
                .append("Build.PRODUCT=").append(Build.PRODUCT).append('\n')
                .append("Build.DEVICE=").append(Build.DEVICE).append('\n')
                .append("Build.BOARD=").append(Build.BOARD).append('\n')
                .append("Build.MANUFACTURER=").append(Build.MANUFACTURER).append('\n')
                .append("Build.BRAND=").append(Build.BRAND).append('\n')
                .append("Build.MODEL=").append(Build.MODEL).append('\n')
                .append("Build.HARDWARE=").append(Build.HARDWARE).append('\n')
                .append("Build.TYPE=").append(Build.TYPE).append('\n')
                .append("Build.VERSION.INCREMENTAL=").append(Build.VERSION.INCREMENTAL).append('\n')
                .append("Build.VERSION.RELEASE=").append(Build.VERSION.RELEASE).append('\n')
                .append("Build.VERSION.SDK=").append(Build.VERSION.SDK_INT).append('\n')
                .append("Build.VERSION.CODENAME=").append(Build.VERSION.CODENAME).append('\n')
        ;

        sb.append("**Ошибка**").append('\n').append(fullExceptionText).append('\n');
        return sb;
    }


    static int getAPILevel() {

        return Build.VERSION.SDK_INT;
    }

    public static String createLogFile(Context context, String addInfo) {

        try {
            String filePath = Environment.getExternalStorageDirectory() + "/logcat.txt";
            FileWriter fw = new FileWriter(filePath);
            //copyPrefsOnSd(context, fw);
            try {
                fw.write("**LOGCAT**\n\n");


                String[] logcatArguments = {"logcat", "-d", "-v", "time"};


                Process process = Runtime.getRuntime().exec(logcatArguments);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line;
                int linesCount = 300;

                while ((line = bufferedReader.readLine()) != null) {
                    //  fos.write(line.getBytes());
                    fw.append(line).append('\n');

                    linesCount--;
                    if (linesCount == 0) break;
                }

                if (addInfo != null)
                    fw.write(addInfo);

            } finally {
                fw.close();
            }


            return filePath;
        } catch (Exception e) {
            Log.e(context, e, false);
            return e.getMessage();
        }


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

    private static void copyPrefsOnSd(Context context, FileWriter fw) {
        try {

            fw.write("**PREFERENCES**\n");
            File sharedPrefsDir = new File(context.getFilesDir(), "../shared_prefs");


            String packageName = context.getPackageName();
            File inputFile = new File(sharedPrefsDir, packageName + "_preferences.xml");


            FileReader input = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(inputFile));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fw.append(line).append('\n');
            }
            input.close();


        } catch (Exception ex) {
            e(context, ex, false);
        }

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

    public static void v(String tableName, String message, Exception e) {
        android.util.Log.e(TAG, getLocation() + " table: " + tableName
                + message + e);

    }


}
