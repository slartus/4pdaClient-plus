package org.softeg.slartus.forpdaplus.core_api.netwotk

import android.net.Uri
import org.softeg.slartus.hosthelper.HostHelper

interface ForPdaService {
    companion object {
        val endPoint = Uri.Builder()
            .scheme(HostHelper.SCHEMA)
            .authority(HostHelper.AUTHORITY)
            .build().toString()
    }
}


