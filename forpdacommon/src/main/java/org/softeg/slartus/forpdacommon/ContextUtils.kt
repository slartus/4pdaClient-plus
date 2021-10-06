package org.softeg.slartus.forpdacommon

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import timber.log.Timber

val Context.packageInfo: PackageInfo
    get() {
        val packageName = packageName
        return try {
            packageManager.getPackageInfo(
                packageName, PackageManager.GET_META_DATA
            )
        } catch (e1: PackageManager.NameNotFoundException) {
            Timber.e(e1)
            val packageInfo = PackageInfo()
            packageInfo.packageName = packageName
            packageInfo.versionName = "unknown"
            packageInfo.versionCode = 1
            packageInfo
        }
    }

val Context.appFullName: String
    get() {
        var programName = getString(R.string.app_name)
        val pInfo = packageInfo
        programName += " v" + pInfo.versionName + " c" + pInfo.versionCode
        return programName
    }