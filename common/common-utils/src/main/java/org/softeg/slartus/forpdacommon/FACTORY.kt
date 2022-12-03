package org.softeg.slartus.forpdacommon

import android.content.Context
import java.lang.ref.WeakReference

object FACTORY {
    @JvmStatic
    lateinit var application: WeakReference<Context>
        private set
    @JvmStatic
    fun init(app: Context) {
        application = WeakReference(app)
    }
}