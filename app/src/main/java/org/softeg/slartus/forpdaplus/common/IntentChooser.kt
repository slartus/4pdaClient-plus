package org.softeg.slartus.forpdaplus.common

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.NotReportException
import timber.log.Timber


object IntentChooser {
    @JvmStatic
    fun choose(context: Context, intent: Intent, title: String) {
        if (Build.VERSION.SDK_INT < 33) {
            context.startActivitySafe(Intent.createChooser(intent, title))
        } else {
            runCatching {
                val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
                val titles = resolveInfos.map { it.activityInfo.loadLabel(context.packageManager) }
                    .toTypedArray()
                MaterialDialog.Builder(context)
                    .title(title)
                    .items(*titles)
                    .itemsCallbackSingleChoice(-1) { _, _, which, _ ->
                        val resolveInfo = resolveInfos[which]
                        val activity = resolveInfo.activityInfo
                        intent.component =
                            ComponentName(activity.applicationInfo.packageName, activity.name)
                        context.startActivitySafe(intent)
                        true
                    }
                    .show()
            }.onFailure {
                Timber.e(it)
                context.startActivitySafe(Intent.createChooser(intent, title))
            }
        }
    }

    private fun Context.startActivitySafe(intent: Intent) {
        runCatching {
            startActivity(intent)
        }.onFailure {
            when (it) {
                is ActivityNotFoundException -> {
                    Timber.e(NotReportException("Приложения для запуска не найдены"))
                }
                else -> {
                    Timber.e(it)
                }
            }
        }
    }
}