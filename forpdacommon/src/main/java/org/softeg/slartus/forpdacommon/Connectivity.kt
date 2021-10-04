package org.softeg.slartus.forpdacommon

import android.content.Context

object Connectivity {
    @JvmStatic
    fun isConnectedWifi(context: Context): Boolean {
        return isWifiConnection(context)
    }
}