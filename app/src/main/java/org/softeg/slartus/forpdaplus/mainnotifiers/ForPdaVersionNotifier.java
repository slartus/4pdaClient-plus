package org.softeg.slartus.forpdaplus.mainnotifiers;/*
 * Created by slinkin on 03.07.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BuildConfig;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForPdaVersionNotifier extends MainNotifier {
    public ForPdaVersionNotifier(NotifiersManager notifiersManager, int period) {
        super(notifiersManager, "ForPdaVersionNotifier", period);
    }

    public void start(Context context, boolean toast, boolean now) {
        if (now) {
            checkVersionFromGithub(context, toast);
        } else {

            if (!isTime())
                return;
            saveTime();
//        showNotify(context);
            checkVersionFromGithub(context, toast);
        }
    }

//    public void showNotify(final Context context) {
//        final Handler handler = new Handler();
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    String currentVersion = getAppVersion(App.getContext());
//                    currentVersion = currentVersion.trim();
//
//                    String url = "http://4pda.ru/forum/index.php?showtopic=271502";
//                    String page = Http.getPage(url, "windows-1251");
//                    Matcher m = Pattern
//                            .compile("&#91;json_info&#93;([\\s\\S]*?)&#91;/json_info&#93;",
//                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(page);
//                    if (!m.find())
//                        return;
//                    JSONObject jsonObject = new JSONObject(Html.fromHtml(m.group(1)).toString());
//                    jsonObject = jsonObject.getJSONObject(App.getContext().getPackageName());
//
//                    JSONObject versionObject = jsonObject.getJSONObject("release");
//
//                    if (Preferences.notifyBetaVersions() && jsonObject.has("beta")) {
//                        JSONObject betaObject = jsonObject.getJSONObject("beta");
//                        if (betaObject != null) {
//                            String releaseVersion = versionObject.getString("ver").trim();
//                            String betaVersion = betaObject.getString("ver").trim();
//                            if (isFirstArgVersionsNewer(betaVersion, releaseVersion))
//                                versionObject = betaObject;
//                        }
//                    }
//
//
//                    checkVersion(currentVersion, versionObject, handler, context);
//                } catch (Throwable ignored) {
//
//                }
//            }
//        }).start();
//    }

    private void checkVersionFromGithub(final Context context, final boolean toast) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String currentVersion = getAppVersion(App.getContext());
                currentVersion = currentVersion.trim();
                String link = "https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/updateinfo.json";
                try {

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(link).build();
                    Response response = client.newCall(request).execute();
                    String jo = response.body().string();


                    JSONObject tJson = new JSONObject(jo);
                    boolean object = tJson.getBoolean("show_beta_dialog");
                    if (object) {
                        JSONObject obj = tJson.getJSONObject("update_beta");
                        if (Preferences.notifyBetaVersions() && obj.has("beta")) {
                            JSONObject betaObject = obj.getJSONObject("beta");
                            if (betaObject != null) {
                                String releaseVersion = obj.getString("ver").trim();
                                String betaVersion = betaObject.getString("ver").trim();
                                if (isFirstArgVersionsNewer(betaVersion, releaseVersion)) {
                                    obj = betaObject;
                                }
                            }
                        }
                        checkVersion(currentVersion, obj, handler, context, toast);
                    }

                    if (!toast) {
                        // тут показываем весь букет(если есть что показывать)
                        boolean notice = tJson.getBoolean("notice");
                        if (notice) {
                            String notice_text = tJson.getString("notice_text");
                            if(!notice_text.equals(Preferences.Notice.getNotice()))
                                showDialog(context, false, notice_text, handler);
                        }

                        boolean warning = tJson.getBoolean("warning");
                        if (warning) {
                            String warning_text = tJson.getString("warning_text");
                            if(!warning_text.equals(Preferences.Warning.getWarning()))
                                showDialog(context, true, warning_text, handler);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    msge("error json: " + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    msge("error IOE: " + e.getMessage());
                }
            }
        }).start();
    }

    private void checkVersion(String currentVersion, JSONObject versionObject, Handler handler,
                              final Context context, boolean toast) throws JSONException {
        String releaseVer;
        Boolean siteVersionsNewer;

        final String version = versionObject.getString("ver").trim();
        final SharedPreferences prefs = App.getInstance().getPreferences();
        if (version.equals(prefs.getString("client.version.4pda", "")))
            return;
//        prefs.edit().putString("client.version.4pda", version).apply();

        final String apk = versionObject.getString("apk");
        final String info = versionObject.getString("info");

        releaseVer = version.trim();
        siteVersionsNewer = isFirstArgVersionsNewer(releaseVer, currentVersion);
        if (siteVersionsNewer) {
            handler.post(new Runnable() {
                public void run() {
                    try {
                        addToStack(new MaterialDialog.Builder(context)
                                .title(R.string.update_new_version)
                                .content(context.getString(R.string.update_detected_update) + version + "\n\n" +
                                        context.getString(R.string.update_changes) + info)
                                .positiveText(R.string.update_download)
                                .negativeText(R.string.update_later)
                                .neutralText(R.string.update_forget)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        try {
//                                            IntentActivity.tryShowFile((Activity) context, Uri.parse(apk), false);
                                            DownloadsService.download((Activity) context, apk, false);
                                        } catch (Throwable ex) {
                                            AppLog.e(context, ex);
                                        }
                                    }

                                    @Override
                                    public void onNeutral(MaterialDialog dialog) {
                                        super.onNeutral(dialog);
                                        prefs.edit().putString("client.version.4pda", version).apply();
                                    }
                                })
                                .build());

                    } catch (Exception ex) {
                        AppLog.e(context, new NotReportException(context.getString(R.string.error_check_new_version), ex));
                    }

                }
            });

        } else {
            if (toast) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(context);

                    }
                });
            }
        }
    }

    private static boolean isFirstArgVersionsNewer(String siteVersion, String programVersion) {
        /*Нужно для того, что бы не падало если вдруг кто исортил билд фаил
        * К примеру: написал *beta* с большой буквы*/
        try {
            programVersion = programVersion.trim();
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void showDialog(final Context context, final boolean warning, final String msg_text, Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                addToStack(new MaterialDialog.Builder(context)
                        .title(warning ? context.getString(R.string.notifier_warning) : context.getString(R.string.notifier_notification))
                        .content(Html.fromHtml(msg_text))
                        .positiveText(R.string.notifier_understand)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                if (warning)
                                    Preferences.Warning.setWarning(msg_text);
                                else
                                    Preferences.Notice.setNotice(msg_text);
                            }
                        }).build());
            }
        });
    }

    private void showToast(Context context){
        Toast.makeText(context, R.string.update_no_update, Toast.LENGTH_SHORT).show();
    }

    private void msg(String text) {
        if (BuildConfig.DEBUG)
            Log.d("JSON TEST", text);
    }

    private void msge(String text) {
        if (BuildConfig.DEBUG)
            Log.e("JSON TEST", text);
    }
}
