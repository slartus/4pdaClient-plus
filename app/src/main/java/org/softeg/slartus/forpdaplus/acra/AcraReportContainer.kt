package org.softeg.slartus.forpdaplus.acra

import androidx.core.util.Pair
import java.util.ArrayList

/*
 * Created by slinkin on 07.02.2018.
 */
class AcraReportContainer {

    companion object {
        private var acraReportContainer: AcraReportContainer? = null
        fun instance(): AcraReportContainer {
            if (acraReportContainer == null)
                acraReportContainer = AcraReportContainer()
            return acraReportContainer!!
        }
    }

    val reports = ArrayList<Report>()
    val lock = Any()
    fun addReport(url: String, params: ArrayList<Pair<String, String>>) {
        synchronized(lock) {
            reports.add(Report(url, params))
        }
        AcraJob.scheduleJob()
    }


}

class Report(val url: String, val params: ArrayList<Pair<String, String>>)
