package org.softeg.slartus.forpdaplus

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class CustomTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())
        super.onCreate(arguments)
    }

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return if (hiltTest)
            super.newApplication(cl, HiltTestApplication::class.java.name, context)
        else
            super.newApplication(cl, App::class.java.name, context)
    }

    companion object {
        var hiltTest = true
    }
}