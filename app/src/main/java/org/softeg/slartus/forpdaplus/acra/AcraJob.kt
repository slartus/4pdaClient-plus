package org.softeg.slartus.forpdaplus.acra

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.CountDownLatch

/*
 * Created by slinkin on 07.02.2018.
 */
class AcraJob(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        try {
            synchronized(AcraReportContainer.instance().lock) {
                while (AcraReportContainer.instance().reports.size > 0) {
                    val report = AcraReportContainer.instance().reports[0]
                    ru.slartus.http.Http.instance.performPost(report.url, report.params)
                    AcraReportContainer.instance().reports.removeAt(0)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, e.message, e)
        }
        return Result.success()
    }

    companion object {
        const val TAG = "acra_job_tag"


        fun scheduleJob() {
            sendLogsAsync()

            val jobRequest: WorkRequest =
                OneTimeWorkRequestBuilder<AcraJob>()
                    .build()
            WorkManager
                .getInstance()
                .enqueue(jobRequest)
        }

        private fun sendLogsAsync() {
            val countDownLatch = CountDownLatch(1)

            object : Thread() {
                override fun run() {
                    // do async operation here
                    try {
                        synchronized(AcraReportContainer.instance().lock) {
                            while (AcraReportContainer.instance().reports.size > 0) {
                                val report = AcraReportContainer.instance().reports[0]
                                ru.slartus.http.Http.instance.performPost(report.url, report.params)
                                AcraReportContainer.instance().reports.removeAt(0)
                            }
                        }
                    } catch (e: Throwable) {
                        Log.e(TAG, e.message, e)
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }.start()

            try {
                countDownLatch.await()
            } catch (ignored: InterruptedException) {
            }
        }
    }
}
