package org.softeg.slartus.forpdaplus.core_api.netwotk

import android.net.Uri
import android.os.Build
import org.softeg.slartus.hosthelper.HostHelper
import java.util.*

interface ForPdaService {
    companion object {
        val endPoint = Uri.Builder()
            .scheme(HostHelper.SCHEMA)
            .authority(HostHelper.AUTHORITY)
            .build().toString()

        const val USER_AGENT_CHROME =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36"

        val USER_AGENT_CLIENT by lazy {
            String.format(
                Locale.getDefault(),
                "%s/%s (Android %s; %s; %s %s; %s)",
                "ForPdaClient",
                "4.x",
                Build.VERSION.RELEASE,
                Build.MODEL,
                Build.BRAND,
                Build.DEVICE,
                Locale.getDefault().language
            )
        }

        val MOBILE_COOKIE="Cookie: User-Agent=$USER_AGENT_CLIENT,"
    }
}


