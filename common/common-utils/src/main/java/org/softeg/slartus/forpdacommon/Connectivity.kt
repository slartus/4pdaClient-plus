package org.softeg.slartus.forpdacommon

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Created by slinkin on 27.12.13.
 */
/**
 * Check device's network connectivity and speed
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 */
object Connectivity {
    /**
     * Get the network info
     */
    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm?.activeNetworkInfo
    }

    @JvmStatic
    fun isConnectedWifi(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }
}