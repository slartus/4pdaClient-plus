package org.softeg.slartus.forpdanotifyservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.util.*

object BackgroundServiceUtils {
    @JvmStatic
    fun requestBackgroundPermission(context: Context) {
        val intent = Intent()
        when (android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault())) {
            "xiaomi" ->
                intent.component =
                        ComponentName("com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity")
            "oppo" ->
                intent.component =
                        ComponentName("com.coloros.safecenter",
                                "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            "vivo" ->
                intent.component =
                        ComponentName("com.vivo.permissionmanager",
                                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
        }

        val list = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.size > 0) {
            context.startActivity(intent)
        }
    }
}