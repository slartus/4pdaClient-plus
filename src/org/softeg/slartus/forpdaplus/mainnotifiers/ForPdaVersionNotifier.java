package org.softeg.slartus.forpdaplus.mainnotifiers;/*
 * Created by slinkin on 03.07.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;

import org.json.JSONObject;
import org.softeg.slartus.forpdacommon.Http;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.common.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForPdaVersionNotifier extends MainNotifier {
    public ForPdaVersionNotifier(NotifiersManager notifiersManager,int period) {
        super(notifiersManager,"ForPdaVersionNotifier", period);
    }

    public void start(Context context) {
//        if (!isTime())
//            return;
        saveTime();
        showNotify(context);
    }

    public void showNotify(final Context context) {
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
                            .compile("&#91;json_info&#93;([\\s\\S]*?)&#91;/json_info&#93;",
                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(page);
                    if (!m.find())
                        return;
                    JSONObject jsonObject = new JSONObject(Html.fromHtml(m.group(1)).toString());
                    jsonObject = jsonObject.getJSONObject(MyApp.getContext().getPackageName());
                    jsonObject = jsonObject.getJSONObject("release");
                    final String version = jsonObject.getString("ver").trim().replace("beta", ".").trim();
                    final String apk = jsonObject.getString("apk");
                    final String info = jsonObject.getString("info");

                    releaseVer = version.replace("beta", ".").trim();
                    siteVersionsNewer = isSiteVersionsNewer(releaseVer, currentVersion);
                    if (siteVersionsNewer) {

                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    addToStack(new AlertDialogBuilder(context)
                                            .setTitle("Новая версия!")
                                            .setMessage("На сайте 4pda.ru обнаружена новая версия: " + version + "\n\n" +
                                                    "Изменения:\n" + info)
                                            .setPositiveButton("Скачать", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    try {
                                                        IntentActivity.tryShowFile((Activity) context, Uri.parse(apk), false);
                                                    } catch (Throwable ex) {
                                                        Log.e(context, ex);
                                                    }
                                                }
                                            })
                                            .setNegativeButton("Закрыть", null).create());

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
