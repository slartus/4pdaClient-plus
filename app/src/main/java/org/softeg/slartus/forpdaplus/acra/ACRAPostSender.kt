package org.softeg.slartus.forpdaplus.acra

import org.acra.ReportField
import org.acra.data.CrashReportData
import org.softeg.slartus.forpdaplus.App
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import androidx.core.util.Pair

object ACRAPostSender {
    private fun addNotNullParameter(
        parameters: ArrayList<Pair<String, String>>,
        report: CrashReportData,
        key: ReportField
    ) {
        val value = report[key.name]
        if (value != null)
            parameters.add(Pair(key.name, value.toString()))
    }

    @JvmStatic
    fun send(report: CrashReportData) {

        try {
            val parameters = ArrayList<Pair<String, String>>()
            addNotNullParameter(parameters, report, ReportField.APP_VERSION_CODE)

            parameters.add(Pair("DATE", Date().toString()))
            addNotNullParameter(parameters, report, ReportField.REPORT_ID)
            addNotNullParameter(parameters, report, ReportField.APP_VERSION_NAME)
            addNotNullParameter(parameters, report, ReportField.PACKAGE_NAME)
            addNotNullParameter(parameters, report, ReportField.FILE_PATH)
            addNotNullParameter(parameters, report, ReportField.PHONE_MODEL)
            addNotNullParameter(parameters, report, ReportField.ANDROID_VERSION)
            addNotNullParameter(parameters, report, ReportField.BUILD)
            addNotNullParameter(parameters, report, ReportField.BRAND)
            addNotNullParameter(parameters, report, ReportField.PRODUCT)
            addNotNullParameter(parameters, report, ReportField.TOTAL_MEM_SIZE)
            addNotNullParameter(parameters, report, ReportField.AVAILABLE_MEM_SIZE)
            addNotNullParameter(parameters, report, ReportField.CUSTOM_DATA)
            addNotNullParameter(parameters, report, ReportField.STACK_TRACE)
            addNotNullParameter(parameters, report, ReportField.INITIAL_CONFIGURATION)
            addNotNullParameter(parameters, report, ReportField.CRASH_CONFIGURATION)
            addNotNullParameter(parameters, report, ReportField.DISPLAY)
            parameters.add(
                Pair(
                    ReportField.USER_COMMENT.name,
                    App.getInstance().preferences.getString("Login", "empty") ?: ""
                )
            )
            addNotNullParameter(parameters, report, ReportField.USER_APP_START_DATE)
            addNotNullParameter(parameters, report, ReportField.USER_CRASH_DATE)
            addNotNullParameter(parameters, report, ReportField.DUMPSYS_MEMINFO)
            addNotNullParameter(parameters, report, ReportField.DROPBOX)
            addNotNullParameter(parameters, report, ReportField.LOGCAT)
            addNotNullParameter(parameters, report, ReportField.EVENTSLOG)
            addNotNullParameter(parameters, report, ReportField.RADIOLOG)
            addNotNullParameter(parameters, report, ReportField.IS_SILENT)
            addNotNullParameter(parameters, report, ReportField.DEVICE_ID)
            addNotNullParameter(parameters, report, ReportField.INSTALLATION_ID)
            addNotNullParameter(parameters, report, ReportField.USER_EMAIL)
            addNotNullParameter(parameters, report, ReportField.DEVICE_FEATURES)
            addNotNullParameter(parameters, report, ReportField.ENVIRONMENT)
            addNotNullParameter(parameters, report, ReportField.SETTINGS_SYSTEM)
            addNotNullParameter(parameters, report, ReportField.SETTINGS_SECURE)
            addNotNullParameter(parameters, report, ReportField.SHARED_PREFERENCES)
            addNotNullParameter(parameters, report, ReportField.APPLICATION_LOG)
            addNotNullParameter(parameters, report, ReportField.MEDIA_CODEC_LIST)
            addNotNullParameter(parameters, report, ReportField.THREAD_DETAILS)
            AcraReportContainer.instance().addReport(url, parameters)
            AcraJob.scheduleJob()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private const val BASE_URL = "http://slartus.ru/acra.php?email=slartus@gmail.com"
    private const val SHARED_SECRET = "jj8EOkcJLJkTBUAaRJ0BaZDLZQCcwrTc"

    private val url: String
        get() {
            val token = token
            val key = getKey(token)
            return String.format("%s&token=%s&key=%s&", BASE_URL, token, key)
        }

    private fun getKey(token: String): String = md5(String.format("%s+%s", SHARED_SECRET, token))

    private val token: String
        get() = md5(UUID.randomUUID().toString())

    private fun md5(s: String): String {
        var m: MessageDigest? = null
        try {
            m = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        m!!.update(s.toByteArray(), 0, s.length)
        return BigInteger(1, m.digest()).toString(16)
    }
}

