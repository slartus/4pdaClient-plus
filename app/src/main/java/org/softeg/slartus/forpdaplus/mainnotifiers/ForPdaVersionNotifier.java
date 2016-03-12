package org.softeg.slartus.forpdaplus.mainnotifiers;/*
 * Created by slinkin on 03.07.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.softeg.slartus.forpdacommon.Http;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForPdaVersionNotifier extends MainNotifier {
    public ForPdaVersionNotifier(NotifiersManager notifiersManager, int period) {
        super(notifiersManager, "ForPdaVersionNotifier", period);
    }

    public void start(Context context) {
        if (!isTime())
            return;
        saveTime();
        showNotify(context);
    }

    public void showNotify(final Context context) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                try {
                    /*String resp = Client.getInstance().performGet("https://api.github.com/repos/slartus/4pdaClient-plus/contents/updateinfo.json");
                    JsonObject jsonObject = new JsonParser().parse(resp).getAsJsonObject();
                    jsonObject = new JsonParser().parse(new String(Base64.decode(jsonObject.get("content").getAsString(), Base64.DEFAULT))).getAsJsonObject();
                    boolean notice = jsonObject.get("notice").getAsBoolean(),
                            warning = jsonObject.get("warning").getAsBoolean(),
                            show_beta_dialog = jsonObject.get("show_beta_dialog").getAsBoolean(),
                            show_release_dialog = jsonObject.get("show_release_dialog").getAsBoolean();
                    String notice_text,
                            warning_text;*/
                    String currentVersion = getAppVersion(App.getContext());
                    currentVersion = currentVersion.trim();

                    String url = "http://4pda.ru/forum/index.php?showtopic=271502";
                    String page = Http.getPage(url, "windows-1251");
                    Matcher m = Pattern
                            .compile("&#91;json_info&#93;([\\s\\S]*?)&#91;/json_info&#93;",
                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(page);
                    if (!m.find())
                        return;
                    JSONObject jsonObject = new JSONObject(Html.fromHtml(m.group(1)).toString());
                    jsonObject = jsonObject.getJSONObject(App.getContext().getPackageName());

                    JSONObject versionObject = jsonObject.getJSONObject("release");

                    if (Preferences.notifyBetaVersions() && jsonObject.has("beta")) {
                        JSONObject betaObject = jsonObject.getJSONObject("beta");
                        if (betaObject != null) {
                            String releaseVersion = versionObject.getString("ver").trim();
                            String betaVersion = betaObject.getString("ver").trim();
                            if (isFirstArgVersionsNewer(betaVersion, releaseVersion))
                                versionObject = betaObject;
                        }
                    }


                    checkVersion(currentVersion, versionObject, handler, context);
                } catch (Throwable ignored) {

                }
            }
        }).start();

    }

    private void checkVersion(String currentVersion, JSONObject versionObject, Handler handler,
                              final Context context) throws JSONException {
        String releaseVer;
        Boolean siteVersionsNewer;

        final String version = versionObject.getString("ver").trim();
        SharedPreferences prefs = App.getInstance().getPreferences();
        if (version.equals(prefs.getString("client.version.4pda", "")))
            return;
        prefs.edit().putString("client.version.4pda", version).apply();

        final String apk = versionObject.getString("apk");
        final String info = versionObject.getString("info");

        releaseVer = version.trim();
        siteVersionsNewer = isFirstArgVersionsNewer(releaseVer, currentVersion);
        if (siteVersionsNewer) {

            handler.post(new Runnable() {
                public void run() {
                    try {
                        addToStack(new MaterialDialog.Builder(context)
                                .title("Новая версия!")
                                .content("На сайте 4pda.ru обнаружена новая версия: " + version + "\n\n" +
                                        "Изменения:\n" + info)
                                .positiveText("Скачать")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        try {
                                            IntentActivity.tryShowFile((Activity) context, Uri.parse(apk), false);
                                        } catch (Throwable ex) {
                                            AppLog.e(context, ex);
                                        }
                                    }
                                })
                                .negativeText("Закрыть")
                                .build());

                    } catch (Exception ex) {
                        AppLog.e(context, new NotReportException("Ошибка проверки новой версии", ex));
                    }

                }
            });

        }
    }

    private static boolean isFirstArgVersionsNewer(String siteVersion, String programVersion) {
        if (siteVersion.contains("beta") && programVersion.contains("beta")) {
            siteVersion = siteVersion.replace("beta", ".");
            programVersion = programVersion.replace("beta", ".");
        } else {
            siteVersion = siteVersion.replaceAll("beta.*", "");
            programVersion = programVersion.replaceAll("beta.*", "");
        }
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
