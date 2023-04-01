package org.softeg.slartus.forpdaplus.acra

import android.app.Application
import org.acra.config.limiter
import org.acra.config.toast
import org.acra.ktx.initAcra
import org.softeg.slartus.forpdaplus.BuildConfig
import org.softeg.slartus.forpdaplus.R

fun Application.configureAcra(){
    initAcra {
        buildConfigClass = BuildConfig::class.java
        toast {
            text = getString(R.string.crash_dialog_text)
            enabled = true
        }
        limiter {

        }
    }
}