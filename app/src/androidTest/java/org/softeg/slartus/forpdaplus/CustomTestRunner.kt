package org.softeg.slartus.forpdaplus

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import android.os.StrictMode

import android.os.Bundle
import android.os.Debug
import android.os.StrictMode.ThreadPolicy
import android.util.Log

@Suppress("unused")
class CustomTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        Log.d("CustomTestRunner", "onCreate")
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())
        checkHiltTest(arguments?.get("class")?.toString() ?: "")
        super.onCreate(arguments)
    }

    private fun checkHiltTest(className: String) {
        try {
//            val clz = Class.forName(className)
//            hiltTest =
//                clz.declaredAnnotations.any { it.annotationClass.simpleName == "HiltAndroidTest" }

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {

        return if (hiltTest)
            super.newApplication(cl, HiltTestApplication::class.java.name, context)
        else
            super.newApplication(cl, App::class.java.name, context)
    }
    companion object{
        var hiltTest = false
    }
}