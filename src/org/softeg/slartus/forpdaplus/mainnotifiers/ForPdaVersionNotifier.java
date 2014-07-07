package org.softeg.slartus.forpdaplus.mainnotifiers;/*
 * Created by slinkin on 03.07.2014.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;

import org.softeg.slartus.forpdacommon.Http;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForPdaVersionNotifier extends MainNotifier {
    public ForPdaVersionNotifier(int period) {
        super("ForPdaVersionNotifier", period);
    }

    public void start(Context context) {
        if (!isTime())
            return;
        saveTime();
        showNotify(context);
    }

    public static void showNotify(final Context context) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {

                try {
                    Boolean siteVersionsNewer;
                    String releaseVer;
                    String currentVersion = getAppVersion(MyApp.getContext());
                    currentVersion = currentVersion.replace("beta", ".").trim();


                    String url = "http://4pda.ru/forum/index.php?showtopic=271502";
                    String page = Http.getPage(url, "windows-1251");
                    Matcher m = Pattern
                            .compile("<b>версия:\\s*(.*?)\\s*/s*([^<]*)\\s*</b>",
                                    Pattern.CASE_INSENSITIVE).matcher(page);
                    if (!m.find())
                        return;

                    String version = "org.softeg.slartus.forpdaplus".equals(MyApp.getContext().getPackageName()) ?
                            m.group(2) : m.group(1);

                    releaseVer = version.replace("beta", ".").trim();
                    siteVersionsNewer = isSiteVersionsNewer(releaseVer, currentVersion);
                    if (siteVersionsNewer) {
                        final String finalReleaseVer = releaseVer;
                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    new AlertDialogBuilder(context)
                                            .setTitle("Новая версия!")
                                            .setMessage("На сайте 4pda.ru обнаружена новая версия: "
                                                    + finalReleaseVer)
                                            .setPositiveButton("Открыть", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    try {
                                                        ExtUrl.showInBrowser(context, "http://4pda.ru/forum/index.php?showtopic=271502");
                                                    } catch (Throwable ex) {
                                                        Log.e(context, ex);
                                                    }
                                                }
                                            })
                                            .setNegativeButton("Отмена", null)
                                            .create().show();

                                } catch (Exception ex) {
                                    Log.e(context, new NotReportException("Ошибка проверки новой версии", ex));
                                }

                            }
                        });
                    }
                } catch (Throwable ignored) {

                }
            }
        }).start();

    }

    private static boolean isSiteVersionsNewer(String siteVersion, String programVersion) {
        String[] siteVersionVals = TextUtils.split(siteVersion, "\\.");
        String[] programVersionVals = TextUtils.split(programVersion, "\\.");

        for (int i = 0; i < siteVersionVals.length; i++) {
            int siteVersionVal = Integer.parseInt(siteVersionVals[i]);

            if (programVersionVals.length == i)// значит на сайте версия с доп. циферкой
                return true;

            int programVersionVal = Integer.parseInt(programVersionVals[i]);

            if (siteVersionVal == programVersionVal) continue;
            return siteVersionVal > programVersionVal;
        }
        return false;
    }
}
