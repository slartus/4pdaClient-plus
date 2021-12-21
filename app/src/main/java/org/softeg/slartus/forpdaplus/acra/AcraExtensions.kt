package org.softeg.slartus.forpdaplus.acra

import android.app.Application
import org.acra.config.limiter
import org.acra.config.toast
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.softeg.slartus.forpdaplus.BuildConfig
import org.softeg.slartus.forpdaplus.R

fun Application.configureAcra(){
    initAcra {
        buildConfigClass = BuildConfig::class.java
        reportFormat = StringFormat.JSON
        toast {
            text = getString(R.string.crash_dialog_text)
            //opening this block automatically enables the plugin.
        }
        limiter {

        }
    }
}