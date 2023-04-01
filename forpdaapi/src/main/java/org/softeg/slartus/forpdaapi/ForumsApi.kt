package org.softeg.slartus.forpdaapi

import org.softeg.slartus.hosthelper.HostHelper
import java.util.*

class ForumsApi : ArrayList<Forum>() {
    companion object {


        @Throws(Throwable::class)
        fun markAllAsRead(httpClient: IHttpClient) {
            httpClient.performGet(
                "https://${HostHelper.host}/forum/index.php?act=auth&action=markboard",
                true,
                false
            )
        }
    }
}
