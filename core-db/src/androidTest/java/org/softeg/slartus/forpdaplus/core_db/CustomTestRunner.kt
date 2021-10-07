package org.softeg.slartus.forpdaplus.core_db

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import android.os.StrictMode

import android.os.Bundle
import android.os.StrictMode.ThreadPolicy


@Suppress("unused")
class CustomTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())
        super.onCreate(arguments)
    }
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}