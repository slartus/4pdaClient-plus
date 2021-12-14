package org.softeg.slartus.forpdaplus.acra

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/*
 * Created by slinkin on 07.02.2018.
 */
class AcraJob : Job() {
    companion object {
        const val TAG = "acra_job_tag"


        fun scheduleJob(): Int {

            sendLogs()
            val jobRequests = JobManager.instance().getAllJobRequestsForTag(TAG)
            if (jobRequests.isNotEmpty()) {
                return jobRequests.iterator().next().jobId
            }

            return JobRequest.Builder(TAG)
                    .setExecutionWindow(1000, TimeUnit.DAYS.toMillis(1))
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .build()
                    .schedule()
        }

        private fun sendLogs(){
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

    override fun onRunJob(params: Params): Result {

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
        return Result.SUCCESS
    }


}
