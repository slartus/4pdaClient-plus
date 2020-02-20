package org.softeg.slartus.forpdaplus.mainnotifiers

/*
 * Created by slinkin on 03.07.2014.
 */

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.fromHtml
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.BuildConfig
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.download.DownloadsService
import org.softeg.slartus.forpdaplus.prefs.Preferences
import ru.slartus.http.Http
import kotlin.math.max


class ForPdaVersionNotifier(
        notifiersManager: NotifiersManager,
        period: Int,
        /**
         * Только проверка версий. Сообщение о результате в любом случае
         */
        private val checkVersionOnly: Boolean
) : MainNotifier(notifiersManager, "ForPdaVersionNotifier", period) {

    fun start(context: Context) {
        if (!isTime)
            return
        saveTime()
        checkVersionFromGithub(context)
    }

    private fun checkVersionFromGithub(context: Context) {
        val handler = Handler()
        Thread {
            var currentVersion = getAppVersion()
            currentVersion = currentVersion.trim { it <= ' ' }
            val link = "https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/updateinfo.json"
            try {
                val client = Http.newClientBuiler().build()
                val request = Request.Builder()
                        .url(link)
                        .cacheControl(CacheControl.FORCE_NETWORK)// не исопльуем кеширование
                        .build()

                val responseBody = client.newCall(request).execute().body()?.string()

                val updateInfo = Gson().fromJson(responseBody, UpdateInfo::class.java)

                val currentIsBeta = currentVersion.contains("beta", true)

                val newerVersion = // обновление бет показываем только для бет
                        updateInfo.versions
                                ?.sortedWith(AppVersionComparator())
                                ?.lastOrNull {
                                    currentIsBeta || it.name != "beta"// обновление бет показываем только для бет
                                }

                if (newerVersion != null)
                    checkVersion(currentVersion, newerVersion, handler, context)


                if (!checkVersionOnly) {
                    updateInfo?.notices?.filter { !it.text.isNullOrEmpty() }?.forEach {
                        if (!Preferences.Notice.isNoticed(it.id))
                            showNotice(context, it, handler)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                msge("error IOE: " + e.message)
            }
        }.start()
    }

    @Throws(JSONException::class)
    private fun checkVersion(currentVersion: String, siteVersion: AppVersion, handler: Handler,
                             context: Context) {
        val prefs = App.getInstance().preferences
        if (siteVersion.ver == prefs.getString("client.version.4pda", ""))
            return

        val siteVersionsNewer = AppVersionComparator.compare(siteVersion,
                AppVersion().apply {
                    ver = currentVersion.replace("beta", "")
                    name = if (currentVersion.contains("beta")) "beta" else "release"
                    versionCode = BuildConfig.VERSION_CODE
                }) == 1
        if (siteVersionsNewer) {
            handler.post {
                try {
                    addToStack(MaterialDialog.Builder(context)
                            .title(R.string.update_new_version)
                            .content("${context.getString(R.string.update_detected_update)} " +
                                    "${siteVersion.ver} ${siteVersion.name} \n\n ${context.getString(R.string.update_changes)} ${siteVersion.info}")
                            .positiveText(R.string.update_download)
                            .negativeText(R.string.update_later)
                            .neutralText(R.string.update_forget)
                            .onPositive { _, _ ->
                                try {
                                    //                                            IntentActivity.tryShowFile((Activity) context, Uri.parseCount(apk), false);
                                    DownloadsService.download(context as Activity, siteVersion.apk, false)
                                } catch (ex: Throwable) {
                                    AppLog.e(context, ex)
                                }
                            }
                            .onNeutral { _, _ -> prefs.edit().putString("client.version.4pda", siteVersion.ver).apply() }
                            .build())

                } catch (ex: Exception) {
                    AppLog.e(context, NotReportException(context.getString(R.string.error_check_new_version), ex))
                }
            }
        } else {
            if (checkVersionOnly) {
                handler.post { showToast(context) }
            }
        }
    }

    private fun showNotice(context: Context, appNotice: AppNotice, handler: Handler) {
        handler.post {
            addToStack(MaterialDialog.Builder(context)
                    .title(if (appNotice.type == "warning") context.getString(R.string.notifier_warning) else context.getString(R.string.notifier_notification))
                    .content(appNotice.text.fromHtml())
                    .positiveText(R.string.notifier_understand)
                    .onPositive { _, _ ->
                        Preferences.Notice.setNoticed(appNotice.id)
                    }
                    .build())
        }
    }

    private fun showToast(context: Context) {
        Toast.makeText(context, R.string.update_no_update, Toast.LENGTH_SHORT).show()
    }


    private fun msge(text: String) {
        if (BuildConfig.DEBUG)
            Log.e("JSON TEST", text)
    }
}

class AppVersionComparator : Comparator<AppVersion> {
    companion object {
        fun compare(p0: AppVersion?, p1: AppVersion?): Int {
            val res = compare(p0?.ver?:"", p1?.ver?:"")
            if (res == 0) {
                when {
                    p0?.name?:"" == p1?.name?:"" -> return p0?.versionCode?.compareTo(p1?.versionCode ?: 0)
                            ?: 0
                    p0?.name == "beta" -> return -1
                    p1?.name == "beta" -> return 1
                    p0?.name == "release" && p1?.name.isNullOrEmpty() -> return p0.versionCode.compareTo(p1?.versionCode
                            ?: 0)
                    p1?.name == "release" && p0?.name.isNullOrEmpty() -> return p0?.versionCode?.compareTo(p1.versionCode)
                            ?: 0
                    p0?.name == "release" -> return 1
                    p1?.name == "release" -> return -1
                }
            }
            return res
        }

        fun compare(p0: String?, p1: String?): Int {
            if (p0?:"" == p1?:"") return 0



            val p0IsBeta = (p0?:"").contains("beta", true)
            val p1IsBeta = (p1?:"").contains("beta", true)

            val p0Vals = (p0?:"")
                    .replace("beta", "")
                    .replace("release", "")
                    .split(".")
                    .filterNot { it.isEmpty() }
            val p1Vals = (p1?:"")
                    .replace("beta", "")
                    .replace("release", "")
                    .split(".")
                    .filterNot { it.isEmpty() }

            val maxLength = max(p0Vals.size, p1Vals.size)

            for (i in 0 until maxLength) {
                if (p0Vals.size == i)
                    return -1
                if (p1Vals.size == i)
                    return 1
                val p0Int = p0Vals[i].trim().toIntOrNull()
                val p1Int = p1Vals[i].trim().toIntOrNull()

                if (p0Int == p1Int)
                    continue
                if (p0Int == null)
                    return 1// буквы больше цифр
                if (p1Int == null)
                    return -1
                return if (p0Int < p1Int) -1 else 1
            }
            return when {
                p0IsBeta && p1IsBeta -> 0
                p0IsBeta -> -1
                p1IsBeta -> 1
                else -> 0
            }
        }
    }

    override fun compare(p0: AppVersion?, p1: AppVersion?) =
            AppVersionComparator.compare(p0, p1)

}

private class UpdateInfo {
    var notices: List<AppNotice>? = null
    var versions: List<AppVersion>? = null
}

private class AppNotice {
    var id: String? = null
    var type: String? = null
    var text: String? = ""
}

class AppVersion {
    var name: String? = ""
    var ver: String? = ""
    var apk: String? = ""
    var info: String? = ""
    var versionCode: Int = 0
}
