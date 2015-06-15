package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;
import org.softeg.slartus.forpdacommon.Http;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;

/*
 * Created by slartus on 03.06.2014.
 */
public class MarketVersionNotifier extends MainNotifier {

    public MarketVersionNotifier(NotifiersManager notifiersManager,int period) {
        super(notifiersManager,"MarketVersionNotifier", period);
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
                    String currentVersion = getAppVersion(App.getContext());
                    currentVersion = currentVersion.replace("beta", ".");
                    String url = "https://androidquery.appspot.com//api/market?locale=ru&app=" + context.getPackageName();
                    String page = Http.getPage(url, "utf-8");
                    JSONObject jObj = new JSONObject(page);
                    releaseVer = jObj.getString("version").replace("beta", ".");
                    siteVersionsNewer = isSiteVersionsNewer(releaseVer, currentVersion);
                    if (siteVersionsNewer) {
                        final String finalReleaseVer = releaseVer;
                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    new MaterialDialog.Builder(context)
                                            .title("Новая версия!")
                                            .content("В Google Play обнаружена новая версия: "
                                                    + finalReleaseVer)
                                            .positiveText("Открыть маркет")
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    try {
                                                        ExtUrl.showInBrowser(context, "https://play.google.com/store/apps/details?id=" + context.getPackageName());
                                                    } catch (Throwable ex) {
                                                        AppLog.e(context, ex);
                                                    }
                                                }
                                            })
                                            .negativeText("Отмена")
                                            .show();

                                } catch (Exception ex) {
                                    AppLog.e(context, new NotReportException("Ошибка проверки новой версии", ex));
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
